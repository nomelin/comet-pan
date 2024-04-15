package top.nomelin.cometpan.service;


import com.github.pagehelper.PageInfo;
import top.nomelin.cometpan.pojo.FileMeta;

import java.util.List;

public interface FileService {

    /**
     * 新增
     */
    int add(FileMeta fileMeta);

    /**
     * 在当前用户空间新增文件夹
     */
    int addFolder(String folderName, Integer parentFolderId);

    /**
     * 在当前用户空间新增文件
     */
    int addFile(String fileName, Integer parentFolderId, int size, String type);

    /**
     * 循环向上更新大小，直到根目录
     *
     * @param id  节点ID
     * @param add true为增加，false为减少
     */

    void updateSizeById(Integer id, boolean add);

    /**
     * 更新“修改时间”
     */
    void updateTimeById(Integer id);

    PageInfo<FileMeta> selectPagesByFolderId(Integer folderId, Integer pageNum, Integer pageSize);

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
     * 标记删除节点，包括子节点
     */
    void setDeleteNode(Integer id);

    /**
     * 取消标记删除节点，包括子节点
     */
    void cancelDeleteNode(Integer id);

    /**
     * 彻底删除节点，包括子节点
     */
    void deleteNode(Integer id);

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
     * 根据父目录ID查询所有子目录
     */
    List<FileMeta> selectByParentFolderId(Integer parentFolderId);

    /**
     * 分页查询
     */
    PageInfo<FileMeta> selectPage(FileMeta fileMeta, Integer pageNum, Integer pageSize);


}
