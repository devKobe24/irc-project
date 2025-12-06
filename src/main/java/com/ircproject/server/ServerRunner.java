package com.ircproject.server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * packageName    : com.ircproject.server
 * fileName       : ServerRunner
 * author         : kobe
 * date           : 2025. 12. 6.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 6.        kobe       최초 생성
 */
@Component
public class ServerRunner implements CommandLineRunner {

    private final IrcServer ircServer;

    public ServerRunner(IrcServer ircServer) {
        this.ircServer = ircServer;
    }

    @Override
    public void run(String... args) throws Exception {
        // Spring Boot 구동 완료 시점에 IRC 서버 시작
        ircServer.start();
    }
}
