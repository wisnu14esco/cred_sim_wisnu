package com.wisnu.creditsimulator.json;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON parser.
 */
public final class JsonParser {

    private final String text;
    private int pos;

    private JsonParser(String text) {
        this.text = text;
    }

    public static Object parse(String text) {
        if (text == null) {
            throw new JsonException("JSON input is null");
        }
        JsonParser parser = new JsonParser(text);
        parser.skipWhitespace();
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (!parser.atEnd()) {
            throw parser.error("Unexpected trailing content");
        }
        return value;
    }

    /** Parses text that must be a JSON object. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String text) {
        Object value = parse(text);
        if (!(value instanceof Map)) {
            throw new JsonException("Expected a JSON object");
        }
        return (Map<String, Object>) value;
    }

    private Object readValue() {
        if (atEnd()) {
            throw error("Unexpected end of input");
        }
        char c = text.charAt(pos);
        switch (c) {
            case '{':
                return readObject();
            case '[':
                return readArray();
            case '"':
                return readString();
            case 't':
                expectLiteral("true");
                return Boolean.TRUE;
            case 'f':
                expectLiteral("false");
                return Boolean.FALSE;
            case 'n':
                expectLiteral("null");
                return null;
            default:
                if (c == '-' || isDigit(c)) {
                    return readNumber();
                }
                throw error("Unexpected character '" + c + "'");
        }
    }

    private Map<String, Object> readObject() {
        Map<String, Object> object = new LinkedHashMap<>();
        pos++; // '{'
        skipWhitespace();
        if (peek('}')) {
            pos++;
            return object;
        }
        while (true) {
            skipWhitespace();
            if (!peek('"')) {
                throw error("Expected a string key");
            }
            String key = readString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            object.put(key, readValue());
            skipWhitespace();
            if (peek(',')) {
                pos++;
            } else if (peek('}')) {
                pos++;
                return object;
            } else {
                throw error("Expected ',' or '}'");
            }
        }
    }

    private List<Object> readArray() {
        List<Object> array = new ArrayList<>();
        pos++; // '['
        skipWhitespace();
        if (peek(']')) {
            pos++;
            return array;
        }
        while (true) {
            skipWhitespace();
            array.add(readValue());
            skipWhitespace();
            if (peek(',')) {
                pos++;
            } else if (peek(']')) {
                pos++;
                return array;
            } else {
                throw error("Expected ',' or ']'");
            }
        }
    }

    private String readString() {
        pos++; // opening quote
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (atEnd()) {
                throw error("Unterminated string");
            }
            char c = text.charAt(pos++);
            if (c == '"') {
                return sb.toString();
            }
            if (c < 0x20) {
                throw error("Control character in string");
            }
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            if (atEnd()) {
                throw error("Unterminated escape sequence");
            }
            char escaped = text.charAt(pos++);
            switch (escaped) {
                case '"':
                case '\\':
                case '/':
                    sb.append(escaped);
                    break;
                case 'b':
                    sb.append('\b');
                    break;
                case 'f':
                    sb.append('\f');
                    break;
                case 'n':
                    sb.append('\n');
                    break;
                case 'r':
                    sb.append('\r');
                    break;
                case 't':
                    sb.append('\t');
                    break;
                case 'u':
                    sb.append(readUnicodeEscape());
                    break;
                default:
                    throw error("Invalid escape '\\" + escaped + "'");
            }
        }
    }

    private char readUnicodeEscape() {
        if (pos + 4 > text.length()) {
            throw error("Incomplete unicode escape");
        }
        String hex = text.substring(pos, pos + 4);
        try {
            pos += 4;
            return (char) Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            throw error("Invalid unicode escape '" + hex + "'");
        }
    }

    private BigDecimal readNumber() {
        int start = pos;
        if (peek('-')) {
            pos++;
        }
        if (peek('0')) {
            pos++;
        } else {
            readDigits();
        }
        if (peek('.')) {
            pos++;
            readDigits();
        }
        if (peek('e') || peek('E')) {
            pos++;
            if (peek('+') || peek('-')) {
                pos++;
            }
            readDigits();
        }
        return new BigDecimal(text.substring(start, pos));
    }

    private void readDigits() {
        int start = pos;
        while (!atEnd() && isDigit(text.charAt(pos))) {
            pos++;
        }
        if (start == pos) {
            throw error("Expected a digit");
        }
    }

    private void expectLiteral(String literal) {
        if (!text.startsWith(literal, pos)) {
            throw error("Expected '" + literal + "'");
        }
        pos += literal.length();
    }

    private void expect(char c) {
        if (!peek(c)) {
            throw error("Expected '" + c + "'");
        }
        pos++;
    }

    private boolean peek(char c) {
        return !atEnd() && text.charAt(pos) == c;
    }

    private boolean atEnd() {
        return pos >= text.length();
    }

    private void skipWhitespace() {
        while (!atEnd()) {
            char c = text.charAt(pos);
            if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
                return;
            }
            pos++;
        }
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private JsonException error(String message) {
        return new JsonException(message + " at position " + pos);
    }
}
