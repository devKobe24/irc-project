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
 * fileName       : JoinHandler
 * author         : kobe
 * date           : 2025. 12. 7.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 7.        kobe       최초 생성
 */
@Component
public class JoinHandler implements CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(JoinHandler.class);
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    // 생성자 주입
    public JoinHandler(ChannelRepository channelRepository, UserRepository userRepository) {
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
    }

    @Override
    public String getCommand() {
        return "JOIN";
    }

    @Override
    public void handle(User user, IrcMessage message) throws IOException {
        if (message.parameters().isEmpty()) {
            return; // 파라미터가 없으면 무시 (원래는 에러 메시지 전송)
        }

        String channelName = message.parameters().get(0);

        // 채널 저장소에서 채널을 가져오거나 생성
        Channel channel = channelRepository.getOrCreate(channelName);

        // 채널에 유저 입장
        channel.join(user);

        // 유저에게 채널 등록
        user.addChannel(channel.getName());

        logger.info("User {} joined channel {}", user.getNickname(), channel.getName());
        logger.info("Current users in {}: {}", channel.getName(), channel.getUsers().size());

        // (중요) 3. [TODO] 같은 방에 있는 사람들에게 "누가 들어왔다"고 알려줘야 함 (Broadcasting)
        broadcastJoinMessage(channel, user);
    }

    private void broadcastJoinMessage(Channel channel, User joiner) {
        String joinMessage = ":" + joiner.getNickname() + " JOIN " + channel.getName() + "\r\n";

        for (User member : channel.getUsers()) {
            member.sendMessage(joinMessage);
        }
    }
}