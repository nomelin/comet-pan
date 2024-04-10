package top.nomelin.cometpan.dao;

import org.apache.ibatis.annotations.Mapper;
import top.nomelin.cometpan.pojo.FileMeta;

import java.util.List;

@Mapper
public interface FileMapper {

    /**
     * 新增
     */
    int insert(FileMeta fileMeta);

    /**
     * 删除一个回收站文件
     */
    int deleteById(Integer id);

    /**
     * 批量删除一个用户的所有回收站文件
     */
    int deleteBatch(Integer userId);

    /**
     * 修改
     */
    int updateById(FileMeta diskFiles);

    /**
     * 根据ID查询
     */
    FileMeta selectById(Integer id);

    /**
     * 查询所有
     */
    List<FileMeta> selectAll(FileMeta diskFiles);
}
