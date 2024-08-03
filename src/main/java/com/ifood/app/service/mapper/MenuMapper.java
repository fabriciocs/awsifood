package com.ifood.app.service.mapper;

import com.ifood.app.domain.Menu;
import com.ifood.app.domain.Restaurant;
import com.ifood.app.service.dto.MenuDTO;
import com.ifood.app.service.dto.RestaurantDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Menu} and its DTO {@link MenuDTO}.
 */
@Mapper(componentModel = "spring")
public interface MenuMapper extends EntityMapper<MenuDTO, Menu> {
    @Mapping(target = "restaurant", source = "restaurant", qualifiedByName = "restaurantId")
    MenuDTO toDto(Menu s);

    @Named("restaurantId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    RestaurantDTO toDtoRestaurantId(Restaurant restaurant);
}
