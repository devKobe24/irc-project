package com.ircproject.parser;

import com.ircproject.domain.IrcMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * packageName    : com.ircproject.parser
 * fileName       : IrcParser
 * author         : kobe
 * date           : 2025. 12. 5.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 5.        kobe       최초 생성
 */
@Component
public class IrcParser {

    // 정규표현식 컴파일 (성능 최적화를 위해 static final로 선언)
    // 그룹 1: Prefix (옵션) - 예: :nick!user@host
    // 그룹 2: Command (필수) - 예: PRIVMSG 또는 001
    // 그룹 3: Parameters (옵션) - 나머지 뒷부분 전체
    private static final Pattern IRC_PATTERN = Pattern.compile(
            "^(?::(\\S+)\\s+)?([a-zA-Z]+|[0-9]{3})(?:\\s+(.*))?$"
    );

    public IrcMessage parse(String line) {
        if (line == null || line.isBlank()) {
            throw new IrcFormatException("Message cannot be empty");
        }

        // 1. 정규식 매칭 시도
        Matcher matcher = IRC_PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new IrcFormatException("Invalid IRC message format: " + line);
        }

        // 2. 그룹별 데이터 추출
        String prefix = matcher.group(1);  // 첫 번째 괄호
        String command = matcher.group(2); // 두 번째 괄호
        String rawParams = matcher.group(3); // 세 번째 괄호 (파라미터 덩어리)

        // 3. 파라미터 세부 분리 로직 호출
        List<String> parameters = parseParameters(rawParams);

        return new IrcMessage(prefix, command, parameters);
    }

    /**
     * 파라미터 문자열을 RFC 규칙에 따라 리스트로 변환합니다.
     * 규칙: " :" (공백+콜론)이 나오면 그 뒤는 전체가 하나의 파라미터(Trailing)입니다.
     */
    private List<String> parseParameters(String rawParams) {
        List<String> params = new ArrayList<>();

        if (rawParams == null || rawParams.isBlank()) {
            return params;
        }

        // Trailing Parameter 확인 ( " :" 로 시작하는 부분이 있는지 탐색)
        int trailingIndex = rawParams.indexOf(" :");

        if (trailingIndex >= 0) {
            // Case 1: Trailing Parameter가 존재하는 경우
            // 예: "#channel :Hello World this is message"

            // 앞부분(일반 파라미터) 처리
            String normalPart = rawParams.substring(0, trailingIndex);
            if (!normalPart.isEmpty()) {
                String[] parts = normalPart.split("\\s+");
                for (String part : parts) {
                    params.add(part);
                }
            }

            // 뒷부분(Trailing 파라미터) 처리 - 콜론(:) 제외하고 추가
            String trailingPart = rawParams.substring(trailingIndex + 2);
            params.add(trailingPart);

        } else {
            // Case 2: Trailing Parameter가 없는 경우
            // 예: "param1 param2 param3"

            // 예외 케이스: 라인 자체가 ":"로 시작하는 경우 (Trailing만 있는 경우)
            if (rawParams.startsWith(":")) {
                params.add(rawParams.substring(1));
            } else {
                // 단순히 공백으로 분리
                String[] parts = rawParams.split("\\s+");
                for (String part : parts) {
                    params.add(part);
                }
            }
        }

        return params;
    }
}
