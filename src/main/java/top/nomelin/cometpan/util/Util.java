package top.nomelin.cometpan.util;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.exception.SystemException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * 通用工具类
 *
 * @author nomelin
 */
public class Util {
    /**
     * 判断字符串是否为整数
     */
    public static boolean isNumber(String str) {
        return str != null && str.chars().allMatch(Character::isDigit);
    }

    /**
     * 将list转换为PageInfo
     * 用于查好list以后再转换为PageInfo对象，以便分页展示
     */
    public static <T> PageInfo<T> listToPage(List<T> list, Integer pageNum, Integer pageSize) {
        //创建Page类
        Page<T> page = new Page<T>(pageNum, pageSize);
        //为Page类中的total属性赋值
        page.setTotal(list.size());
        //计算当前需要显示的数据下标起始值
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, list.size());
        //从链表中截取需要显示的子链表，并加入到Page
        page.addAll(list.subList(startIndex, endIndex));
        //以Page创建PageInfo
        return new PageInfo<>(page);
    }

    /**
     * 移除文件名称的类型（如果类型不是空字符串），如：abc.txt -> abc，abc -> abc
     *
     * @param str    完整名称
     * @param suffix 类型(不包含点),可以是null或空字符串
     * @return 移除类型后的字符串
     */
    public static String removeType(String str, String suffix) {
        if (ObjectUtil.isNull(str)) {
            return null;
        }
        if (ObjectUtil.isNull(suffix) || StrUtil.isEmpty(suffix)) {
            return str;
        }
        suffix = "." + suffix;
        if (str.endsWith(suffix)) {
            return str.substring(0, str.length() - suffix.length());
        }
        return str;
    }

    /**
     * 自动移除文件名称的类型(如果没有.则认为没有类型)
     *
     * @param str 完整名称
     * @return 移除类型后的字符串
     */
    public static String removeType(String str) {
        if (ObjectUtil.isNull(str)) {
            return null;
        }
        int index = str.lastIndexOf(".");
        if (index == -1) {
            return str;
        }
        return str.substring(0, index);
    }

    /**
     * 获取文件类型
     *
     * @param str 文件名称
     * @return 文件类型, 不包含点。如果没有类型，则返回空字符串
     */
    public static String getType(String str) {
        if (ObjectUtil.isNull(str)) {
            return null;
        }
        int index = str.lastIndexOf(".");
        if (index == -1) {
            return "";
        }
        return str.substring(index + 1);
    }

    /**
     * 获取文件全名,如：abc和txt -> abc.txt
     * 如果type为空或者null，则不添加后缀，也没有小数点。
     *
     * @param name 文件名
     * @param type 文件类型
     * @return 文件全名
     */
    public static String getFullName(String name, String type) {
        if (ObjectUtil.isNull(name)) {
            return "";
        }
        if (ObjectUtil.isNull(type) || StrUtil.isEmpty(type)) {
            return name + "";
        }
        return name + "." + type;
    }

    /**
     * 将整数数组转换为字符串
     *
     * @param list 整数数组
     * @return 字符串，如："1,2,3,4"
     */
    public static String getArrayStr(List<Integer> list) {
        if (ObjectUtil.isNull(list) || list.isEmpty()) {
            return "";
        }
        return StrUtil.join(",", list);
    }

    /**
     * 将字符串转换为整数数组
     *
     * @param str 字符串，如："1,2,3,4"
     */
    public static List<Integer> getArrayInt(String str) {
        if (StrUtil.isEmpty(str)) {
            return new ArrayList<>();
        }
        List<Integer> resultList = new ArrayList<>();
        // 按照逗号分割字符串，并去除空格
        String[] strArray = str.split(",");
        // 将每个分割得到的字符串转换为整数并添加到结果列表中
        for (String s : strArray) {
            try {
                Integer intValue = Integer.parseInt(s.trim()); // 转换为Integer对象
                resultList.add(intValue); // 添加到结果列表中
            } catch (NumberFormatException e) {
                throw new SystemException(CodeMessage.SYSTEM_ERROR);
            }
        }
        return resultList;
    }

    /**
     * 随机混合任意数量的字符串
     *
     * @param strings 任意数量的字符串
     * @return 随机混合后的字符串
     */
    public static String randomMixStrings(String... strings) {
        // 将传入的字符串转换为字符数组并放入一个列表中
        List<Character> charList = new ArrayList<>();
        for (String str : strings) {
            char[] chars = str.toCharArray();
            for (char c : chars) {
                charList.add(c);
            }
        }
        // 使用随机数生成器对列表中的字符进行随机排序
        Collections.shuffle(charList, new Random());
        // 将随机排序后的字符组合成一个新的字符串
        StringBuilder mixedString = new StringBuilder();
        for (char c : charList) {
            mixedString.append(c);
        }
        return mixedString.toString();
    }

    /**
     * 根据已有的一组字符串，生成随机字符串
     *
     * @param strings 已有的一组字符串
     * @return 随机字符串
     */
    public static String getRandomStr(String... strings) {
        String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        UUID uuid = UUID.randomUUID();
        String randomPart = uuid.toString().replaceAll("-", "");
        // 将randomPart、str以及strings合并成一个新的数组
        String[] combinedStrings = new String[strings.length + 2];
        combinedStrings[0] = randomPart;
        combinedStrings[1] = str;
        System.arraycopy(strings, 0, combinedStrings, 2, strings.length);
        // 将合并后的数组作为参数传递给randomMixStrings方法
        return randomMixStrings(combinedStrings);
    }

    /**
     * 获取剩余时间，不足一天的部分按一天计算
     *
     * @param currentTime 当前时间戳
     * @param endTime     结束时间戳
     * @return 剩余天数
     */
    public static int calculateRemainingDays(long currentTime, long endTime) {
        // 将时间戳转换为Instant
        Instant currentInstant = Instant.ofEpochMilli(currentTime);
        Instant endInstant = Instant.ofEpochMilli(endTime);
        // 将Instant转换为LocalDate，以便计算剩余天数
        LocalDate currentDate = LocalDate.ofInstant(currentInstant, ZoneId.systemDefault());
        LocalDate endDate = LocalDate.ofInstant(endInstant, ZoneId.systemDefault());
        // 计算剩余的天数，并向上取整
        return (int) (Duration.between(currentDate.atStartOfDay(), endDate.atStartOfDay()).toDays() + 1);
    }


}
