package com.ircproject.domain;

import java.nio.channels.SocketChannel;
import java.util.Objects;

/**
 * packageName    : com.ircproject.domain
 * fileName       : User
 * author         : kobe
 * date           : 2025. 12. 5.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 5.        kobe       최초 생성
 */
public class User {
    private String nickname;
    private final SocketChannel socketChannel;

    // 생성 시점에는 닉네임을 아직 모를 수 있으므로(연결 직후), 초기값은 *로 설정하거나 null 처리
    public User(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        this.nickname = "*"; // 아직 NICK 명령어를 보내기 전 상태
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    // Set<User> 같은 컬렉션에 담을 때 중복 제거를 위해 equals/hashCode 필수
    // 여기서는 'SocketChannel'이 유니크한 식별자 역할을 합니다. (닉네임은 변경 가능하므로 식별자로 부적합)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(socketChannel, user.socketChannel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(socketChannel);
    }
}
