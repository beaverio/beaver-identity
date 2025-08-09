package com.beaver.identity.common.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GenericMapper {

    private final ModelMapper modelMapper;

    @Autowired
    public GenericMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Convert entity to specified DTO type
     */
    public <D> D toDto(Object entity, Class<D> dtoClass) {
        // Check if the DTO class has a custom mapping method
        try {
            var fromEntityMethod = dtoClass.getMethod("fromEntity", entity.getClass());
            return (D) fromEntityMethod.invoke(null, entity);
        } catch (Exception e) {
            // Fall back to ModelMapper if no custom method exists
            return modelMapper.map(entity, dtoClass);
        }
    }

    /**
     * Convert list of entities to list of DTOs
     */
    public <S, D> List<D> toDto(List<S> sourceList, Class<D> dtoClass) {
        return sourceList.stream()
                .map(source -> toDto(source, dtoClass))
                .toList();
    }

    /**
     * Update existing entity with DTO values
     */
    public void updateEntity(Object dto, Object entity) {
        modelMapper.map(dto, entity);
    }

    /**
     * Convert DTO to entity
     */
    public <E> E toEntity(Object dto, Class<E> entityClass) {
        return modelMapper.map(dto, entityClass);
    }
}
