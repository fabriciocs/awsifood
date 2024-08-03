package com.ifood.app.domain;

import static com.ifood.app.domain.MenuTestSamples.*;
import static com.ifood.app.domain.RestaurantTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.ifood.app.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RestaurantTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Restaurant.class);
        Restaurant restaurant1 = getRestaurantSample1();
        Restaurant restaurant2 = new Restaurant();
        assertThat(restaurant1).isNotEqualTo(restaurant2);

        restaurant2.setId(restaurant1.getId());
        assertThat(restaurant1).isEqualTo(restaurant2);

        restaurant2 = getRestaurantSample2();
        assertThat(restaurant1).isNotEqualTo(restaurant2);
    }

    @Test
    void menuTest() {
        Restaurant restaurant = getRestaurantRandomSampleGenerator();
        Menu menuBack = getMenuRandomSampleGenerator();

        restaurant.addMenu(menuBack);
        assertThat(restaurant.getMenus()).containsOnly(menuBack);
        assertThat(menuBack.getRestaurant()).isEqualTo(restaurant);

        restaurant.removeMenu(menuBack);
        assertThat(restaurant.getMenus()).doesNotContain(menuBack);
        assertThat(menuBack.getRestaurant()).isNull();

        restaurant.menus(new HashSet<>(Set.of(menuBack)));
        assertThat(restaurant.getMenus()).containsOnly(menuBack);
        assertThat(menuBack.getRestaurant()).isEqualTo(restaurant);

        restaurant.setMenus(new HashSet<>());
        assertThat(restaurant.getMenus()).doesNotContain(menuBack);
        assertThat(menuBack.getRestaurant()).isNull();
    }
}
