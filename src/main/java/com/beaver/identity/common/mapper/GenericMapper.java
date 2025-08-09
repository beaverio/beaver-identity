package com.beaver.identity.common.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        return modelMapper.map(entity, dtoClass);
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
