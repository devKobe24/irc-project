package com.ircproject.handler.commands;

import com.ircproject.domain.Channel;
import com.ircproject.domain.IrcMessage;
import com.ircproject.domain.User;
import com.ircproject.handler.CommandHandler;
import com.ircproject.repository.ChannelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * packageName    : com.ircproject.handler.commands
 * fileName       : QuitHandler
 * author         : kobe
 * date           : 2025. 12. 10.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 10.        kobe       최초 생성
 */
@Component
public class QuitHandler implements CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(QuitHandler.class);
    private final ChannelRepository channelRepository;

    public QuitHandler(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    @Override
    public String getCommand() {
        return "QUIT";
    }

    @Override
    public void handle(User user, IrcMessage message) throws IOException {
        // 종료 사유 (옵션)
        String reason = (message.parameters().isEmpty()) ? "Client Quit" : message.parameters().get(0);

        // 1. 내가 속한 모든 채널에 QUIT 메시지 브로드캐스팅
        // IRC 표준: :닉네입 QUIT :이유
        String quitMessage = ":" + user.getNickname() + " QUIT :" + reason + "\r\n";

        for (String channelName : user.getJoinedChannels()) {
            Channel channel = channelRepository.get(channelName);
            if (channel != null) {
                // 방에 있는 다른 사람들에게 알림
                for (User member : channel.getUsers()) {
                    if (!member.equals(user)) {
                        member.sendMessage(quitMessage);
                    }
                }
            }
        }

        logger.info("User {} is quitting: {}", user.getNickname(), reason);

        // 2. 클라이언트에게 마지막 에러 메시지 전송 (표준 절차)
        // 이메시지를 받고 클라이언트는 스스로 소켓을 닫거나, 서버가 닫기를 기다림
        user.sendMessage("ERROR :Closing Link: " + user.getNickname() + " (" + reason + ")\r\n");

        // 3. 소켓 강제 종료
        // 소켓을 닫으면 IrcServer.run() 루프에서 read()가 -1을 반환하거나 Exception이 발생하여
        // IrcServer.disconnect()가 호출되고, 거기서 '채널 퇴장' 및 '메모리 정리'가 수행됩니다.
        user.getSocketChannel().close();
    }
}
