package com.ircproject.handler.commands;

import com.ircproject.domain.IrcMessage;
import com.ircproject.domain.User;
import com.ircproject.handler.CommandHandler;
import com.ircproject.repository.UserRepository;
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
    private final UserRepository userRepository;

    public NickHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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

        // 닉네임 중복 체크
        if (userRepository.exists(newNickname)) {
            user.sendMessage(":server 433 * " + newNickname + " :Nickname is already in use\r\n");
            return;
        }

        // [Core Logic] 저장소 업데이트
        // 기존 닉네임으로 저장된 기록 삭제 (처음 접속 시 "*" 닉네임은 없을 수 있으므로 체크)
        userRepository.remove(oldNickname);

        // 유저 객체 닉네임 변경
        user.setNickname(newNickname);

        // 새로운 닉네임으로 저장
        userRepository.save(user);

        logger.info("User nickname change: {} -> {}", oldNickname, newNickname);

        // 변경 알림 (자신에게)
        // :oldNick NICK :newNick
        user.sendMessage(":" + oldNickname + " NICK " + newNickname + "\r\n");
    }
}