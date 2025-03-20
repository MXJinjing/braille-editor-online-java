package wang.jinjing.editor.controller.manage;


import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import wang.jinjing.common.controller.RestfulAPIsController;
import wang.jinjing.common.pojo.DTO.BaseDTO;
import wang.jinjing.common.pojo.VO.BaseVO;
import wang.jinjing.common.pojo.entity.BaseEntity;
import wang.jinjing.common.util.BeanConvertUtil;

import java.util.List;

@NoArgsConstructor
public abstract class AbstractManageController <DTO extends BaseDTO, E extends BaseEntity, VO extends BaseVO,
        M extends wang.jinjing.common.service.BasicCRUDService<E> & wang.jinjing.common.service.BatchCRUDService<E>> extends RestfulAPIsController<DTO> {

    protected M service;

    private Class<E> entityClass;
    private Class<VO> VoClass;

    public AbstractManageController(M service, Class<E> entityClass, Class<VO> VoClass) {
        this.entityClass = entityClass;
        this.service = service;
        this.VoClass = VoClass;
    }


    @Override
    public ResponseEntity<?> addOne(DTO dto) {
        E e =  BeanConvertUtil.convertToEntity(entityClass,dto);
        int i = service.addOne(e);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> updateOne(Long id, DTO dto) {
        E e =  BeanConvertUtil.convertToEntity(entityClass,dto);
        int i = service.updateOne(id,e);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> deleteOne(Long id) {
        int i = service.deleteOne(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> findById(Long id) {
        E e = service.findById(id);
        VO vo = BeanConvertUtil.convertToVo(VoClass,e);
        return ResponseEntity.ok(vo);
    }

    @Override
    public ResponseEntity<?> addBatch(List<DTO> dtos) {
        return null;
    }

    @Override
    public ResponseEntity<?> deleteBatch(List<Long> ids) {
        return null;
    }

    @Override
    public ResponseEntity<?> listPage(int page, int size) {
        return null;
    }

    @Override
    public ResponseEntity<?> search(DTO dto) {
        return null;
    }
}