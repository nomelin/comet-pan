package top.nomelin.cometpan.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.nomelin.cometpan.cache.CurrentUserCache;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.exception.BusinessException;
import top.nomelin.cometpan.dao.FileMapper;
import top.nomelin.cometpan.dao.UserMapper;
import top.nomelin.cometpan.pojo.FileMeta;
import top.nomelin.cometpan.pojo.User;
import top.nomelin.cometpan.service.FileService;
import top.nomelin.cometpan.util.Util;

import java.util.*;

/**
 * 网盘文件信息表业务处理
 **/
@Service
public class FileServiceImpl implements FileService {
    private final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    private final FileMapper fileMapper;
    private final UserMapper userMapper;

    @Autowired
    public FileServiceImpl(FileMapper fileMapper, UserMapper userMapper, CurrentUserCache currentUserCache) {
        this.fileMapper = fileMapper;
        this.userMapper = userMapper;
    }


    /**
     * 新增
     */
    @Override
    public int add(FileMeta fileMeta) {
        fileMapper.insert(fileMeta);
        return fileMeta.getId();
    }

    /**
     * 得到用户所用的空间
     */
    @Override
    public int getUsedSpace(Integer userId) {
        User user = userMapper.selectById(userId);
        FileMeta root = fileMapper.selectById(user.getRootId());
        return root.getSize();

    }

    @Transactional
    @Override
    public void setDeleteNode(Integer id) {
        FileMeta root = fileMapper.selectById(id);
        updateSizeById(id, false);// 更新全部父目录大小
        updateTimeById(id);// 更新全部父目录时间
        if (!root.getFolder()) { // 如果不是文件夹，直接删除然后返回
            setDeleteById(id);
            return;
        }
        //bfs遍历删除，相当于层序遍历
        Queue<FileMeta> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            FileMeta current = queue.poll();
            setDeleteById(current.getId());
            List<FileMeta> children = selectAllByParentFolderId(current.getId());
            for (FileMeta child : children) {
                if (child.getFolder()) { // 如果是文件夹，将其加入队列
                    queue.offer(child);
                }
                setDeleteById(child.getId());// 不管是文件还是文件夹，都删除子节点
            }
        }
    }

    public void cancelDeleteById(Integer id) {
        FileMeta fileMeta = new FileMeta();
        fileMeta.setId(id);
        fileMeta.setDelete(false);
        fileMapper.updateById(fileMeta);
    }

    @Transactional
    @Override
    public void cancelDeleteNode(Integer id) {
        FileMeta root = fileMapper.selectById(id);
        updateSizeById(id, true);// 更新全部父目录大小
        updateTimeById(id);// 更新全部父目录时间
        if (!root.getFolder()) {
            cancelDeleteById(id);
            return;
        }
        Queue<FileMeta> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            FileMeta current = queue.poll();
            cancelDeleteById(current.getId());
            List<FileMeta> children = selectAllByParentFolderId(current.getId());
            for (FileMeta child : children) {
                if (child.getFolder()) { // 如果是文件夹，将其加入队列
                    queue.offer(child);
                }
                cancelDeleteById(child.getId());
            }
        }
    }

    @Transactional
    @Override
    public void deleteNode(Integer id) {
        FileMeta root = fileMapper.selectById(id);
        //删除首先要标记删除，所以不用再更新父目录大小了，因为已经更新过了
        //不用更新时间，因为删除回收站文件不会改变时间
        if (!root.getDelete()) {
            throw new BusinessException(CodeMessage.INVALID_DELETE_ERROR);//如果文件没有被标记删除，则不能删除
        }
        if (!root.getFolder()) { // 如果不是文件夹，直接删除然后返回
            logger.info("delete file id: " + id);
            deleteById(id);
            return;
        }
        //bfs遍历删除，相当于层序遍历
        List<Integer> ids = new ArrayList<>();//直接删除会导致记录已经删除，导致查询不到子节点，所以需要记录一下id
        Queue<FileMeta> queue = new LinkedList<>();
        queue.offer(root);
        logger.info("delete folder id: " + id);
        while (!queue.isEmpty()) {
            FileMeta current = queue.poll();
            if (ObjectUtil.isNull(current)) {
                throw new BusinessException(CodeMessage.UNKNOWN_ERROR);
            }
            ids.add(current.getId());
            List<FileMeta> children = selectAllByParentFolderId(current.getId());
            for (FileMeta child : children) {
                if (child.getFolder()) { // 如果是文件夹，将其加入队列
                    queue.offer(child);
                }
                ids.add(child.getId());// 不管是文件还是文件夹，都删除子节点
            }
        }
        logger.info("delete file ids: " + ids);
        deleteBatch(ids);
    }

    /**
     * 重命名
     *
     * @param id       文件ID
     * @param fullName 带有后缀的文件名
     */
    @Transactional
    @Override
    public void updateName(Integer id, String fullName) {
        FileMeta fileMeta = selectById(id);
        if (ObjectUtil.isNull(fileMeta) || fileMeta.getDelete()) {
            throw new BusinessException(CodeMessage.INVALID_FILE_ID_ERROR);
        }
        if (ObjectUtil.isNotNull(fullName) && StrUtil.isEmpty(fullName)) {
            throw new BusinessException(CodeMessage.INVALID_NAME_ERROR);
        }
        logger.info("update file id: " + id + " name: " + fullName);
        String oldName = fileMeta.getName();
        if (!fileMeta.getFolder()) {
            oldName = oldName + "." + fileMeta.getType();
        }
        if (StrUtil.equals(oldName, fullName)) {
            return;//如果文件名没有变化，则直接返回
        }
        if (fileMeta.getFolder()) {
            // 如果是文件夹,不需要考虑类型
            fullName = checkSameNameAndUpdate(fullName, fileMeta.getFolderId(), true);
            fileMeta.setName(fullName);
            fileMeta.setPath(fileMeta.getPath().substring(0, fileMeta.getPath().lastIndexOf("/")) + "/" + fullName);
            fileMapper.updateById(fileMeta);
        } else {
            String newType = Util.getType(fullName);
            String newName = Util.removeType(fullName);
            newName = checkSameNameAndUpdate(newName, fileMeta.getFolderId(), false);
            fileMeta.setName(newName);
            fileMeta.setPath(fileMeta.getPath().substring(0, fileMeta.getPath().lastIndexOf("/"))
                    + "/" + newName + "." + newType);
            fileMeta.setType(newType);
            fileMapper.updateById(fileMeta);
        }
        updateTimeById(id);// 更新父目录时间
    }

    @Transactional
    @Override
    public int addFile(String fileName, Integer parentFolderId, int size, String type) {
        FileMeta file = new FileMeta();
        fileName = checkSameNameAndUpdate(fileName, parentFolderId, false);
        file.setName(fileName);
        file.setFolderId(parentFolderId);
        file.setSize(size);
        file.setType(type);
        FileMeta parent = fileMapper.selectById(parentFolderId);
        if (parent == null) {
            throw new BusinessException(CodeMessage.PARENT_FOLDER_NOT_EXIST_ERROR);
        }
        if (!parent.getFolder()) {
            throw new BusinessException(CodeMessage.PARENT_IS_NOT_FOLDER_ERROR);
        }
        if (parent.getDelete()) {
            throw new BusinessException(CodeMessage.INVALID_FILE_ID_ERROR);
        }
        file.setPath(parent.getFolderId() == 0 ? "/" + fileName + "." + type
                : parent.getPath() + "/" + fileName + "." + type);
        int userId = parent.getUserId();
        User user = userMapper.selectById(userId);
        file.setUserId(userId);
        file.setFolder(false);
        file.setRootFolderId(user.getRootId());
        String time = String.valueOf(System.currentTimeMillis());
        file.setCreateTime(time);
        file.setUpdateTime(time);
        fileMapper.insert(file);
        updateSizeById(file.getId(), true);// 更新全部父目录大小
        updateTimeById(file.getId());// 更新全部父目录时间
        return file.getId();
    }

    /**
     * 更新节点大小,直到根目录
     *
     * @param id  节点ID
     * @param add true为增加，false为减少
     */
    @Transactional
    @Override
    public void updateSizeById(Integer id, boolean add) {
        FileMeta fileMeta = new FileMeta();
        fileMeta.setId(id);
        fileMeta = fileMapper.selectById(id);
        int size = fileMeta.getSize();
        if (fileMeta.getFolderId() == 0) {
            throw new BusinessException(CodeMessage.INVALID_FILE_ID_ERROR);
        }

        int folderId;
        while (true) {
            folderId = fileMeta.getFolderId();
            if (folderId == 0) {
                break; // 到达根目录
            }
            fileMeta = fileMapper.selectById(folderId); // 获取父目录
            int newSize = add ? fileMeta.getSize() + size : fileMeta.getSize() - size;
            FileMeta updatedFolder = new FileMeta(); // 创建一个新的对象来保存更新后的父目录信息
            updatedFolder.setId(fileMeta.getId());
            updatedFolder.setSize(newSize);
            fileMapper.updateById(updatedFolder); // 更新父目录
        }
    }

    @Transactional
    @Override
    public void updateTimeById(Integer id) {
        FileMeta fileMeta = new FileMeta();
        fileMeta.setId(id);
        String time = String.valueOf(System.currentTimeMillis());
        fileMeta = fileMapper.selectById(id);
        if (fileMeta.getFolderId() == 0) {
            throw new BusinessException(CodeMessage.INVALID_FILE_ID_ERROR);
        }
        int folderId;
        while (true) {
            fileMeta.setUpdateTime(time);
            fileMapper.updateById(fileMeta); // 更新目录
            folderId = fileMeta.getFolderId();
            if (folderId == 0) {
                break; // 到达根目录
            }
            fileMeta = fileMapper.selectById(folderId); // 获取父目录
        }
    }

    /**
     * 检查同名文件或文件夹
     *
     * @param fileName       文件名或文件夹名（不包括后缀）
     * @param parentFolderId 父文件夹ID
     * @param isFolder       是否为文件夹
     * @return 修改后的名字，加上(1)或(2)等后缀
     */
    public String checkSameNameAndUpdate(String fileName, Integer parentFolderId, boolean isFolder) {
        List<FileMeta> fileMetas = selectAllByParentFolderId(parentFolderId);
        int num = 0;
        boolean hasSameName = false;
        // 找到同名文件或文件夹
        for (FileMeta fileMeta : fileMetas) {
            if (fileMeta.getName().equals(fileName) && fileMeta.getFolder().equals(isFolder)) {
                hasSameName = true;
            }
            // 同名文件或文件夹
            if (fileMeta.getName().startsWith(fileName) && fileMeta.getFolder().equals(isFolder)) {
                String ends = fileMeta.getName().substring(fileName.length());
                // 如果字符串不为空，判断是否有(1)或(2)等后缀
                if (!StrUtil.isEmpty(ends) && ends.charAt(0) == '(' && ends.charAt(ends.length() - 1) == ')') {
                    ends = ends.substring(1, ends.length() - 1);
                    //如果是数字，则取最大值
                    if (Util.isNumber(ends)) {
                        num = Math.max(num, Integer.parseInt(ends));
                    }
                }
            }
        }
        if (!hasSameName) {
            return fileName;// 没有同名文件或文件夹
        }
        if (num > 0) {
            return fileName + "(" + (num + 1) + ")";// 有多次同名文件或文件夹，后缀数字增加
        } else {
            return fileName + "(1)";// 仅有同名文件或文件夹加上(1)
        }

    }

    @Transactional
    @Override
    public int addFolder(String folderName, Integer parentFolderId) {
        FileMeta folder = new FileMeta();
        folder.setFolderId(parentFolderId);
        FileMeta parent = fileMapper.selectById(parentFolderId);
        if (parent == null) {
            throw new BusinessException(CodeMessage.PARENT_FOLDER_NOT_EXIST_ERROR);
        }
        if (!parent.getFolder()) {
            throw new BusinessException(CodeMessage.PARENT_IS_NOT_FOLDER_ERROR);
        }
        if (parent.getDelete()) {
            throw new BusinessException(CodeMessage.INVALID_FILE_ID_ERROR);
        }
        folderName = checkSameNameAndUpdate(folderName, parentFolderId, true);
        folder.setName(folderName);
        // 路径为父路径加上文件夹名
        folder.setPath(parent.getFolderId() == 0 ? "/" + folderName : parent.getPath() + "/" + folderName);
        int userId = parent.getUserId();
        User user = userMapper.selectById(userId);
        folder.setUserId(userId);
        folder.setRootFolderId(user.getRootId());
        String time = String.valueOf(System.currentTimeMillis());
        folder.setCreateTime(time);
        folder.setUpdateTime(time);
        fileMapper.insert(folder);
        updateTimeById(folder.getId());// 更新父目录时间
        //不更新父目录大小，因为文件夹大小为0
        return folder.getId();
    }

    /**
     * 根据父文件夹ID查询文件，文件夹
     */
    @Override
    public List<FileMeta> selectByParentFolderId(Integer parentFolderId) {
        FileMeta fileMeta = new FileMeta();
        fileMeta.setFolderId(parentFolderId);
        fileMeta.setDelete(false);
        fileMeta.setUserId(fileMapper.selectById(parentFolderId).getUserId());// 限制用户只能看到自己的文件
        return fileMapper.selectAll(fileMeta);
    }


    public List<FileMeta> selectAllByParentFolderId(Integer parentFolderId) {
        FileMeta fileMeta = new FileMeta();
        fileMeta.setFolderId(parentFolderId);
        fileMeta.setUserId(fileMapper.selectById(parentFolderId).getUserId());// 限制用户只能看到自己的文件
        return fileMapper.selectAll(fileMeta);
    }


    /**
     * 根据父文件夹ID分页查询文件，文件夹
     */
    @Override
    @Deprecated(forRemoval = true)
    public PageInfo<FileMeta> selectPagesByFolderId(Integer folderId, Integer pageNum, Integer pageSize) {
        List<FileMeta> list = selectAllByParentFolderId(folderId);
        return Util.listToPage(list, pageNum, pageSize);
    }


    /**
     * 新增根目录
     * 返回根目录ID
     */
    @Override
    public int addRoot(Integer userId) {
        FileMeta root = new FileMeta();
        root.setUserId(userId);
        root.setFolderId(0);
        root.setPath("/");
        String time = String.valueOf(System.currentTimeMillis());
        root.setCreateTime(time);
        root.setUpdateTime(time);
        add(root);
        return root.getId();
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        fileMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            fileMapper.deleteById(id);
        }
    }

    @Override
    public void setDeleteById(Integer id) {
        FileMeta fileMeta = new FileMeta();
        fileMeta.setId(id);
        fileMeta.setDelete(true);
        fileMapper.updateById(fileMeta);
    }

    @Override
    public void setDeleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            setDeleteById(id);
        }
    }

    /**
     * 修改
     */
    public void updateById(FileMeta diskFiles) {
        fileMapper.updateById(diskFiles);
    }

    /**
     * 根据ID查询
     */
    public FileMeta selectById(Integer id) {
        return fileMapper.selectById(id);
    }

    /**
     * 查询所有,不包括删除的文件
     */
    public List<FileMeta> selectAll(FileMeta fileMeta) {
        fileMeta.setDelete(false);
        return fileMapper.selectAll(fileMeta);
    }

    /**
     * 查询回收站文件,只查询垃圾森林的根节点,不查询出垃圾文件夹嵌套的子节点。
     */
    @Override
    public List<FileMeta> selectAllTrash(Integer rootId) {
        FileMeta fileMeta = new FileMeta();
        fileMeta.setRootFolderId(rootId);
        fileMeta.setDelete(true);
        List<FileMeta> list = fileMapper.selectAll(fileMeta);
        Iterator<FileMeta> iterator = list.iterator();
        while (iterator.hasNext()) {
            FileMeta file = iterator.next();
            FileMeta parent = fileMapper.selectById(file.getFolderId());
            if (parent != null && parent.getDelete()) {// 如果父节点是垃圾文件，则删除当前节点
                iterator.remove(); // 使用迭代器的 remove 方法来安全地删除元素
            }
        }
        return list;
    }

    /**
     * 分页查询
     */
    public PageInfo<FileMeta> selectPage(FileMeta diskFiles, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<FileMeta> list = fileMapper.selectAll(diskFiles);
        return PageInfo.of(list);
    }

}
