package wang.jinjing.common.util;


import org.springframework.beans.BeanUtils;
import wang.jinjing.common.pojo.DTO.BaseDTO;
import wang.jinjing.common.pojo.VO.BaseVO;
import wang.jinjing.common.pojo.entity.BaseEntity;

import java.util.List;
import java.util.stream.Collectors;

public class BeanConvertUtil {


    public static <E extends BaseEntity, T extends BaseDTO> E convertToEntity(Class<E> entityClass, T dto) {
        try {
            E entity = entityClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(dto, entity);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert DTO to Entity", e);
        }
    }

    public static <E extends BaseEntity, V extends BaseVO> V convertToVo(Class<V> voClass, E entity) {
        try {
            V vo = voClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Entity to VO", e);
        }
    }

    /**
     * 将源对象的属性拷贝到目标对象
     *
     * @param source 源对象（如DTO）
     * @param target 目标对象（如VO）
     */
    public static void copyProperties(Object source, Object target) {
        try {
            BeanUtils.copyProperties(target, source); // 目标在前，源在后
        } catch (Exception e) {
            throw new RuntimeException("属性复制失败", e);
        }
    }

    /**
     * 转换单个对象（通过反射创建目标实例）
     *
     * @param source      源对象
     * @param targetClass 目标类类型
     * @return 转换后的目标对象
     */
    public static <T> T convert(Object source, Class<T> targetClass) {
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(target, source);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("对象转换失败", e);
        }
    }

    /**
     * 转换对象列表
     *
     * @param sources     源对象列表
     * @param targetClass 目标类类型
     * @return 转换后的目标对象列表
     */
    public static <T> List<T> convertList(List<?> sources, Class<T> targetClass) {
        return sources.stream()
                .map(source -> convert(source, targetClass))
                .collect(Collectors.toList());
    }
}
