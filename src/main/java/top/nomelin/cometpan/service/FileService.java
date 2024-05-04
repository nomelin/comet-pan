package top.nomelin.cometpan.service;


import com.github.pagehelper.PageInfo;
import org.springframework.transaction.annotation.Transactional;
import top.nomelin.cometpan.pojo.FileMeta;

import java.util.List;

public interface FileService {

    /**
     * 新增
     */
    int add(FileMeta fileMeta);

    /**
     * 检查同名文件或文件夹,没有重名，则返回原名字，有重名，则加上(1)或(2)等后缀
     *
     * @param fileName       文件名或文件夹名（不包括后缀）
     * @param parentFolderId 父文件夹ID
     * @param isFolder       是否为文件夹
     * @return 修改后的名字，加上(1)或(2)等后缀
     */
    String checkSameNameAndUpdate(String fileName, Integer parentFolderId, boolean isFolder);

    /**
     * 在当前用户空间新增文件夹
     */
    int addFolder(String folderName, Integer parentFolderId);


    void updateName(Integer id, String name);


    /**
     * 在当前用户空间新增文件
     */

    int addFile(String fileName, Integer parentFolderId, Long size, int disk_id);

    /**
     * 从这个节点的父文件夹开始，更新节点大小,直到根目录
     * 更新的大小就是这个节点的大小
     *
     * @param id  节点ID
     * @param add true为增加，false为减少
     */

    void updateSizeById(Integer id, boolean add);

    /**
     * 从这个节点开始（包括），更新节点updateTime时间，直到根目录
     */
    void updateTimeById(Integer id);


    void moveNode(Integer id, Integer targetFolderId);

    /**
     * 更新所有子文件夹的路径。自动处理根节点（只有一个/，以/结尾）的特殊情况。
     * 例如：将/demo/folder1移动到/下，则变成/folder1，/demo/folder1/file1.txt的路径变成/folder1/file1.txt，依此类推。
     * 又如，/demo/folder1移动到/folder2下，则变成/folder2/folder1，
     * /demo/folder1/file1.txt的路径变成/folder2/folder1/file1.txt，依此类推。
     * 同时，如果要更改此文件夹的名字，请传入newName参数，会自动更改所有子节点路径。
     * 否则，传入原名
     * <p>
     * 注意,不会更新根节点的文件名,但是会更新路径
     *
     * @param folderId 要更新的文件树的根目录ID, 【也可以是一个文件】
     * @param path     要更新到的路径，不包括根目录文件名，以/开头。
     * @param newName  要更新的文件名，如果为原名，则不更新文件名。
     */
    void updateSubFolderPath(Integer folderId, String path, String newName);

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

    Long getUsedSpace(Integer userId);

    /**
     * 标记删除节点，包括子节点
     */
    void setDeleteNode(Integer id);

    void cancelDeleteById(Integer id);

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

    List<FileMeta> selectAllTrash(Integer rootId);

    /**
     * 分页查询
     */
    PageInfo<FileMeta> selectPage(FileMeta fileMeta, Integer pageNum, Integer pageSize);


    List<FileMeta> selectAllByParentFolderId(Integer parentFolderId);

    @Transactional
    void copyNode(Integer id, Integer targetFolderId);

    @Transactional
    int copyNodeMethod(Integer id, Integer parentFolderId);
}
