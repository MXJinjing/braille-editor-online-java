package wang.jinjing.common.util;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import wang.jinjing.common.pojo.DTO.BaseDTO;
import wang.jinjing.common.pojo.VO.BaseVO;
import wang.jinjing.common.pojo.entity.BaseEntity;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BeanConvertUtil {

    /**
     * 将DTO转换为Entity
     */
    public static <E extends BaseEntity, T extends BaseDTO> E convertToEntity(Class<E> entityClass, T dto) {
        try {
            E entity = entityClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(dto, entity);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert DTO to Entity", e);
        }
    }

    /**
     * 将Entity转换为VO
     */
    public static <E extends BaseEntity, V extends BaseVO> V convertToVo(Class<V> voClass, E entity) {
        try {
            V vo = voClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Entity to VO", e);
        }
    }

    public static <E extends BaseEntity, DTO extends BaseDTO> List<E> convertToEntityList(Class<E> eClass, List<DTO> dtos) {
        try {
            List<E> es = convertList(dtos, eClass);
            return es;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert DTO list to Entity list", e);
        }
    }

    public static <E extends BaseEntity, V extends BaseVO> List<V> convertToVoList(Class<V> vClass, List<E> entities) {
        try {
            List<V> vos = convertList(entities, vClass);
            return vos;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Entity list to VO list", e);
        }
    }

    public static <E extends BaseEntity, V extends BaseVO> Page<V> convertToVoPage(Class<V> voClass, Page<E> tPage) {
        Page<V> voPage = new Page<>(tPage.getCurrent(), tPage.getSize(), tPage.getTotal());
        voPage.setRecords(convertToVoList(voClass, tPage.getRecords()));
        return voPage;
    }


    /**
     * 转换对象列表
     *
     * @param sources     源对象列表
     * @param targetClass 目标类类型
     * @return 转换后的目标对象列表
     */
    public static <T> List<T> convertList(List<?> sources, Class<T> targetClass) {
        List<T> targets = new ArrayList<>();
        try {
            for (Object source : sources) {
                T target = targetClass.getDeclaredConstructor().newInstance();
                BeanUtils.copyProperties(source, target);
                targets.add(target);
            }
            return targets;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Entity list", e);
        }
    }

}



