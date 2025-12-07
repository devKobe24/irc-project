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
 * fileName       : PartHandler
 * author         : kobe
 * date           : 2025. 12. 8.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 8.        kobe       최초 생성
 */
@Component
public class PartHandler implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(PartHandler.class);
    private final ChannelRepository channelRepository;

    public PartHandler(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    @Override
    public String getCommand() {
        return "PART";
    }

    @Override
    public void handle(User user, IrcMessage message) throws IOException {
        if (message.parameters().isEmpty()) {
            // 에러: 채널명을 입력하지 않음 (461 ERR_NEEDMOREPARAMS)
            user.sendMessage(":server 461 " + user.getNickname() + " PART :Not enough parameters\r\n");
            return;
        }

        String channelName = message.parameters().get(0);

        // 퇴장 사유 (옵션): 입력 안 했으면 기본값 "Leaving"
        String reason = (message.parameters().size() > 1) ? message.parameters().get(1) : "Leaving";

        Channel channel = channelRepository.get(channelName);

        // 1. 채널 존재 여부 및 가입 여부 확인
        if (channel == null || !user.getJoinedChannels().contains(channelName)) {
            user.sendMessage(":server 442 " + user.getNickname() + " " + channelName + " :You're not on that channel\r\n");
            return;
        }

        // 2. 상태 업데이트 (양방향 삭제)
        channel.leave(user); // 채널에서 유저 뺌
        user.removeChannel(channelName); // 유저에게서 채널 뺌

        logger.info("User {} left channel {}", user.getNickname(), channelName);

        // 3. 브로드캐스팅 (방에 남은 사람들에게 알림 + 나가는 본인에게도 알림)
        broadcastPartMessage(channel, user, reason);
    }


    private void broadcastPartMessage(Channel channel, User leaver, String reason) {
        // IRC 표준 :닉네임 PART #채널 :이유
        String partMessage = ":" + leaver.getNickname() + " PART " + channel.getName() + " :" + reason + "\r\n";

        // 나가는 사람 본인에게도 PART 메시지를 보내야 클라이언트가 방을 닫습니다.
        leaver.sendMessage(partMessage);

        // 방에 남은 사람들에게 전송
        for (User member : channel.getUsers()) {
            member.sendMessage(partMessage);
        }
    }
}