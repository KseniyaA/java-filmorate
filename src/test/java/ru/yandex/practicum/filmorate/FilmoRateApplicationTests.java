package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.impl.UserDbStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmoRateApplicationTests {
	private final UserDbStorage userStorage;
	private final FilmDbStorage filmStorage;

	@Test
	public void testCreateAndUpdateUser() {
		User user = User.builder()
				.login("login")
				.birthday(LocalDate.of(1987, 01, 01))
				.email("mail@ya.ru")
				.name("name")
				.build();

		User createdUser = userStorage.create(user);
		Optional<User> userOptional = Optional.ofNullable(createdUser);

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(u -> assertThat(u).hasFieldOrProperty("id"))
				.hasValueSatisfying(u -> assertThat(u).hasFieldOrPropertyWithValue("name", "name"))
				.hasValueSatisfying(u -> assertThat(u).hasFieldOrPropertyWithValue("email", "mail@ya.ru"))
				.hasValueSatisfying(u -> assertThat(u).hasFieldOrPropertyWithValue("birthday", LocalDate.of(1987, 01, 01)));

		user = User.builder()
				.login("updatedLogin")
				.birthday(LocalDate.of(1987, 01, 01))
				.email("newMail@ya.ru")
				.name("updated name")
				.id(createdUser.getId())
				.build();

		userOptional = Optional.ofNullable(userStorage.update(user));

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(u -> assertThat(u).hasFieldOrProperty("id"))
				.hasValueSatisfying(u -> assertThat(u).hasFieldOrPropertyWithValue("name", "updated name"))
				.hasValueSatisfying(u -> assertThat(u).hasFieldOrPropertyWithValue("email", "newMail@ya.ru"))
				.hasValueSatisfying(u -> assertThat(u).hasFieldOrPropertyWithValue("login", "updatedLogin"))
				.hasValueSatisfying(u -> assertThat(u).hasFieldOrPropertyWithValue("birthday", LocalDate.of(1987, 01, 01)));
	}

	@Test
	public void testFindUserById() {
		User user = User.builder()
				.login("login")
				.birthday(LocalDate.of(1987, 01, 01))
				.email("mail@ya.ru")
				.name("name")
				.build();

		User createdUser = userStorage.create(user);

		Optional<User> userOptional = Optional.ofNullable(userStorage.get(createdUser.getId()));

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(u ->
						assertThat(u).hasFieldOrPropertyWithValue("id", createdUser.getId())
				);
	}

	@Test
	public void testDeleteUserById() {
		User user = User.builder()
				.login("login")
				.birthday(LocalDate.of(1987, 01, 01))
				.email("mail@ya.ru")
				.name("name")
				.build();

		User createdUser = userStorage.create(user);
		int createdUserId = createdUser.getId();

		userStorage.delete(createdUser);

		assertThrows(UserNotFoundException.class, () -> userStorage.get(createdUserId));
	}

	@Test
	public void testAddAndGetAndRemoveFriend() {
		User user1 = User.builder()
				.login("login")
				.birthday(LocalDate.of(1987, 01, 01))
				.email("mail@ya.ru")
				.name("name")
				.build();
		User createdUser1 = userStorage.create(user1);
		User user2 = User.builder()
				.login("login2")
				.birthday(LocalDate.of(1987, 01, 01))
				.email("mail2@ya.ru")
				.name("name2")
				.build();
		User createdUser2 = userStorage.create(user2);

		userStorage.addFriend(createdUser1.getId(), createdUser2.getId());

		List<User> userFriends = userStorage.getUserFriends(createdUser1.getId());
		assertEquals(1, userFriends.size());

		userStorage.removeFriend(createdUser1.getId(), createdUser2.getId());
		userFriends = userStorage.getUserFriends(createdUser1.getId());
		assertEquals(0, userFriends.size());
	}

	@Test
	public void testFindAllUsers() {
		User user1 = User.builder()
				.login("login")
				.birthday(LocalDate.of(1987, 01, 01))
				.email("mail@ya.ru")
				.name("name")
				.build();
		User createdUser1 = userStorage.create(user1);
		User user2 = User.builder()
				.login("login2")
				.birthday(LocalDate.of(1987, 01, 01))
				.email("mail2@ya.ru")
				.name("name2")
				.build();
		User createdUser2 = userStorage.create(user2);

		List<User> all = userStorage.findAll();
		assertEquals(2, all.size());
	}

	@Test
	public void testGetCommonFriend() {
		User user1 = User.builder()
				.login("login")
				.birthday(LocalDate.of(1987, 01, 01))
				.email("mail@ya.ru")
				.name("name")
				.build();
		User createdUser1 = userStorage.create(user1);

		User user2 = User.builder()
				.login("login2")
				.birthday(LocalDate.of(1987, 01, 01))
				.email("mail2@ya.ru")
				.name("name2")
				.build();
		User createdUser2 = userStorage.create(user2);

		User user3 = User.builder()
				.login("login3")
				.birthday(LocalDate.of(1987, 01, 01))
				.email("mail3@ya.ru")
				.name("name3")
				.build();
		User createdUser3 = userStorage.create(user3);

		User user4 = User.builder()
				.login("login4")
				.birthday(LocalDate.of(1987, 01, 01))
				.email("mail4@ya.ru")
				.name("name4")
				.build();
		User createdUser4 = userStorage.create(user4);

		userStorage.addFriend(user1.getId(), user3.getId());
		userStorage.addFriend(user1.getId(), user4.getId());
		userStorage.addFriend(user2.getId(), user4.getId());

		List<User> common = userStorage.getCommonFriends(user1.getId(), user2.getId());
		assertEquals(1, common.size());
		assertEquals(createdUser4.getId(), common.get(0).getId());
	}

	@Test
	public void testGetAllGenres() {
		List<Genre> allGenres = filmStorage.findAllGenres();
		for (Genre genre: allGenres) {
			Optional<Genre> optionalGenre = Optional.ofNullable(genre);
			assertThat(optionalGenre)
					.hasValueSatisfying(g -> assertThat(g).hasFieldOrProperty("id"))
					.hasValueSatisfying(g -> assertThat(g).hasFieldOrProperty("name"));
		}

		List<String> expectedValues = Arrays.asList("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");

		assertTrue(allGenres.stream().map(Genre::getName).collect(Collectors.toSet()).containsAll(expectedValues));
	}

	@Test
	public void testGetAllMpa() {
		List<Mpa> allMpa = filmStorage.findAllMpa();
		for (Mpa mpa: allMpa) {
			Optional<Mpa> optionalGenre = Optional.ofNullable(mpa);
			assertThat(optionalGenre)
					.hasValueSatisfying(g -> assertThat(g).hasFieldOrProperty("id"))
					.hasValueSatisfying(g -> assertThat(g).hasFieldOrProperty("name"));
		}

		List<String> expectedValues = Arrays.asList("G", "PG", "PG-13", "R", "NC-17");

		assertTrue(allMpa.stream().map(Mpa::getName).collect(Collectors.toSet()).containsAll(expectedValues));
	}

	@Test
	public void testCreateAndUpdateAndGetFilm() {
		Film film = Film.builder()
				.name("name")
				.description("description")
				.rate(1)
				.duration(100)
				.releaseDate(LocalDate.of(2020, 01, 01))
				.mpa(Mpa.builder().id(1).build())
				.build();
		Film createdFilm = filmStorage.create(film);
		int createdFilmId = createdFilm.getId();

		Optional<Film> filmOptional = Optional.ofNullable(createdFilm);

		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(u -> assertThat(u).hasFieldOrProperty("id"))
				.hasValueSatisfying(u -> assertThat(u).hasFieldOrPropertyWithValue("name", "name"))
				.hasValueSatisfying(u -> assertThat(u).hasFieldOrPropertyWithValue("rate", 1))
				.hasValueSatisfying(u -> assertThat(u).hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(2020, 01, 01)))
				.hasValueSatisfying(u -> assertThat(u).hasFieldOrPropertyWithValue("description", "description"))
				.hasValueSatisfying(u -> assertThat(u).hasFieldOrPropertyWithValue("duration", 100));

		Film updated = Film.builder()
				.id(createdFilmId)
				.name("updated name")
				.description("updated description")
				.rate(1)
				.duration(200)
				.mpa(Mpa.builder().id(2).build())
				.releaseDate(LocalDate.of(2020, 01, 01))
				.build();
		Film updatedFilm = filmStorage.update(updated);
		Optional<Film> filmUpdatedOptional = Optional.ofNullable(updatedFilm);
		assertThat(filmUpdatedOptional)
				.isPresent()
				.hasValueSatisfying(u -> assertThat(u).hasFieldOrProperty("id"))
				.hasValueSatisfying(u -> assertThat(u).hasFieldOrPropertyWithValue("name", "updated name"))
				.hasValueSatisfying(u -> assertThat(u).hasFieldOrPropertyWithValue("rate", 1))
				.hasValueSatisfying(u -> assertThat(u).hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(2020, 01, 01)))
				.hasValueSatisfying(u -> assertThat(u).hasFieldOrPropertyWithValue("description", "updated description"))
				.hasValueSatisfying(u -> assertThat(u).hasFieldOrPropertyWithValue("duration", 200));
	}

	@Test
	public void testDeleteFilm() {
		Film film = Film.builder()
				.name("name")
				.description("description")
				.rate(1)
				.duration(100)
				.releaseDate(LocalDate.of(2020, 01, 01))
				.mpa(Mpa.builder().id(1).build())
				.build();
		Film createdFilm = filmStorage.create(film);
		int createdFilmId = createdFilm.getId();

		filmStorage.delete(createdFilm);

		assertThrows(FilmNotFoundException.class, () -> filmStorage.get(createdFilmId));
	}

	@Test
	public void testLikeAndDislikeFilm() {
		User user1 = User.builder()
				.login("login")
				.birthday(LocalDate.of(1987, 01, 01))
				.email("mail@ya.ru")
				.name("name")
				.build();
		User createdUser1 = userStorage.create(user1);
		Film film1 = Film.builder()
				.name("name1")
				.description("description1")
				.rate(1)
				.duration(100)
				.releaseDate(LocalDate.of(2020, 01, 01))
				.mpa(Mpa.builder().id(1).build())
				.build();
		filmStorage.create(film1);
		Film film2 = Film.builder()
				.name("name1")
				.description("description1")
				.rate(1)
				.duration(100)
				.releaseDate(LocalDate.of(2020, 01, 01))
				.mpa(Mpa.builder().id(1).build())
				.build();
		filmStorage.create(film2);
		assertEquals(0, filmStorage.get(film1.getId()).getLikes().size());

		filmStorage.likeFilm(film1, createdUser1);
		filmStorage.likeFilm(film2, createdUser1);

		Set<Integer> likesFilm1 = filmStorage.get(film1.getId()).getLikes();
		assertEquals(1, likesFilm1.size());
		assertTrue(likesFilm1.contains(createdUser1.getId()));

		Set<Integer> likesFilm2 = filmStorage.get(film2.getId()).getLikes();
		assertEquals(1, likesFilm2.size());
		assertTrue(likesFilm2.contains(createdUser1.getId()));

		filmStorage.dislikeFilm(film1, createdUser1);
		likesFilm1 = filmStorage.get(film1.getId()).getLikes();
		assertEquals(0, likesFilm1.size());
	}

	@Test
	public void testGetAndUpdateGenre() {
		Genre genre1 = Genre.builder().id(1).build();
		Genre genre2 = Genre.builder().id(2).build();

		Set<Genre> genres = new HashSet<>();
		genres.add(genre1);
		genres.add(genre2);

		Film film1 = Film.builder()
				.name("name1")
				.description("description1")
				.rate(1)
				.duration(100)
				.releaseDate(LocalDate.of(2020, 01, 01))
				.mpa(Mpa.builder().id(1).build())
				.genres(genres)
				.build();
		Film createdFilm = filmStorage.create(film1);
		assertEquals(2, createdFilm.getGenres().size());

		film1 = Film.builder()
				.id(createdFilm.getId())
				.name("name1")
				.description("description1")
				.rate(1)
				.duration(100)
				.releaseDate(LocalDate.of(2020, 01, 01))
				.mpa(Mpa.builder().id(1).build())
				.genres(new HashSet<>())
				.build();
		Film updatedFilm = filmStorage.update(film1);
		assertEquals(0, updatedFilm.getGenres().size());
	}
}
