package top.nomelin.cometpan.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.nomelin.cometpan.pojo.User;

import java.util.List;

/**
 *
 * @author nomelin
 */
@Mapper
public interface UserMapper {
    /**
     * 新增
     */
    int insert(User user);

    /**
     * 删除
     */
    int deleteById(Integer id);

    /**
     * 修改
     */
    int updateById(User user);

    /**
     * 根据ID查询
     */
    User selectById(Integer id);

    /**
     * 查询所有
     */
    List<User> selectAll(User user);

    @Select("select * from user where user_name = #{userName}")
    User selectByUsername(String userName);
}
