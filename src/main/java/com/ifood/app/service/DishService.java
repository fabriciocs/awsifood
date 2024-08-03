package com.ifood.app.service;

import com.ifood.app.repository.DishRepository;
import com.ifood.app.service.dto.DishDTO;
import com.ifood.app.service.mapper.DishMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.ifood.app.domain.Dish}.
 */
@Service
public class DishService {

    private static final Logger log = LoggerFactory.getLogger(DishService.class);

    private final DishRepository dishRepository;

    private final DishMapper dishMapper;

    public DishService(DishRepository dishRepository, DishMapper dishMapper) {
        this.dishRepository = dishRepository;
        this.dishMapper = dishMapper;
    }

    /**
     * Save a dish.
     *
     * @param dishDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<DishDTO> save(DishDTO dishDTO) {
        log.debug("Request to save Dish : {}", dishDTO);
        return dishRepository.save(dishMapper.toEntity(dishDTO)).map(dishMapper::toDto);
    }

    /**
     * Update a dish.
     *
     * @param dishDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<DishDTO> update(DishDTO dishDTO) {
        log.debug("Request to update Dish : {}", dishDTO);
        return dishRepository.save(dishMapper.toEntity(dishDTO)).map(dishMapper::toDto);
    }

    /**
     * Partially update a dish.
     *
     * @param dishDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<DishDTO> partialUpdate(DishDTO dishDTO) {
        log.debug("Request to partially update Dish : {}", dishDTO);

        return dishRepository
            .findById(dishDTO.getId())
            .map(existingDish -> {
                dishMapper.partialUpdate(existingDish, dishDTO);

                return existingDish;
            })
            .flatMap(dishRepository::save)
            .map(dishMapper::toDto);
    }

    /**
     * Get all the dishes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    public Flux<DishDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Dishes");
        return dishRepository.findAllBy(pageable).map(dishMapper::toDto);
    }

    /**
     * Returns the number of dishes available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return dishRepository.count();
    }

    /**
     * Get one dish by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Mono<DishDTO> findOne(Long id) {
        log.debug("Request to get Dish : {}", id);
        return dishRepository.findById(id).map(dishMapper::toDto);
    }

    /**
     * Delete the dish by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete Dish : {}", id);
        return dishRepository.deleteById(id);
    }
}
