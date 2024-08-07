package com.ifood.app.service;

import com.ifood.app.config.Constants;
import com.ifood.app.domain.User;
import com.ifood.app.repository.UserRepository;
import com.ifood.app.security.AuthoritiesConstants;
import com.ifood.app.security.SecurityUtils;
import com.ifood.app.service.dto.AdminUserDTO;
import com.ifood.app.service.dto.UserDTO;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tech.jhipster.security.RandomUtil;

/**
 * Service class for managing users.
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return userRepository
            .findOneByActivationKey(key)
            .flatMap(user -> {
                // activate given user for the registration key.
                user.setActivated(true);
                user.setActivationKey(null);
                return saveUser(user);
            })
            .doOnNext(user -> log.debug("Activated user: {}", user));
    }

    public Mono<User> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);
        return userRepository
            .findOneByResetKey(key)
            .filter(user -> user.getResetDate().isAfter(Instant.now().minus(1, ChronoUnit.DAYS)))
            .publishOn(Schedulers.boundedElastic())
            .map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setResetDate(null);
                return user;
            })
            .flatMap(this::saveUser);
    }

    public Mono<User> requestPasswordReset(String mail) {
        return userRepository
            .findOneByEmailIgnoreCase(mail)
            .filter(User::isActivated)
            .publishOn(Schedulers.boundedElastic())
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(Instant.now());
                return user;
            })
            .flatMap(this::saveUser);
    }

    public Mono<User> registerUser(AdminUserDTO userDTO, String password) {
        return userRepository
            .findOneByLogin(userDTO.getLogin().toLowerCase())
            .flatMap(existingUser -> {
                if (!existingUser.isActivated()) {
                    return userRepository.delete(existingUser);
                } else {
                    return Mono.error(new UsernameAlreadyUsedException());
                }
            })
            .then(userRepository.findOneByEmailIgnoreCase(userDTO.getEmail()))
            .flatMap(existingUser -> {
                if (!existingUser.isActivated()) {
                    return userRepository.delete(existingUser);
                } else {
                    return Mono.error(new EmailAlreadyUsedException());
                }
            })
            .publishOn(Schedulers.boundedElastic())
            .then(
                Mono.fromCallable(() -> {
                    User newUser = new User();
                    String encryptedPassword = passwordEncoder.encode(password);
                    newUser.setLogin(userDTO.getLogin().toLowerCase());
                    // new user gets initially a generated password
                    newUser.setPassword(encryptedPassword);
                    newUser.setFirstName(userDTO.getFirstName());
                    newUser.setLastName(userDTO.getLastName());
                    if (userDTO.getEmail() != null) {
                        newUser.setEmail(userDTO.getEmail().toLowerCase());
                    }
                    newUser.setLangKey(userDTO.getLangKey());
                    // new user is not active
                    newUser.setActivated(false);
                    // new user gets registration key
                    newUser.setActivationKey(RandomUtil.generateActivationKey());
                    return newUser;
                })
            )
            .flatMap(newUser -> {
                Set<String> authorities = new HashSet<>();
                authorities.add(AuthoritiesConstants.USER);
                newUser.setAuthorities(authorities);
                return saveUser(newUser).doOnNext(user -> log.debug("Created Information for User: {}", user));
            });
    }

    public Mono<User> createUser(AdminUserDTO userDTO) {
        User user = new User();
        user.setLogin(userDTO.getLogin().toLowerCase());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail().toLowerCase());
        }
        if (userDTO.getLangKey() == null) {
            user.setLangKey(Constants.DEFAULT_LANGUAGE); // default language
        } else {
            user.setLangKey(userDTO.getLangKey());
        }
        user.setAuthorities(userDTO.getAuthorities());
        return Mono.just(user)
            .publishOn(Schedulers.boundedElastic())
            .map(newUser -> {
                String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
                newUser.setPassword(encryptedPassword);
                newUser.setResetKey(RandomUtil.generateResetKey());
                newUser.setResetDate(Instant.now());
                newUser.setActivated(true);
                return newUser;
            })
            .flatMap(this::saveUser)
            .doOnNext(user1 -> log.debug("Created Information for User: {}", user1));
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update.
     * @return updated user.
     */
    public Mono<AdminUserDTO> updateUser(AdminUserDTO userDTO) {
        return userRepository
            .findById(userDTO.getId())
            .map(user -> {
                user.setLogin(userDTO.getLogin().toLowerCase());
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                if (userDTO.getEmail() != null) {
                    user.setEmail(userDTO.getEmail().toLowerCase());
                }
                user.setActivated(userDTO.isActivated());
                user.setLangKey(userDTO.getLangKey());
                user.setAuthorities(userDTO.getAuthorities());
                return user;
            })
            .flatMap(this::saveUser)
            .doOnNext(user -> log.debug("Changed Information for User: {}", user))
            .map(AdminUserDTO::new);
    }

    public Mono<Void> deleteUser(String login) {
        return userRepository
            .findOneByLogin(login)
            .flatMap(user -> userRepository.delete(user).thenReturn(user))
            .doOnNext(user -> log.debug("Deleted User: {}", user))
            .then();
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     * @return a completed {@link Mono}.
     */
    public Mono<Void> updateUser(String firstName, String lastName, String email, String langKey, String imageUrl) {
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .flatMap(user -> {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                if (email != null) {
                    user.setEmail(email.toLowerCase());
                }
                user.setLangKey(langKey);
                user.setImageUrl(imageUrl);
                return saveUser(user);
            })
            .doOnNext(user -> log.debug("Changed Information for User: {}", user))
            .then();
    }

    private Mono<User> saveUser(User user) {
        return SecurityUtils.getCurrentUserLogin()
            .switchIfEmpty(Mono.just(Constants.SYSTEM))
            .flatMap(login -> {
                if (user.getCreatedBy() == null) {
                    user.setCreatedBy(login);
                }
                user.setLastModifiedBy(login);
                return userRepository.save(user);
            });
    }

    public Mono<Void> changePassword(String currentClearTextPassword, String newPassword) {
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .publishOn(Schedulers.boundedElastic())
            .map(user -> {
                String currentEncryptedPassword = user.getPassword();
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw new InvalidPasswordException();
                }
                String encryptedPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encryptedPassword);
                return user;
            })
            .flatMap(this::saveUser)
            .doOnNext(user -> log.debug("Changed password for User: {}", user))
            .then();
    }

    public Flux<AdminUserDTO> getAllManagedUsers() {
        return userRepository.findAll().map(AdminUserDTO::new);
    }

    public Flux<UserDTO> getAllPublicUsers() {
        return userRepository.findAll().filter(user -> user.isActivated()).map(UserDTO::new);
    }

    public Mono<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneByLogin(login);
    }

    public Mono<User> getUserWithAuthorities() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneByLogin);
    }
}
