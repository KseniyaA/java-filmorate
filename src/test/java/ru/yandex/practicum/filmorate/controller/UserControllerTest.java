package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private final UserController userController = new UserController(new UserService(new InMemoryUserStorage()));

    @Test
    void create() {
        User user = User.builder()
                .email("email@ya.ru")
                .login("userLogin")
                .birthday(LocalDate.of(2000, 01, 01))
                .name("userName")
                .build();
        User createdUser = userController.create(user);
        assertTrue(createdUser.getId() > 0);
        assertEquals("email@ya.ru", createdUser.getEmail());
        assertEquals("userLogin", createdUser.getLogin());
        assertEquals("userName", createdUser.getName());
        assertEquals(LocalDate.of(2000, 01, 01), createdUser.getBirthday());
    }

    @Test
    void createWithNullEmail() {
        final NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> {
                    User user = User.builder()
                            .login("userLogin")
                            .birthday(LocalDate.of(2000, 01, 01))
                            .name("userName")
                            .build();
                });
    }

    @Test
    void createWithEmptyEmail() {
        User user = User.builder()
                .login("userLogin")
                .birthday(LocalDate.of(2000, 01, 01))
                .name("userName")
                .email("")
                .build();

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> {
                    userController.create(user);
                });
    }

    @Test
    void createWithIncorrectEmail() {
        User user = User.builder()
                .login("userLogin")
                .birthday(LocalDate.of(2000, 01, 01))
                .name("userName")
                .email("ya.ru")
                .build();

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> {
                    userController.create(user);
                });
    }

    @Test
    void createWithNullLogin() {
        final NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> {
                    User user = User.builder()
                            .birthday(LocalDate.of(2000, 01, 01))
                            .email("mail@ya.ru")
                            .build();
                });
    }

    @Test
    void createWithEmptyLogin() {
        User user = User.builder()
                .login("userLogin")
                .birthday(LocalDate.of(2000, 01, 01))
                .email("mail@ya.ru")
                .login("")
                .build();

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> {

                    userController.create(user);
                });
    }

    @Test
    void createWithSpacesLogin() {
        User user = User.builder()
                .login("userLogin")
                .birthday(LocalDate.of(2000, 01, 01))
                .email("mail@ya.ru")
                .login("a a")
                .build();

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> {

                    userController.create(user);
                });
    }

    @Test
    void createWithEmptyName() {
        User user = User.builder()
                .login("userLogin")
                .birthday(LocalDate.of(2000, 01, 01))
                .email("mail@ya.ru")
                .build();

        User createdUser = userController.create(user);
        assertEquals(createdUser.getName(), user.getLogin());
    }

    @Test
    void createWithInvalidBirthday() {
        User user = User.builder()
                .login("userLogin")
                .birthday(LocalDate.now().plusDays(1))
                .email("mail@ya.ru")
                .build();

        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> {
                    userController.create(user);
                });
    }

    @Test
    void update() {
        User user = User.builder()
                .email("email@ya.ru")
                .login("userLogin")
                .birthday(LocalDate.of(2000, 01, 01))
                .name("userName")
                .build();
        User createdUser = userController.create(user);

        User updateUser = User.builder()
                .id(createdUser.getId())
                .email("email@ya.ru")
                .login("userLogin1")
                .birthday(LocalDate.of(2000, 01, 01))
                .name("userName1")
                .build();
        User updatedUser = userController.update(updateUser);

        assertEquals(1, userController.findAll().size());
        assertEquals("email@ya.ru", updatedUser.getEmail());
        assertEquals("userLogin1", updatedUser.getLogin());
        assertEquals(LocalDate.of(2000, 01, 01), updatedUser.getBirthday());
        assertEquals("userName1", updateUser.getName());
    }

    @Test
    void updateUnExistedUser() {
        User user = User.builder()
                .email("email@ya.ru")
                .login("userLogin")
                .birthday(LocalDate.of(2000, 01, 01))
                .name("userName")
                .build();
        User createdUser = userController.create(user);

        User updateUser = User.builder()
                .id(999)
                .email("email@ya.ru")
                .login("userLogin1")
                .birthday(LocalDate.of(2000, 01, 01))
                .name("userName1")
                .build();

        final UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> {
                    userController.update(updateUser);
                });
        assertEquals("Пользователь с id = 999 не найден", exception.getMessage());
    }

    @Test
    void getAllUsers() {
        User user1 = User.builder()
                .email("email@ya.ru")
                .login("userLogin")
                .birthday(LocalDate.of(2000, 01, 01))
                .name("userName")
                .build();
        User createdUser1 = userController.create(user1);

        User user2 = User.builder()
                .email("email1@ya.ru")
                .login("userLogin2")
                .birthday(LocalDate.of(2000, 01, 01))
                .name("userName2")
                .build();
        User createdUser2 = userController.create(user2);

        assertEquals(2, userController.findAll().size());
    }
}