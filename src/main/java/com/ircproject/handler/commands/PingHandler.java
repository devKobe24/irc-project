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
 * fileName       : PingHandler
 * author         : kobe
 * date           : 2025. 12. 10.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 10.        kobe       최초 생성
 */
@Component
public class PingHandler implements CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(PingHandler.class);

    @Override
    public String getCommand() {
        return "PING";
    }

    @Override
    public void handle(User user, IrcMessage message) throws IOException {
        // PING 메시지에는 보통 파라미터(TOKEN)가 하나 따라옵니다.
        // 예: PING: 1733812345

        if (message.parameters().isEmpty()) {
            // 파라미터가 없으면 그냥 서버 이름 등으로 응답
            user.sendMessage("PONG :irc-server\r\n");
            return;
        }

        String token = message.parameters().get(0);

        // RFC 표준: 받은 토큰을 그대로 PONG 뒤에 붙여서 돌려줘야 함
        // 형식 PONG <TOKEN>
        String response = "PONG :" + token + "\r\n";

        user.sendMessage(response);

        logger.debug("PING received from {}, replied with PONG", user.getNickname());
    }
}
