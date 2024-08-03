package com.ifood.app.service;

import com.ifood.app.repository.MenuRepository;
import com.ifood.app.service.dto.MenuDTO;
import com.ifood.app.service.mapper.MenuMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.ifood.app.domain.Menu}.
 */
@Service
public class MenuService {

    private static final Logger log = LoggerFactory.getLogger(MenuService.class);

    private final MenuRepository menuRepository;

    private final MenuMapper menuMapper;

    public MenuService(MenuRepository menuRepository, MenuMapper menuMapper) {
        this.menuRepository = menuRepository;
        this.menuMapper = menuMapper;
    }

    /**
     * Save a menu.
     *
     * @param menuDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<MenuDTO> save(MenuDTO menuDTO) {
        log.debug("Request to save Menu : {}", menuDTO);
        return menuRepository.save(menuMapper.toEntity(menuDTO)).map(menuMapper::toDto);
    }

    /**
     * Update a menu.
     *
     * @param menuDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<MenuDTO> update(MenuDTO menuDTO) {
        log.debug("Request to update Menu : {}", menuDTO);
        return menuRepository.save(menuMapper.toEntity(menuDTO)).map(menuMapper::toDto);
    }

    /**
     * Partially update a menu.
     *
     * @param menuDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<MenuDTO> partialUpdate(MenuDTO menuDTO) {
        log.debug("Request to partially update Menu : {}", menuDTO);

        return menuRepository
            .findById(menuDTO.getId())
            .map(existingMenu -> {
                menuMapper.partialUpdate(existingMenu, menuDTO);

                return existingMenu;
            })
            .flatMap(menuRepository::save)
            .map(menuMapper::toDto);
    }

    /**
     * Get all the menus.
     *
     * @return the list of entities.
     */
    public Flux<MenuDTO> findAll() {
        log.debug("Request to get all Menus");
        return menuRepository.findAll().map(menuMapper::toDto);
    }

    /**
     * Returns the number of menus available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return menuRepository.count();
    }

    /**
     * Get one menu by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Mono<MenuDTO> findOne(Long id) {
        log.debug("Request to get Menu : {}", id);
        return menuRepository.findById(id).map(menuMapper::toDto);
    }

    /**
     * Delete the menu by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete Menu : {}", id);
        return menuRepository.deleteById(id);
    }
}
