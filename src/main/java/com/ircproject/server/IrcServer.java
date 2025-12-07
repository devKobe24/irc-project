package com.ircproject.server;

import com.ircproject.domain.Channel;
import com.ircproject.domain.IrcMessage;
import com.ircproject.domain.User;
import com.ircproject.handler.CommandDispatcher;
import com.ircproject.parser.IrcFormatException;
import com.ircproject.parser.IrcParser;
import com.ircproject.repository.ChannelRepository;
import com.ircproject.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * packageName    : com.ircproject.server
 * fileName       : IrcServer
 * author         : kobe
 * date           : 2025. 12. 6.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 6.        kobe       최초 생성
 */

@Component
public class IrcServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(IrcServer.class);
    private static final int PORT = 6667; // IRC 표준 포트
    private static final int BUFFER_SIZE = 1024;

    private final IrcParser parser; // Parser
    private final CommandDispatcher dispatcher;
    private final Map<SocketChannel, User> userRegistry = new ConcurrentHashMap<>(); // 사용자 관리
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private boolean running = false;

    // 생성자 주입 (Spring이 자동으로 IrcParser를 넣어줍니다)
    public IrcServer(IrcParser parser,
                     CommandDispatcher dispatcher,
                     ChannelRepository channelRepository,
                     UserRepository userRepository) {
        this.parser = parser;
        this.dispatcher = dispatcher;
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
    }

    // 서버 시작 메서드 (Spring Boot가 시작되면 호출됨)
    public void start() {
        try {
            // 1. Selector(이벤트 감지자) 생성
            selector = Selector.open();

            // 2. ServerSocketChannel(서버 소켓) 생성 및 설정
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false); // **Non-blocking 모드 필수**

            // 3. Selector에 "연결 요청(ACCEPT)" 이벤트를 감시하도록 등록
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            running = true;
            logger.info("🚀 IRC Server started on port {}", PORT);

            // 4. 별도 스레드에서 무한 루프 실행 (메인 스레드 차단 방지)
            new Thread(this).start();

        } catch (IOException e) {
            logger.error("Failed to start server", e);
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                // 5. 이벤트 발생 대기 (이벤트가 없으면 여기서 블로킹됨 - CPU 낭비 방지)
                if (selector.select() == 0) {
                    continue;
                }

                // 6. 발생한 이벤트 목록 가져오기
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove(); // **중요: 처리한 이벤트는 반드시 목록에서 제거해야 함**

                    if (!key.isValid()) {
                        continue;
                    }

                    // 7. 이벤트 종류에 따른 분기 처리
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            } catch (IOException e) {
                logger.error("Error in server loop", e);
            }
        }
    }

    // [연결 처리] 새로운 클라이언트가 접속했을 때
    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept(); // 연결 수락

        clientChannel.configureBlocking(false); // 클라이언트 소켓도 Non-blocking 설정

        // 클라이언트가 "데이터를 보낼 때(READ)"를 감시하도록 Selector에 등록
        clientChannel.register(selector, SelectionKey.OP_READ);

        // 접속 시 User 객체 생성 및 등록
        User newUser = new User(clientChannel);
        userRegistry.put(clientChannel, newUser);

        logger.info("New Client Connected: {}", clientChannel.getRemoteAddress());

        // 환영 메시지 전송 (테스트용)
        sendMessage(clientChannel, """
                 _       __     __                             __                                 \s
                | |     / /__  / /________  ____ ___  ___     / /_____                            \s
                | | /| / / _ \\/ / ___/ __ \\/ __ `__ \\/ _ \\   / __/ __ \\                           \s
                | |/ |/ /  __/ / /__/ /_/ / / / / / /  __/  / /_/ /_/ /                           \s
                |__/|__/\\___/_/\\___/\\____/_/ /_/_/_/\\___/___\\__/\\____/__________ _    ____________\s
                   / //_/___  / /_  ___     /  _/ __ \\/ ____/  / ___// ____/ __ \\ |  / / ____/ __ \\
                  / ,< / __ \\/ __ \\/ _ \\    / // /_/ / /       \\__ \\/ __/ / /_/ / | / / __/ / /_/ /
                 / /| / /_/ / /_/ /  __/  _/ // _, _/ /___    ___/ / /___/ _, _/| |/ / /___/ _, _/\s
                /_/ |_\\____/_.___/\\___/  /___/_/ |_|\\____/   /____/_____/_/ |_| |___/_____/_/ |_| \s
    \r\n""");

        // 닉네임 설정 메시지 전송
        sendMessage(clientChannel, "PLZ MAKE OWN YOUR NICKNAME :)\r\n");
        sendMessage(clientChannel, "[EXAMPLE] NIKC $Own_your_nickname\r\n");
        sendMessage(clientChannel, "IF YOU WANT TO MORE INFO PLZ USE 'HELP' COMMAND\r\n");
    }

    // [데이터 수신] 클라이언트가 메시지를 보냈을 때
    private void handleRead(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        // 레지스트리에서 현재 사용자 찾기
        User user = userRegistry.get(clientChannel);
        if (user == null) {
            return; // 예외 상황
        }

        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        try {
            int bytesRead = clientChannel.read(buffer);

            if (bytesRead == -1) {
                // -1은 클라이언트가 연결을 끊었다는 신호
                disconnect(key, clientChannel);
                return;
            }

            if (bytesRead > 0) {
                // 읽기 모드로 전환 (Write -> Read)
                buffer.flip();
                // 바이트를 문자열로 변환
                String data = StandardCharsets.UTF_8.decode(buffer).toString();

                // [핵심] 1. 데이터를 유저 버퍼에 쌓음
                user.appendData(data);

                // [핵심] 2. 완성된 문장이 있는지 확인 (여러 문장이 한 번에 올 수도 있음 - while)
                String line;
                while ((line = user.nextLine()) != null) {
                    processMessage(user, line);
                }
                logger.info("Received {}", data.trim());
            }
        } catch (IOException e) {
            logger.warn("Connection reset by peer");
            disconnect(key, clientChannel);
        }
    }

    // 메시지 처리 로직
    private void processMessage(User user, String line) {
        try {
            logger.info("[RAW] Client says: {}", line);

            // 1. 파싱
            IrcMessage message = parser.parse(line);

            // 2. 디스패처에게 위임 (이제 서버는 구제척인 명령어를 몰라도 됩니다.)
            dispatcher.dispatch(user, message);

        } catch (IrcFormatException e) {
            logger.warn("Parsing Failed: {}", e.getMessage());
            // 파싱 에러 시 클라이언트에게 알려주는 것이 관례
            try {
                sendMessage(user.getSocketChannel(), "ERROR :Invalid Message Format\r\n");
            } catch (IOException ignored) {}
        }
    }

    // 메시지 전송 헬퍼 메서드
    private void sendMessage(SocketChannel client, String message) throws IOException {
        client.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
    }

    // 연결 종료 처리
    private void disconnect(SelectionKey key, SocketChannel clientChannel) {
        try {
            User user = userRegistry.get(clientChannel);
            if (user != null) {
                logger.info("Client Disconnected: {} ({})", clientChannel.getRemoteAddress(), user.getNickname());

                // 닉네임 저장소에서 삭제
                userRepository.remove(user.getNickname());

                userRegistry.remove(clientChannel);

                // 유령 유저 방지: 가입된 모든 채널에서 퇴장 처리
                for (String channelName : user.getJoinedChannels()) {
                    Channel channel = channelRepository.get(channelName);
                    if (channel != null) {
                        channel.leave(user); // 채널에서 유저 삭제
                        logger.info("Cleaned up user {} from channel {}", user.getNickname(), channelName);

                        // 방에 남은 사람들에게 "누가 나갔다"고 알려주기 (PART 메시지 전송)
                        broadcastPartMessage(channel, user);
                    }
                }
                userRegistry.remove(clientChannel);
            }
            key.cancel(); // Selector 감시 취소
            clientChannel.close(); // 소켓 닫기
        } catch (IOException e) {
            logger.error("Error closing channel", e);
        }
    }

    // PART 메세지 전송 메서드
    private void broadcastPartMessage(Channel channel, User leaver) {
        // 1. IRC 표준 프로토콜 메시지 생성
        // 형식: :닉네임 PART #채널명 :메시지
        String partMessage = ":" + leaver.getNickname() + " PART " + channel.getName() + "\r\n";
        String leaveMessage = leaver.getNickname() + " " + "leave this channel : " + channel.getName() + "\r\n";

        // 2. 채널에 남아있는 모든 유저에게 전송 (Loop)
        for (User member : channel.getUsers()) {
            // 떠나는 본인이 아닌 경우에만 전송
            if (!member.equals(leaver)) {
                member.sendMessage(partMessage);
                member.sendMessage(leaveMessage);
            }
        }
    }
}
