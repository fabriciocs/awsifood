package com.ifood.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DishTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Dish getDishSample1() {
        return new Dish().id(1L).name("name1").description("description1").spicyLevel(1);
    }

    public static Dish getDishSample2() {
        return new Dish().id(2L).name("name2").description("description2").spicyLevel(2);
    }

    public static Dish getDishRandomSampleGenerator() {
        return new Dish()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .spicyLevel(intCount.incrementAndGet());
    }
}
