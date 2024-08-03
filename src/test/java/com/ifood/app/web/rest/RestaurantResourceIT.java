package com.ifood.app.web.rest;

import static com.ifood.app.domain.RestaurantAsserts.*;
import static com.ifood.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifood.app.IntegrationTest;
import com.ifood.app.domain.Restaurant;
import com.ifood.app.repository.RestaurantRepository;
import com.ifood.app.service.dto.RestaurantDTO;
import com.ifood.app.service.mapper.RestaurantMapper;
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
 * Integration tests for the {@link RestaurantResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class RestaurantResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_LOCATION = "AAAAAAAAAA";
    private static final String UPDATED_LOCATION = "BBBBBBBBBB";

    private static final Double DEFAULT_RATING = 0D;
    private static final Double UPDATED_RATING = 1D;

    private static final String ENTITY_API_URL = "/api/restaurants";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private RestaurantMapper restaurantMapper;

    @Autowired
    private WebTestClient webTestClient;

    private Restaurant restaurant;

    private Restaurant insertedRestaurant;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Restaurant createEntity() {
        Restaurant restaurant = new Restaurant().name(DEFAULT_NAME).location(DEFAULT_LOCATION).rating(DEFAULT_RATING);
        return restaurant;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Restaurant createUpdatedEntity() {
        Restaurant restaurant = new Restaurant().name(UPDATED_NAME).location(UPDATED_LOCATION).rating(UPDATED_RATING);
        return restaurant;
    }

    @BeforeEach
    public void initTest() {
        restaurant = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedRestaurant != null) {
            restaurantRepository.delete(insertedRestaurant).block();
            insertedRestaurant = null;
        }
    }

    @Test
    void createRestaurant() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Restaurant
        RestaurantDTO restaurantDTO = restaurantMapper.toDto(restaurant);
        var returnedRestaurantDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(restaurantDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(RestaurantDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the Restaurant in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedRestaurant = restaurantMapper.toEntity(returnedRestaurantDTO);
        assertRestaurantUpdatableFieldsEquals(returnedRestaurant, getPersistedRestaurant(returnedRestaurant));

        insertedRestaurant = returnedRestaurant;
    }

    @Test
    void createRestaurantWithExistingId() throws Exception {
        // Create the Restaurant with an existing ID
        restaurant.setId(1L);
        RestaurantDTO restaurantDTO = restaurantMapper.toDto(restaurant);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(restaurantDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Restaurant in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        restaurant.setName(null);

        // Create the Restaurant, which fails.
        RestaurantDTO restaurantDTO = restaurantMapper.toDto(restaurant);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(restaurantDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllRestaurants() {
        // Initialize the database
        insertedRestaurant = restaurantRepository.save(restaurant).block();

        // Get all the restaurantList
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
            .jsonPath("$.[*].location")
            .value(hasItem(DEFAULT_LOCATION))
            .jsonPath("$.[*].rating")
            .value(hasItem(DEFAULT_RATING.doubleValue()));
    }

    @Test
    void getRestaurant() {
        // Initialize the database
        insertedRestaurant = restaurantRepository.save(restaurant).block();

        // Get the restaurant
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, restaurant.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME))
            .jsonPath("$.location")
            .value(is(DEFAULT_LOCATION))
            .jsonPath("$.rating")
            .value(is(DEFAULT_RATING.doubleValue()));
    }

    @Test
    void getNonExistingRestaurant() {
        // Get the restaurant
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingRestaurant() throws Exception {
        // Initialize the database
        insertedRestaurant = restaurantRepository.save(restaurant).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the restaurant
        Restaurant updatedRestaurant = restaurantRepository.findById(restaurant.getId()).block();
        updatedRestaurant.name(UPDATED_NAME).location(UPDATED_LOCATION).rating(UPDATED_RATING);
        RestaurantDTO restaurantDTO = restaurantMapper.toDto(updatedRestaurant);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, restaurantDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(restaurantDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Restaurant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedRestaurantToMatchAllProperties(updatedRestaurant);
    }

    @Test
    void putNonExistingRestaurant() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        restaurant.setId(longCount.incrementAndGet());

        // Create the Restaurant
        RestaurantDTO restaurantDTO = restaurantMapper.toDto(restaurant);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, restaurantDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(restaurantDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Restaurant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchRestaurant() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        restaurant.setId(longCount.incrementAndGet());

        // Create the Restaurant
        RestaurantDTO restaurantDTO = restaurantMapper.toDto(restaurant);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(restaurantDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Restaurant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamRestaurant() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        restaurant.setId(longCount.incrementAndGet());

        // Create the Restaurant
        RestaurantDTO restaurantDTO = restaurantMapper.toDto(restaurant);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(restaurantDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Restaurant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateRestaurantWithPatch() throws Exception {
        // Initialize the database
        insertedRestaurant = restaurantRepository.save(restaurant).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the restaurant using partial update
        Restaurant partialUpdatedRestaurant = new Restaurant();
        partialUpdatedRestaurant.setId(restaurant.getId());

        partialUpdatedRestaurant.rating(UPDATED_RATING);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedRestaurant.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedRestaurant))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Restaurant in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertRestaurantUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedRestaurant, restaurant),
            getPersistedRestaurant(restaurant)
        );
    }

    @Test
    void fullUpdateRestaurantWithPatch() throws Exception {
        // Initialize the database
        insertedRestaurant = restaurantRepository.save(restaurant).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the restaurant using partial update
        Restaurant partialUpdatedRestaurant = new Restaurant();
        partialUpdatedRestaurant.setId(restaurant.getId());

        partialUpdatedRestaurant.name(UPDATED_NAME).location(UPDATED_LOCATION).rating(UPDATED_RATING);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedRestaurant.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedRestaurant))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Restaurant in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertRestaurantUpdatableFieldsEquals(partialUpdatedRestaurant, getPersistedRestaurant(partialUpdatedRestaurant));
    }

    @Test
    void patchNonExistingRestaurant() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        restaurant.setId(longCount.incrementAndGet());

        // Create the Restaurant
        RestaurantDTO restaurantDTO = restaurantMapper.toDto(restaurant);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, restaurantDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(restaurantDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Restaurant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchRestaurant() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        restaurant.setId(longCount.incrementAndGet());

        // Create the Restaurant
        RestaurantDTO restaurantDTO = restaurantMapper.toDto(restaurant);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(restaurantDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Restaurant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamRestaurant() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        restaurant.setId(longCount.incrementAndGet());

        // Create the Restaurant
        RestaurantDTO restaurantDTO = restaurantMapper.toDto(restaurant);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(restaurantDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Restaurant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteRestaurant() {
        // Initialize the database
        insertedRestaurant = restaurantRepository.save(restaurant).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the restaurant
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, restaurant.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return restaurantRepository.count().block();
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

    protected Restaurant getPersistedRestaurant(Restaurant restaurant) {
        return restaurantRepository.findById(restaurant.getId()).block();
    }

    protected void assertPersistedRestaurantToMatchAllProperties(Restaurant expectedRestaurant) {
        assertRestaurantAllPropertiesEquals(expectedRestaurant, getPersistedRestaurant(expectedRestaurant));
    }

    protected void assertPersistedRestaurantToMatchUpdatableProperties(Restaurant expectedRestaurant) {
        assertRestaurantAllUpdatablePropertiesEquals(expectedRestaurant, getPersistedRestaurant(expectedRestaurant));
    }
}
