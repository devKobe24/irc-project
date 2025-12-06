package com.ircproject.domain;

import java.util.List;

/**
 * packageName    : com.ircproject.domain
 * fileName       : IrcMessage
 * author         : kobe
 * date           : 2025. 12. 5.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 5.        kobe       최초 생성
 */

// Java 21 Record: 불변(Immutable) 데이터 객체를 아주 간결하게 정의
public record IrcMessage(
        String prefix,      // 예: ":dave!user@host" (없으면 null)
        String command,     // 예: "PRIVMSG"
        List<String> parameters // 예: ["#channel", "Hello World"]
) {
    // 생성자에서 parameters가 null이면 빈 리스트로 초기화하여 NPE 방지
    public IrcMessage {
        if (parameters == null) {
            parameters = List.of();
        }
    }
}
