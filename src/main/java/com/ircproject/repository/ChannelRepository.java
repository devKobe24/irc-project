package com.ircproject.repository;

import com.ircproject.domain.Channel;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * packageName    : com.ircproject.repository
 * fileName       : ChannelRepository
 * author         : kobe
 * date           : 2025. 12. 7.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 7.        kobe       최초 생성
 */
@Component
public class ChannelRepository {

    // key: 채널이름 (예: "#lobby"), value: Channel 객체
    private final Map<String, Channel> channels = new ConcurrentHashMap<>();

    /**
     * 채널을 가져오거나, 없으면 새로 생성해서 반환합니다.
     */
    public Channel getOrCreate(String name) {
        return channels.computeIfAbsent(name, Channel::new);
    }

    /**
     * 특정 채널을 찾습니다. (없으면 null)
     */
    public Channel get(String name) {
        return channels.get(name);
    }

    // 필요하다면 채널 삭제(remove) 등의 메서드 추가
    /**
     * 특정 채널을 삭제합니다. (없으면 null)
     */
    public Channel remove(String name) {
        return channels.remove(name);
    }

    /**
     * 활성화 된 모든 채널 목록을 반환합니다.
     */
    public Collection<Channel> findAll() {
        return channels.values();
    }
}
