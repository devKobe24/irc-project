package com.ircproject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * packageName    : com.ircproject.config
 * fileName       : SslConfig
 * author         : kobe
 * date           : 2025. 12. 10.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 10.        kobe       최초 생성
 */
@Configuration
public class SslConfig {

    @Value("${irc.ssl.keystore-path}")
    private String keystorePath;

    @Value("${irc.ssl.keystore-password}")
    private String keystorePassword;

    @Value("${irc.ssl.keystore-type}")
    private String keystoreType;

    private final ResourceLoader resourceLoader;

    // ResourceLoader 주입 (JAR 배포 호환성 확보)
    public SslConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public SSLContext sslContext() throws Exception{
        // 1. Keystore 로드
        // JAR 내부의 파일은 'File' 객체가 아니라 'InputStream'으로 읽어야 합니다.
        KeyStore keyStore = KeyStore.getInstance(keystoreType);

        Resource resource = resourceLoader.getResource(keystorePath);

        // try-with-resources 구문으로 스트림 자동 종료
        try (InputStream stream = resource.getInputStream()) {
            keyStore.load(stream, keystorePassword.toCharArray());
        }

        // 2. KeyManager 초기화 (내 신분증)
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keystorePassword.toCharArray());

        // 3. TrustManager 초기화 (상대방 검증용)
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        // 4. SSLContext 생성 (TLS 프로토콜)
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return sslContext;
    }
}
