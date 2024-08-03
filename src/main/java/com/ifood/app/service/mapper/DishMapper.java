package com.ifood.app.service.mapper;

import com.ifood.app.domain.Dish;
import com.ifood.app.domain.Menu;
import com.ifood.app.service.dto.DishDTO;
import com.ifood.app.service.dto.MenuDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Dish} and its DTO {@link DishDTO}.
 */
@Mapper(componentModel = "spring")
public interface DishMapper extends EntityMapper<DishDTO, Dish> {
    @Mapping(target = "menu", source = "menu", qualifiedByName = "menuId")
    DishDTO toDto(Dish s);

    @Named("menuId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    MenuDTO toDtoMenuId(Menu menu);
}
