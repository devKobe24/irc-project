package com.ircproject.handler.commands;

import com.ircproject.domain.IrcMessage;
import com.ircproject.domain.User;
import com.ircproject.handler.CommandHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * packageName    : com.ircproject.handler.commands
 * fileName       : HelpHandler
 * author         : kobe
 * date           : 2025. 12. 7.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 7.        kobe       최초 생성
 */
@Component
public class HelpHandler implements CommandHandler {

    @Override
    public String getCommand() {
        return "HELP";
    }

    @Override
    public void handle(User user, IrcMessage message) throws IOException {
        String nickname = user.getNickname();

        // 도움말 제목
        sendNotice(user, "=== IRC Server Commands ===");

        // 명령어 목록 (지금까지 구현한 기능들)
        sendNotice(user, "1. NICK <새 닉네임> : 닉네임을 변경합니다.");
        sendNotice(user, "2. JOIN <#채널명> : 해당 채널에 입장합니다.");
        sendNotice(user, "3. PRIVMSG <#채널명|닉네임> <메세지> : 대화를 보냅니다.");
        sendNotice(user, "4. HELP : 이 도움말을 표시합니다.");
        sendNotice(user, "5. LIST : 채널 리스트를 표시합니다.");
        sendNotice(user, "6. PART <#채널명> : 해당 채널에서 퇴장합니다.");
    }

    // 헬퍼 메서드: NOTICE 명령어로 서버 메시지 전송
    // 형식 :server NOTICE 닉네임 :내용
    private void sendNotice(User user, String text) {
        String msg = ":server NOTICE " + user.getNickname() + " :" + text + "\r\n";
        user.sendMessage(msg);
    }
}