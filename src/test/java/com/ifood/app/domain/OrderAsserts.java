package com.ifood.app.domain;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderAsserts {

    /**
     * Asserts that the entity has all properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertOrderAllPropertiesEquals(Order expected, Order actual) {
        assertOrderAutoGeneratedPropertiesEquals(expected, actual);
        assertOrderAllUpdatablePropertiesEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all updatable properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertOrderAllUpdatablePropertiesEquals(Order expected, Order actual) {
        assertOrderUpdatableFieldsEquals(expected, actual);
        assertOrderUpdatableRelationshipsEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all the auto generated properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertOrderAutoGeneratedPropertiesEquals(Order expected, Order actual) {
        assertThat(expected)
            .as("Verify Order auto generated properties")
            .satisfies(e -> assertThat(e.getId()).as("check id").isEqualTo(actual.getId()));
    }

    /**
     * Asserts that the entity has all the updatable fields set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertOrderUpdatableFieldsEquals(Order expected, Order actual) {
        assertThat(expected)
            .as("Verify Order relevant properties")
            .satisfies(e -> assertThat(e.getOrderDate()).as("check orderDate").isEqualTo(actual.getOrderDate()))
            .satisfies(e -> assertThat(e.getStatus()).as("check status").isEqualTo(actual.getStatus()));
    }

    /**
     * Asserts that the entity has all the updatable relationships set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertOrderUpdatableRelationshipsEquals(Order expected, Order actual) {
        assertThat(expected)
            .as("Verify Order relationships")
            .satisfies(e -> assertThat(e.getCustomer()).as("check customer").isEqualTo(actual.getCustomer()));
    }
}
