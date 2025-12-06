package com.ircproject.handler;

import com.ircproject.domain.IrcMessage;
import com.ircproject.domain.User;

import java.io.IOException;

/**
 * packageName    : com.ircproject.handler
 * fileName       : CommandHandler
 * author         : kobe
 * date           : 2025. 12. 7.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 7.        kobe       최초 생성
 */
public interface CommandHandler {
    /**
     * @return 처리할 명령어 (예: "NICK", "JOIN")
     */
    String getCommand();

    /**
     * 실제 로직을 수행하는 메서드
     */
    void handle(User user, IrcMessage message) throws IOException;
}