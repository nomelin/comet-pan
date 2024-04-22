package top.nomelin.cometpan.dao;

import org.apache.ibatis.annotations.Mapper;
import top.nomelin.cometpan.pojo.Share;

import java.util.List;

/**
 * 操作share相关数据接口
 */
@Mapper
public interface ShareMapper {

    /**
     * 新增
     */
    int insert(Share share);

    /**
     * 删除
     */
    int deleteById(Integer id);

    /**
     * 修改
     */
    int updateById(Share share);

    /**
     * 根据ID查询
     */
    Share selectById(Integer id);

    /**
     * 查询所有
     */
    List<Share> selectAll(Share share);


}
