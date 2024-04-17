package top.nomelin.cometpan.util;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;

import java.util.List;

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
     *     获取文件类型
     * @param str 文件名称
     * @return 文件类型
     */
    public static String getType(String str){
        if (ObjectUtil.isNull(str)) {
            return null;
        }
        int index = str.lastIndexOf(".");
        if (index == -1) {
            return "";
        }
        return str.substring(index + 1);
    }


}
