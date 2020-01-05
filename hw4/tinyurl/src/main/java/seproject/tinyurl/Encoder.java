package seproject.tinyurl;

import java.util.HashMap;
import java.util.Map;

public class Encoder {

    private static final String DICT = "abcdefghijklmnopqrstuvwxyz" +
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    // 数字->字符映射
    private static final char[] CHARS = DICT.toCharArray();
    // 字符->数字映射
    private static final Map<Character, Integer> NUMBERS = new HashMap<>();

    static {
        int len = CHARS.length;
        for (int i = 0; i<len; i++) {
            NUMBERS.put(CHARS[i], i);
        }
    }

    public String encode(long id) {
        StringBuilder shortURL = new StringBuilder();
        while (id > 0) {
            int r = (int) (id % 62);
            shortURL.insert(0, CHARS[r]);
            id = id / 62;
        }
        int len = shortURL.length();
        while (len < 6) {
            shortURL.insert(0, CHARS[0]);
            len++;
        }
        return shortURL.toString();
    }

    public long decode(String key) {
        char[] shorts = key.toCharArray();
        int len = shorts.length;
        long id = 0l;
        for (int i = 0; i < len; i++) {
            id = id + (long) (NUMBERS.get(shorts[i]) * Math.pow(62, len-i-1));
        }
        return id;
    }

}