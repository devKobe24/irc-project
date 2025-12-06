package com.ircproject.server;

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
import java.util.Set;

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

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private boolean running = false;

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

        logger.info("New Client Connected: {}", clientChannel.getRemoteAddress());

        // 환영 메시지 전송 (테스트용)
        sendMessage(clientChannel, "Welcome to Java NIO IRC Server!\r\n");
    }

    // [데이터 수신] 클라이언트가 메시지를 보냈을 때
    private void handleRead(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
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
                String message = StandardCharsets.UTF_8.decode(buffer).toString();
                logger.info("Received {}", message.trim());

                // [Echo 로직] 받은 메시지를 그대로 다시 돌려줌
                sendMessage(clientChannel, "ECHO: " + message);
            }

        } catch (IOException e) {
            logger.warn("Connection reset by peer");
            disconnect(key, clientChannel);
        }
    }

    // 메시지 전송 헬퍼 메서드
    private void sendMessage(SocketChannel client, String message) throws IOException {
        client.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
    }

    // 연결 종료 처리
    private void disconnect(SelectionKey key, SocketChannel clientChannel) {
        try {
            logger.info("Client Disconnected: {}", clientChannel.getRemoteAddress());
            key.cancel(); // Selector 감시 취소
            clientChannel.close(); // 소켓 닫기
        } catch (IOException e) {
            logger.error("Error closing channel", e);
        }
    }
}
