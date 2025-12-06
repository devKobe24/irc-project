package com.ircproject.handler.commands;

import com.ircproject.domain.IrcMessage;
import com.ircproject.domain.User;
import com.ircproject.handler.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * packageName    : com.ircproject.handler
 * fileName       : NickHandler
 * author         : kobe
 * date           : 2025. 12. 7.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 7.        kobe       최초 생성
 */
@Component
public class NickHandler implements CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(NickHandler.class);

    @Override
    public String getCommand() {
        return "NICK";
    }

    @Override
    public void handle(User user, IrcMessage message) throws IOException {
        // 유효성 검사: 파라미터가 없으면 무시 (나중에는 에러 메시지 전송 필요)
        if (message.parameters().isEmpty()) {
            return;
        }

        String newNickname = message.parameters().get(0);
        String oldNickname = user.getNickname();

        // 닉네임 변경 로직
        user.setNickname(newNickname);

        logger.info("User nickname change: {} -> {}", oldNickname, newNickname);

        // (임시) 변경 확인 메시지 전송(나중에는 클라이언트에게 변경 알림 브로드캐스팅 필요)
        // user.getSocketChannel().write(...)
    }
}