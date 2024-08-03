package com.ifood.app.domain;

import static com.ifood.app.domain.AssertUtils.bigDecimalCompareTo;
import static org.assertj.core.api.Assertions.assertThat;

public class DishAsserts {

    /**
     * Asserts that the entity has all properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertDishAllPropertiesEquals(Dish expected, Dish actual) {
        assertDishAutoGeneratedPropertiesEquals(expected, actual);
        assertDishAllUpdatablePropertiesEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all updatable properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertDishAllUpdatablePropertiesEquals(Dish expected, Dish actual) {
        assertDishUpdatableFieldsEquals(expected, actual);
        assertDishUpdatableRelationshipsEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all the auto generated properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertDishAutoGeneratedPropertiesEquals(Dish expected, Dish actual) {
        assertThat(expected)
            .as("Verify Dish auto generated properties")
            .satisfies(e -> assertThat(e.getId()).as("check id").isEqualTo(actual.getId()));
    }

    /**
     * Asserts that the entity has all the updatable fields set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertDishUpdatableFieldsEquals(Dish expected, Dish actual) {
        assertThat(expected)
            .as("Verify Dish relevant properties")
            .satisfies(e -> assertThat(e.getName()).as("check name").isEqualTo(actual.getName()))
            .satisfies(e -> assertThat(e.getPrice()).as("check price").usingComparator(bigDecimalCompareTo).isEqualTo(actual.getPrice()))
            .satisfies(e -> assertThat(e.getDescription()).as("check description").isEqualTo(actual.getDescription()))
            .satisfies(e -> assertThat(e.getSpicyLevel()).as("check spicyLevel").isEqualTo(actual.getSpicyLevel()));
    }

    /**
     * Asserts that the entity has all the updatable relationships set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertDishUpdatableRelationshipsEquals(Dish expected, Dish actual) {
        assertThat(expected)
            .as("Verify Dish relationships")
            .satisfies(e -> assertThat(e.getMenu()).as("check menu").isEqualTo(actual.getMenu()));
    }
}
