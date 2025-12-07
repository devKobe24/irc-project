package com.ircproject.handler.commands;

import com.ircproject.domain.Channel;
import com.ircproject.domain.IrcMessage;
import com.ircproject.domain.User;
import com.ircproject.handler.CommandHandler;
import com.ircproject.repository.ChannelRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

/**
 * packageName    : com.ircproject.handler.commands
 * fileName       : ChannelListHandler
 * author         : kobe
 * date           : 2025. 12. 7.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 7.        kobe       최초 생성
 */
@Component
public class ChannelListHandler implements CommandHandler {
    private final ChannelRepository channelRepository;

    public ChannelListHandler(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    @Override
    public String getCommand() {
        return "LIST";
    }

    @Override
    public void handle(User user, IrcMessage message) throws IOException {
        Collection<Channel> channels = channelRepository.findAll();

        sendNotice(user, "=== Active Channel List ===");

        if (channels.isEmpty()) {
            sendNotice(user,"No active channels found");
        } else {
            int count = 1;
            for (Channel channel : channels) {
                // 예: 1, #lobby (3 users)
                String info = String.format("%d. %s (%d users)",
                        count++,
                        channel.getName(),
                        channel.getUsers().size());
                sendNotice(user, info);
            }
        }
        sendNotice(user, "==============================");
    }

    private void sendNotice(User user, String text) {
        // :server NOTICE 닉네임 :내용
        String msg = ":server NOTICE " + user.getNickname() + " :" + text + "\r\n";
        user.sendMessage(msg);
    }
}