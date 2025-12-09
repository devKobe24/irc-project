package com.ircproject.handler.commands;

import com.ircproject.domain.IrcMessage;
import com.ircproject.domain.User;
import com.ircproject.handler.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * packageName    : com.ircproject.handler.commands
 * fileName       : PongHandler
 * author         : kobe
 * date           : 2025. 12. 10.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 10.        kobe       최초 생성
 */
@Component
public class PongHandler implements CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(PongHandler.class);

    @Override
    public String getCommand() {
        return "PONG";
    }

    @Override
    public void handle(User user, IrcMessage message) throws IOException {
        // 서버가 보낸 PING에 대해 클라이언트가 응답했을 때 호출됨
        // 나중에 "Last Active Time" 등을 갱신하는 용도로 사용 가능
        logger.info("Heartbeat received from {}: PONG", user.getNickname());
    }
}
