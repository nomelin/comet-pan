package top.nomelin.cometpan.service;


import com.github.pagehelper.PageInfo;
import top.nomelin.cometpan.pojo.FileMeta;

import java.util.List;

public interface FileService {

    /**
     * 新增
     */
    void add(FileMeta fileMeta);

    /**
     * 新增根目录
     * 返回根目录ID
     */
    int addRoot(Integer userId);

    /**
     * 删除
     */
    void deleteById(Integer id);

    /**
     * 批量删除
     */
    void deleteBatch(List<Integer> ids);

    /**
     * 标记删除
     */
    void setDeleteById(Integer id);

    /**
     * 标记批量删除
     */
    void setDeleteBatch(List<Integer> ids);

    /**
     * 修改
     */
    void updateById(FileMeta fileMeta);

    /**
     * 根据ID查询
     */
    FileMeta selectById(Integer id);

    /**
     * 查询所有
     */
    List<FileMeta> selectAll(FileMeta fileMeta);

    /**
     * 分页查询
     */
    PageInfo<FileMeta> selectPage(FileMeta fileMeta, Integer pageNum, Integer pageSize);


}
