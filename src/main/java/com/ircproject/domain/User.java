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

    // 조각난 메시지를 모아둘 버퍼
    private final StringBuilder buffer = new StringBuilder();

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

    // 들어온 데이터를 버퍼에 쌓기
    public void appendData(String data) {
        buffer.append(data);
    }

    // 완성된 라인(\r\n 또는 \n)이 있는지 확인하고 꺼내기
    public String nextLine() {
        String content = buffer.toString();
        // 윈도우(\r\n)와 유닉스(\n) 줄바꿈 모두 처리
        int eolIndex = content.indexOf("\n");

        if (eolIndex == -1) {
            return null; // 아직 완전한 문장이 도착하지 않음
        }

        // 한 줄을 잘라냄(개행문자 포함해서 제거)
        String line = content.substring(0, eolIndex).trim(); // \r 처리를 위해 trim 사용

        // 처리한 부분은 버퍼에서 삭제
        buffer.delete(0, eolIndex + 1);

        return line;
    }
}
