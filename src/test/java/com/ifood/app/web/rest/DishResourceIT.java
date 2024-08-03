package com.ifood.app.web.rest;

import static com.ifood.app.domain.DishAsserts.*;
import static com.ifood.app.web.rest.TestUtil.createUpdateProxyForBean;
import static com.ifood.app.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifood.app.IntegrationTest;
import com.ifood.app.domain.Dish;
import com.ifood.app.repository.DishRepository;
import com.ifood.app.service.dto.DishDTO;
import com.ifood.app.service.mapper.DishMapper;
import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link DishResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class DishResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal(1);
    private static final BigDecimal UPDATED_PRICE = new BigDecimal(2);

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Integer DEFAULT_SPICY_LEVEL = 1;
    private static final Integer UPDATED_SPICY_LEVEL = 2;

    private static final String ENTITY_API_URL = "/api/dishes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private WebTestClient webTestClient;

    private Dish dish;

    private Dish insertedDish;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Dish createEntity() {
        Dish dish = new Dish().name(DEFAULT_NAME).price(DEFAULT_PRICE).description(DEFAULT_DESCRIPTION).spicyLevel(DEFAULT_SPICY_LEVEL);
        return dish;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Dish createUpdatedEntity() {
        Dish dish = new Dish().name(UPDATED_NAME).price(UPDATED_PRICE).description(UPDATED_DESCRIPTION).spicyLevel(UPDATED_SPICY_LEVEL);
        return dish;
    }

    @BeforeEach
    public void initTest() {
        dish = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedDish != null) {
            dishRepository.delete(insertedDish).block();
            insertedDish = null;
        }
    }

    @Test
    void createDish() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Dish
        DishDTO dishDTO = dishMapper.toDto(dish);
        var returnedDishDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(dishDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(DishDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the Dish in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDish = dishMapper.toEntity(returnedDishDTO);
        assertDishUpdatableFieldsEquals(returnedDish, getPersistedDish(returnedDish));

        insertedDish = returnedDish;
    }

    @Test
    void createDishWithExistingId() throws Exception {
        // Create the Dish with an existing ID
        dish.setId(1L);
        DishDTO dishDTO = dishMapper.toDto(dish);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(dishDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Dish in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        dish.setName(null);

        // Create the Dish, which fails.
        DishDTO dishDTO = dishMapper.toDto(dish);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(dishDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkPriceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        dish.setPrice(null);

        // Create the Dish, which fails.
        DishDTO dishDTO = dishMapper.toDto(dish);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(dishDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllDishes() {
        // Initialize the database
        insertedDish = dishRepository.save(dish).block();

        // Get all the dishList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].price")
            .value(hasItem(sameNumber(DEFAULT_PRICE)))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION))
            .jsonPath("$.[*].spicyLevel")
            .value(hasItem(DEFAULT_SPICY_LEVEL));
    }

    @Test
    void getDish() {
        // Initialize the database
        insertedDish = dishRepository.save(dish).block();

        // Get the dish
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, dish.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME))
            .jsonPath("$.price")
            .value(is(sameNumber(DEFAULT_PRICE)))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION))
            .jsonPath("$.spicyLevel")
            .value(is(DEFAULT_SPICY_LEVEL));
    }

    @Test
    void getNonExistingDish() {
        // Get the dish
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingDish() throws Exception {
        // Initialize the database
        insertedDish = dishRepository.save(dish).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the dish
        Dish updatedDish = dishRepository.findById(dish.getId()).block();
        updatedDish.name(UPDATED_NAME).price(UPDATED_PRICE).description(UPDATED_DESCRIPTION).spicyLevel(UPDATED_SPICY_LEVEL);
        DishDTO dishDTO = dishMapper.toDto(updatedDish);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, dishDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(dishDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Dish in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDishToMatchAllProperties(updatedDish);
    }

    @Test
    void putNonExistingDish() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dish.setId(longCount.incrementAndGet());

        // Create the Dish
        DishDTO dishDTO = dishMapper.toDto(dish);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, dishDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(dishDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Dish in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchDish() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dish.setId(longCount.incrementAndGet());

        // Create the Dish
        DishDTO dishDTO = dishMapper.toDto(dish);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(dishDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Dish in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamDish() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dish.setId(longCount.incrementAndGet());

        // Create the Dish
        DishDTO dishDTO = dishMapper.toDto(dish);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(dishDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Dish in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateDishWithPatch() throws Exception {
        // Initialize the database
        insertedDish = dishRepository.save(dish).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the dish using partial update
        Dish partialUpdatedDish = new Dish();
        partialUpdatedDish.setId(dish.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedDish.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedDish))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Dish in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDishUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedDish, dish), getPersistedDish(dish));
    }

    @Test
    void fullUpdateDishWithPatch() throws Exception {
        // Initialize the database
        insertedDish = dishRepository.save(dish).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the dish using partial update
        Dish partialUpdatedDish = new Dish();
        partialUpdatedDish.setId(dish.getId());

        partialUpdatedDish.name(UPDATED_NAME).price(UPDATED_PRICE).description(UPDATED_DESCRIPTION).spicyLevel(UPDATED_SPICY_LEVEL);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedDish.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedDish))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Dish in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDishUpdatableFieldsEquals(partialUpdatedDish, getPersistedDish(partialUpdatedDish));
    }

    @Test
    void patchNonExistingDish() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dish.setId(longCount.incrementAndGet());

        // Create the Dish
        DishDTO dishDTO = dishMapper.toDto(dish);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, dishDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(dishDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Dish in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchDish() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dish.setId(longCount.incrementAndGet());

        // Create the Dish
        DishDTO dishDTO = dishMapper.toDto(dish);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(dishDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Dish in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamDish() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        dish.setId(longCount.incrementAndGet());

        // Create the Dish
        DishDTO dishDTO = dishMapper.toDto(dish);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(dishDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Dish in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteDish() {
        // Initialize the database
        insertedDish = dishRepository.save(dish).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the dish
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, dish.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return dishRepository.count().block();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Dish getPersistedDish(Dish dish) {
        return dishRepository.findById(dish.getId()).block();
    }

    protected void assertPersistedDishToMatchAllProperties(Dish expectedDish) {
        assertDishAllPropertiesEquals(expectedDish, getPersistedDish(expectedDish));
    }

    protected void assertPersistedDishToMatchUpdatableProperties(Dish expectedDish) {
        assertDishAllUpdatablePropertiesEquals(expectedDish, getPersistedDish(expectedDish));
    }
}
