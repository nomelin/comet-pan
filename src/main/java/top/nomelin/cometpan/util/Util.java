package top.nomelin.cometpan.util;

public class Util {
    /**
     * 判断字符串是否为整数
     */
    public static boolean isNumber(String str) {
        return str != null && str.chars().allMatch(Character::isDigit);
    }
}
