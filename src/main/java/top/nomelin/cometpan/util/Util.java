package top.nomelin.cometpan.util;


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


}
