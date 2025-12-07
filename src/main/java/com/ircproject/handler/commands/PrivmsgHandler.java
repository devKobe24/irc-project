package com.ircproject.handler.commands;

import com.ircproject.domain.Channel;
import com.ircproject.domain.IrcMessage;
import com.ircproject.domain.User;
import com.ircproject.handler.CommandHandler;
import com.ircproject.repository.ChannelRepository;
import com.ircproject.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * packageName    : com.ircproject.handler.commands
 * fileName       : PrivmsgHandler
 * author         : kobe
 * date           : 2025. 12. 7.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 7.        kobe       최초 생성
 */
@Component
public class PrivmsgHandler implements CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(PrivmsgHandler.class);
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    public PrivmsgHandler(ChannelRepository channelRepository, UserRepository userRepository) {
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
    }

    @Override
    public String getCommand() {
        return "PRIVMSG";
    }

    @Override
    public void handle(User sender, IrcMessage message) throws IOException {
        if (message.parameters().size() < 2) {
            return; // 대상이나 메시지 내용이 없으면 무시
        }

        String target = message.parameters().get(0); // 예: "#lobboy"
        String text = message.parameters().get(1); // 예: "안녕하세요!"

        // 1. 채널 메시지인지 확인 (#으로 시작하면 채널)
        if (target.startsWith("#")) {
            sendChannelMessage(sender, target, text);
        } else {
            // 귓속말 로직 구현
            sendPrivateMessage(sender, target, text);
        }
    }

    // 귓속말 메서드
    private void sendPrivateMessage(User sender, String targetNickName, String text) {
        User targetUser = userRepository.findByNickname(targetNickName);

        if (targetNickName == null) {
            // 401 ERR_NOSUCHNICK
            sender.sendMessage(":server 401 " + sender.getNickname() + " " + targetNickName + " :No such nick/channel\r\n");
            return;
        }

        // 메시지 전송
        // 형식 :보낸사람 PRIVMSG 받는사람 :할망
        String fullMessage = ":" + sender.getNickname() + " PRIVMSG " + targetNickName + " :" + text + "\r\n";
        targetUser.sendMessage(fullMessage);

        logger.info("[Whisper] {} -> {}: {}", sender.getNickname(), targetNickName, text);
    }

    private void sendChannelMessage(User sender, String channelName, String text) {
        Channel channel = channelRepository.get(channelName);

        if (channel == null) {
            // 채널이 없으면 에러 메시지 전송 (401 ERR_NOSUCHNICK/CHANNEL)
            sender.sendMessage(":server 401 " + sender.getSocketChannel() + " " + channelName + " : No such nick/channel\r\n");
            return;
        }

        // IRC 표준 응답 포맷: 보낸사람명!user@host PRIVMSG #방이름 :할말
        // 여기서는 간단히 :닉네임 PRIVMSG ... 으로 구현
        String fullMessage = ":" + sender.getNickname() + " PRIVMSG " + channelName + " :" + text + "\r\n";

        // 브로드캐스팅 (나를 제외한 모두에게 전송)
        for (User user : channel.getUsers()) {
            if (!user.equals(sender)) {
                user.sendMessage(fullMessage);
            }
        }

        logger.info("[Chat] {} -> {}: {}", sender.getNickname(), channelName, text);
    }
}
