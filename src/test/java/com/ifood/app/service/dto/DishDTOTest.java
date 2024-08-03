package com.ifood.app.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.ifood.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DishDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DishDTO.class);
        DishDTO dishDTO1 = new DishDTO();
        dishDTO1.setId(1L);
        DishDTO dishDTO2 = new DishDTO();
        assertThat(dishDTO1).isNotEqualTo(dishDTO2);
        dishDTO2.setId(dishDTO1.getId());
        assertThat(dishDTO1).isEqualTo(dishDTO2);
        dishDTO2.setId(2L);
        assertThat(dishDTO1).isNotEqualTo(dishDTO2);
        dishDTO1.setId(null);
        assertThat(dishDTO1).isNotEqualTo(dishDTO2);
    }
}
