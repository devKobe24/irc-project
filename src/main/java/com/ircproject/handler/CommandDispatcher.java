package com.ircproject.handler;

import com.ircproject.domain.IrcMessage;
import com.ircproject.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * packageName    : com.ircproject.handler
 * fileName       : CommandDispatcher
 * author         : kobe
 * date           : 2025. 12. 7.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 7.        kobe       최초 생성
 */
@Component
public class CommandDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(CommandDispatcher.class);
    private final Map<String, CommandHandler> handlers = new HashMap<>();

    // 생성자 주입: Spring이 알아서 CommandHandler를 구현한 모든 빈을 List에 담아줍니다.
    public CommandDispatcher(List<CommandHandler> commandHandlers) {
        for (CommandHandler handler : commandHandlers) {
            handlers.put(handler.getCommand(), handler);
            logger.info("Registered Command Handler: {}", handler.getCommand());
        }
    }

    public void dispatch(User user, IrcMessage message) {
        String command = message.command().toUpperCase(); // 대소문자 무시 (NICK == nick)

        CommandHandler handler = handlers.get(command);

        if (handler == null) {
            logger.warn("Unknown Command: {}", command);
            // 나중에 "421 ERR_UNKNOWNCOMMAND" 에러 메시지 전송 로직 추가
            return;
        }

        try {
            handler.handle(user, message);
        } catch (Exception e) {
            logger.error("Error handling command: {}", command, e);
        }
    }
}