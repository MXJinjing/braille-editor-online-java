package wang.jinjing.common.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wang.jinjing.common.controller.interfaces.RestfulAPIs;
import wang.jinjing.common.pojo.DTO.BaseDTO;
import wang.jinjing.common.pojo.ErrorEnum;
import wang.jinjing.common.pojo.VO.BaseVO;
import wang.jinjing.common.pojo.VO.OperationResultVO;
import wang.jinjing.common.pojo.VO.PageVO;
import wang.jinjing.common.pojo.entity.BaseEntity;
import wang.jinjing.common.repository.BaseRepository;
import wang.jinjing.common.service.AbstractCRUDService;
import wang.jinjing.common.service.BasicCRUDService;
import wang.jinjing.common.util.BeanConvertUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class RestfulAPIsController<
            DTO extends BaseDTO,
            E extends BaseEntity,
            VO extends BaseVO,
            R extends BaseRepository<E>,
            S extends BasicCRUDService<E,VO>
        > implements RestfulAPIs<DTO> {

    Class<E> eClass;
    Class<VO> voClass;

    public RestfulAPIsController(S crudService) {
        this.crudService = crudService;
    }

    public void setClasses(Class<E> eClass, Class<VO> voClass){
        this.eClass = eClass;
        this.voClass = voClass;
    }

    protected S crudService;

    @PostMapping
    @Override
    public ResponseEntity<?> addOne(@RequestBody DTO dto){
        E e = BeanConvertUtil.convertToEntity(eClass, dto);
        long id = crudService.addOne(e);
        VO vo = BeanConvertUtil.convertToVo(voClass, e);
        return ResponseEntity.ok(vo);
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<?> updateOne(@PathVariable Long id, @RequestBody DTO dto){
        E e = BeanConvertUtil.convertToEntity(eClass, dto);
        int i = crudService.updateOne(id, e);
        return (i > 0)? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<?> deleteOne(@PathVariable Long id){
        int i = crudService.deleteOne(id);
        return (i > 0)? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<?> findById(@PathVariable Long id){
        VO vo = crudService.findById(id);
        return ResponseEntity.ok(vo);
    }

    @PostMapping("/batch")
    @Override
    public ResponseEntity<?> addBatch(@RequestBody List<DTO> dtos){
        List<E> es = BeanConvertUtil.convertToEntityList(eClass, dtos);
        Map<Integer, ErrorEnum> longErrorEnumMap = crudService.addBatch(es);

        // 构造 OperationResult
        AtomicBoolean isAllSuccess = new AtomicBoolean(false);
        List<OperationResultVO> resultVOS = new ArrayList<>();
        longErrorEnumMap.forEach((k,v) -> {
            if(Objects.isNull(v)){
                resultVOS.add(OperationResultVO.success(k,dtos.get(Math.toIntExact(k))));
                isAllSuccess.set(true);
            }else {
                resultVOS.add(OperationResultVO.fail(k, v.getMessage()));
            }
        });

        if(isAllSuccess.get()){
            return ResponseEntity.ok(resultVOS);
        } else {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(resultVOS);
        }
    }


    @PutMapping("/batch")
    public ResponseEntity<?> updateBatch(@RequestBody List<DTO> dtos){
        List<E> es = BeanConvertUtil.convertToEntityList(eClass, dtos);
        Map<Long, ErrorEnum> longErrorEnumMap = crudService.updateBatch(es);
        // 构造 OperationResult
        return buildOperationResult(longErrorEnumMap);
    }

    @DeleteMapping("/batch")
    @Override
    public ResponseEntity<?> deleteBatch(@RequestBody List<Long> ids){
        Map<Long, ErrorEnum> longErrorEnumMap = crudService.deleteBatch(ids);
        // 构造 OperationResult
        return buildOperationResult(longErrorEnumMap);
    }

    private ResponseEntity<?> buildOperationResult(Map<Long, ErrorEnum> map) {
        AtomicBoolean isAllSuccess = new AtomicBoolean(false);

        // 构造 OperationResult
        List<OperationResultVO> resultVOS = new ArrayList<>();
        map.forEach((k,v) -> {
            if(Objects.isNull(v)){
                resultVOS.add(OperationResultVO.success(k));
                isAllSuccess.set(true);
            }else {
                resultVOS.add(OperationResultVO.fail(k, v.getMessage()));
            }
        });

        if(resultVOS.isEmpty()){
           return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        // 如果全部成功则返回200，否则返回207（MULTI_STATUS）
        if(isAllSuccess.get()){
            return ResponseEntity.ok(resultVOS);
        } else {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(resultVOS);
        }
    }


    @GetMapping("")
    @Override
    public ResponseEntity<?> search(@ModelAttribute DTO dto,
                                    @RequestParam(required = false) Map<String, Object> args,
                                    @RequestParam(required = false) String sorts,
                                    @RequestParam(required = false) Integer page,
                                    @RequestParam(required = false) Integer size){
        E e = BeanConvertUtil.convertToEntity(eClass, dto);
        Sort sort = getSortFromMap(sorts);
        // 从args中移除page/size/sorts参数
        args.remove("page");
        args.remove("size");
        args.remove("sorts");

        if(page != null && size != null && page > 0 && size > 0){
            Page<VO> pageVO = crudService.searchPage(e, args, page, size, sort);
            return ResponseEntity.ok(pageVO);

        } else{
            List<VO> vos = crudService.searchList(e, args, sort);
            return ResponseEntity.ok(vos);
        }
    }


    /**
     * 从sorts参数中获取Sort对象
     * @param sorts sorts参数
     * @return Sort对象
     */
    public static Sort getSortFromMap(String sorts) {
        if(sorts == null || sorts.isEmpty()){
            return Sort.unsorted();
        }

        // 先分割分号，再分割逗号并加入到Sort.Order中
        Set<String> set = new HashSet<>();
        List<Sort.Order> orders = new ArrayList<>();

        String[] split1 = sorts.split(";");
        for(String s : split1){
            String[] split2 = s.split(",");
            // 检测参数是否有效
            if(split2.length != 2){
                throw new IllegalArgumentException("sorts参数格式错误");
            }else
            if(!split2[1].equalsIgnoreCase("ASC") && !split2[1].equalsIgnoreCase("DESC")){
                throw new IllegalArgumentException("sorts参数格式错误");
            }
            // 校验参数是否重复
            if(set.contains(split2[0])){
                throw new IllegalArgumentException("sorts参数重复");
            }
            set.add(split2[0]);
            Sort.Order order = new Sort.Order(Sort.Direction.fromString(split2[1].toUpperCase()), split2[0]);
            orders.add(order);
        }
        return Sort.by(orders);
    }

}
