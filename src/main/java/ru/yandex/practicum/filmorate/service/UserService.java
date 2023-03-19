package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("InMemoryUserStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public void delete(User user) {
        userStorage.delete(user);
    }

    public User get(int id) {
        return userStorage.get(id);
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    // добавление в друзья
    public void addFriend(User user, User friend) {
        user.getFriends().add(friend.getId());
        friend.getFriends().add(user.getId());
    }

    // удаление из друзей
    public void removeFriend(User user, User friend) {
        user.getFriends().remove(friend.getId());
        friend.getFriends().remove(user.getId());
    }

    // вывод списка общих друзей
    public Set<Integer> getCommonFriends(User user1, User user2) {
        Set<Integer> commonFriends = new HashSet<>(user1.getFriends());
        commonFriends.retainAll(user2.getFriends());
        return commonFriends;
    }

    public List<User> getUserFriends(User user) {
        return user.getFriends().stream().map(this::get).collect(Collectors.toList());
    }
}
