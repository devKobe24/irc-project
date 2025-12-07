package com.ircproject.repository;

import com.ircproject.domain.User;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * packageName    : com.ircproject.repository
 * fileName       : UserRepository
 * author         : kobe
 * date           : 2025. 12. 7.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 7.        kobe       최초 생성
 */
@Component
public class UserRepository {
    // Key: 닉네임, Value: User 객체
    private final Map<String, User> usersByNickname = new ConcurrentHashMap<>();

    public void save(User user) {
        usersByNickname.put(user.getNickname(), user);
    }

    public void remove(String nickname) {
        usersByNickname.remove(nickname);
    }

    public User findByNickname(String nickname) {
        return usersByNickname.get(nickname);
    }

    public boolean exists(String nickname) {
        return usersByNickname.containsKey(nickname);
    }
}