package com.ircproject.parser;

import com.ircproject.domain.IrcMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * packageName    : com.ircproject.parser
 * fileName       : IrcParserTest
 * author         : kobe
 * date           : 2025. 12. 5.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 5.        kobe       최초 생성
 */
class IrcParserTest {

    private final IrcParser parser = new IrcParser();

    @Test
    @DisplayName("1. [Standard] 완벽한 형태의 IRC 메시지 파싱 (Prefix + Command + Params + Trailing)")
    void parseFullMessage() {
        // Given
        String raw = ":dave!user@host PRIVMSG #channel :Hello World!";

        // When
        IrcMessage message = parser.parse(raw);

        // Then
        assertThat(message.prefix()).isEqualTo("dave!user@host");
        assertThat(message.command()).isEqualTo("PRIVMSG");
        assertThat(message.parameters()).containsExactly("#channel", "Hello World!");
    }

    @Test
    @DisplayName("2. [Command Only] 파라미터와 Prefix가 없는 가장 단순한 커맨드")
    void parseSimpleCommand() {
        // Given
        String raw = "QUIT";

        // When
        IrcMessage message = parser.parse(raw);

        // Then
        assertThat(message.prefix()).isNull(); // Prefix가 없으면 null이어야 함
        assertThat(message.command()).isEqualTo("QUIT");
        assertThat(message.parameters()).isEmpty();
    }

    @Test
    @DisplayName("3. [Trailing] 콜론(:) 뒤의 공백이 포함된 긴 문장이 하나의 파라미터로 잡혀야 한다")
    void parseTrailingParameter() {
        // Given
        // 주의: 파라미터 사이에 공백이 여러 개 있어도 처리되어야 함
        String raw = "PRIVMSG    #lobby    :This is a long message with spaces";

        // When
        IrcMessage message = parser.parse(raw);

        // Then
        assertThat(message.command()).isEqualTo("PRIVMSG");
        assertThat(message.parameters()).hasSize(2);
        assertThat(message.parameters().get(0)).isEqualTo("#lobby");
        // 핵심: 콜론 뒤의 문장은 쪼개지지 않고 하나로 나와야 함
        assertThat(message.parameters().get(1)).isEqualTo("This is a long message with spaces");
    }

    @Test
    @DisplayName("4. [Edge Case] 메시지 내용(Trailing) 안에 콜론(:)이 또 들어있는 경우")
    void parseMessageWithColons() {
        //Given
        // IRC에서 흔한 실수: 메시지 내용 자체에 이모티콘이나 시간(12:00)이 들어간 경우
        String raw = "PRIVMSG #channel :It is 12:00 PM :)";

        // When
        IrcMessage message = parser.parse(raw);

        // Then
        assertThat(message.parameters()).containsExactly("#channel", "It is 12:00 PM :)");
    }

    @Test
    @DisplayName("5. [Numeric] 숫자로 된 커맨드(Response Code) 파싱")
    void parseNumericResponse() {
        // Given
        String raw = ":irc.server.com 001 nickname :Welcome to the IRC Network";

        // When
        IrcMessage message = parser.parse(raw);

        // Then
        assertThat(message.prefix()).isEqualTo("irc.server.com");
        assertThat(message.command()).isEqualTo("001");
        assertThat(message.parameters()).contains("nickname", "Welcome to the IRC Network");
    }

    @ParameterizedTest
    @DisplayName("6. [Exception] 잘못된 입력값은 예외를 던져야 한다")
    @ValueSource(strings = {"", "   ", ":InvalidPrefixWithoutSpace"})
    // ":InvalidPrefixWithoutSpace" (콜론으로 시작하면 반드시 뒤에 공백이 있어야 함 -> 정규식 불일치)
    // 만약 정규식이 너무 관대하면 이 테스트가 실패할 수 있음 (그럴 땐 정규식 수정 필요)
    void parseInvalidInputs(String invalidInput) {
        assertThatThrownBy(() -> parser.parse(invalidInput))
                .isInstanceOf(IrcFormatException.class);
    }

    @Test
    @DisplayName("7. [Null] 입력이 null인 경우")
    void parseNullInput() {
        assertThatThrownBy(() -> parser.parse(null))
                .isInstanceOf(IrcFormatException.class)
                .hasMessageContaining("cannot be empty");
    }

}