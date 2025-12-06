package com.ircproject.domain;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * packageName    : com.ircproject.domain
 * fileName       : Channel
 * author         : kobe
 * date           : 2025. 12. 6.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 6.        kobe       최초 생성
 */
public class Channel {
    private final String name;

    // 동시성(Concurrency) 문제 방지를 위해 Thread-Safe한 Set 사용
    private final Set<User> users = ConcurrentHashMap.newKeySet();

    public Channel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void join(User user) {
        users.add(user);
    }

    public void leave(User user) {
        users.remove(user);
    }

    // 외부에서 리스트를 수정하지 못하도록 unmodifiable Set 반환 (방어적 복사)
    public Set<User> getUsers() {
        return Collections.unmodifiableSet(users);
    }

    public boolean isEmpty() {
        return users.isEmpty();
    }
}