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
import top.nomelin.cometpan.common.enums.CacheType;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.exception.BusinessException;
import top.nomelin.cometpan.common.exception.SystemException;
import top.nomelin.cometpan.dao.FileMapper;
import top.nomelin.cometpan.dao.UserMapper;
import top.nomelin.cometpan.interfaces.DoubleCache;
import top.nomelin.cometpan.pojo.FileMeta;
import top.nomelin.cometpan.pojo.User;
import top.nomelin.cometpan.service.DiskService;
import top.nomelin.cometpan.service.FileService;
import top.nomelin.cometpan.util.SpringBeanUtil;
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
    private final DiskService diskService;


    @Autowired
    public FileServiceImpl(FileMapper fileMapper, UserMapper userMapper,
                           CurrentUserCache currentUserCache, DiskService diskService) {
        this.fileMapper = fileMapper;
        this.userMapper = userMapper;
        this.diskService = diskService;

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
    public Long getUsedSpace(Integer userId) {
        User user = userMapper.selectById(userId);
        FileMeta root = fileMapper.selectById(user.getRootId());
        return root.getSize();

    }

    @Transactional
    @Override
    public void setDeleteNode(Integer id) {
        FileService bean = SpringBeanUtil.getBean(FileService.class);
        if (ObjectUtil.isNull(bean)) {
            throw new SystemException(CodeMessage.BEAN_ERROR);
        }
        FileMeta root = fileMapper.selectById(id);
        bean.updateSizeById(id, false);// 更新全部父目录大小
        bean.updateTimeById(id);// 更新全部父目录时间
        if (!root.getFolder()) { // 如果不是文件夹，直接删除然后返回
            bean.setDeleteById(id);
            return;
        }
        //bfs遍历删除，相当于层序遍历
        Queue<FileMeta> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            FileMeta current = queue.poll();
            bean.setDeleteById(current.getId());
            List<FileMeta> children = selectAllByParentFolderId(current.getId());
            for (FileMeta child : children) {
                if (child.getFolder()) { // 如果是文件夹，将其加入队列
                    queue.offer(child);
                }
                bean.setDeleteById(child.getId());// 不管是文件还是文件夹，都删除子节点
            }
        }
    }

    @Override
    public void cancelDeleteById(Integer id) {
        FileMeta fileMeta = new FileMeta();
        fileMeta.setId(id);
        fileMeta.setDelete(false);
        fileMapper.updateById(fileMeta);
    }

    @Transactional
    @Override
    public void cancelDeleteNode(Integer id) {
        FileService bean = SpringBeanUtil.getBean(FileService.class);
        if (ObjectUtil.isNull(bean)) {
            throw new SystemException(CodeMessage.BEAN_ERROR);
        }
        FileMeta root = fileMapper.selectById(id);
        bean.updateSizeById(id, true);// 更新全部父目录大小
        bean.updateTimeById(id);// 更新全部父目录时间
        if (!root.getFolder()) {
            bean.cancelDeleteById(id);
            return;
        }
        Queue<FileMeta> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            FileMeta current = queue.poll();
            bean.cancelDeleteById(current.getId());
            List<FileMeta> children = selectAllByParentFolderId(current.getId());
            for (FileMeta child : children) {
                if (child.getFolder()) { // 如果是文件夹，将其加入队列
                    queue.offer(child);
                }
                bean.cancelDeleteById(child.getId());
            }
        }
    }

    @Transactional
    @Override
    public void deleteNode(Integer id) {
        FileService bean = SpringBeanUtil.getBean(FileService.class);
        if (ObjectUtil.isNull(bean)) {
            throw new SystemException(CodeMessage.BEAN_ERROR);
        }
        FileMeta root = fileMapper.selectById(id);
        //删除首先要标记删除，所以不用再更新父目录大小了，因为已经更新过了
        //不用更新时间，因为删除回收站文件不会改变时间
        if (!root.getDelete()) {
            throw new BusinessException(CodeMessage.INVALID_DELETE_ERROR);//如果文件没有被标记删除，则不能删除
        }
        if (!root.getFolder()) { // 如果不是文件夹，直接删除然后返回
            logger.info("delete file id: " + id);
            bean.deleteById(id);
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
        bean.deleteBatch(ids);
    }

    /**
     * 重命名
     *
     * @param id       文件ID
     * @param fullName 带有后缀的文件名
     */

    @Override
    public void updateName(Integer id, String fullName) {
        FileService bean = SpringBeanUtil.getBean(FileService.class);
        if (ObjectUtil.isNull(bean)) {
            throw new SystemException(CodeMessage.BEAN_ERROR);
        }
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
            fullName = bean.checkSameNameAndUpdate(fullName, fileMeta.getFolderId(), true);
            fileMeta.setName(fullName);
            fileMeta.setPath(fileMeta.getPath().substring(0, fileMeta.getPath().lastIndexOf("/")) + "/" + fullName);
            fileMapper.updateById(fileMeta);
            fileMeta = fileMapper.selectById(fileMeta.getFolderId());//父文件夹路径
            bean.updateSubFolderPath(id, fileMeta.getPath(), fullName);//更新子文件夹路径
        } else {
            String newType = Util.getType(fullName);
            String newName = Util.removeType(fullName);
            newName = bean.checkSameNameAndUpdate(newName, fileMeta.getFolderId(), false);
            fileMeta.setName(newName);
            fileMeta.setPath(fileMeta.getPath().substring(0, fileMeta.getPath().lastIndexOf("/"))
                    + "/" + Util.getFullName(newName, newType));
            fileMeta.setType(newType);
            fileMapper.updateById(fileMeta);
        }
        bean.updateTimeById(id);// 更新父目录时间
    }

    /**
     * 写入文件数据库信息
     *
     * @param fileName       文件名，全名，包含后缀
     * @param parentFolderId 父文件夹ID
     * @param size           文件大小，单位字节
     * @param disk_id        磁盘文件ID
     * @return 插入的文件ID
     */
    @Transactional
    @Override
    public int addFile(String fileName, Integer parentFolderId, Long size, int disk_id) {
        FileService bean = SpringBeanUtil.getBean(FileService.class);
        if (ObjectUtil.isNull(bean)) {
            throw new SystemException(CodeMessage.BEAN_ERROR);
        }
        logger.info("add file name: " + fileName + " parentFolderId: " + parentFolderId + " size: " + size);
        String name = Util.removeType(fileName);
        String type = Util.getType(fileName);
        name = bean.checkSameNameAndUpdate(name, parentFolderId, false);
        FileMeta file = new FileMeta();
        logger.info("add file name: " + name + " type: " + type);
        file.setName(name);//不包括后缀
        file.setFolderId(parentFolderId);
        file.setSize(size);//TODO 重构数据库文件size从int到long
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
        file.setPath(parent.getFolderId() == 0 ? "/" + Util.getFullName(name, type)
                : parent.getPath() + "/" + Util.getFullName(name, type));
        int userId = parent.getUserId();
        User user = userMapper.selectById(userId);
        file.setUserId(userId);
        file.setFolder(false);
        file.setRootFolderId(user.getRootId());
        String time = String.valueOf(System.currentTimeMillis());
        file.setCreateTime(time);
        file.setUpdateTime(time);
        file.setDiskId(disk_id);
        fileMapper.insert(file);
//        logger.info("增加大小:" + size + "，文件id:" + file.getId());
        bean.updateSizeById(file.getId(), true);// 更新全部父目录大小
        bean.updateTimeById(file.getId());// 更新全部父目录时间
        return file.getId();
    }

    /**
     * 从这个节点的父文件夹开始，更新节点大小,直到根目录
     * 更新的大小就是这个节点的大小
     *
     * @param id  节点ID
     * @param add true为增加，false为减少
     */
    @Transactional
    @Override
    public void updateSizeById(Integer id, boolean add) {
        FileMeta fileMeta = fileMapper.selectById(id);
        Long size = fileMeta.getSize();
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
            Long newSize = add ? fileMeta.getSize() + size : fileMeta.getSize() - size;
            FileMeta updatedFolder = new FileMeta(); // 创建一个新的对象来保存更新后的父目录信息
            updatedFolder.setId(fileMeta.getId());
            updatedFolder.setSize(newSize);
            fileMapper.updateById(updatedFolder); // 更新父目录
        }
    }

    /**
     * 从这个节点开始（包括），更新节点时间，直到根目录
     */
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


    @Transactional
    @Override
    public int addFolder(String folderName, Integer parentFolderId) {
        FileService bean = SpringBeanUtil.getBean(FileService.class);
        if (ObjectUtil.isNull(bean)) {
            throw new SystemException(CodeMessage.BEAN_ERROR);
        }
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
        folderName = bean.checkSameNameAndUpdate(folderName, parentFolderId, true);
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
        bean.updateTimeById(folder.getId());// 更新父目录时间
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

    @Transactional
    @Override
    public List<FileMeta> selectAllByParentFolderId(Integer parentFolderId) {
        FileMeta fileMeta = new FileMeta();
        fileMeta.setFolderId(parentFolderId);
        fileMeta.setUserId(fileMapper.selectById(parentFolderId).getUserId());// 限制用户只能看到自己的文件
        return fileMapper.selectAll(fileMeta);
    }

    /**
     * 移动节点（文件或文件夹）到目标文件夹
     *
     * @param id             节点ID
     * @param targetFolderId 目标文件夹ID
     */
    @Transactional
    @Override
    public void moveNode(Integer id, Integer targetFolderId) {
        FileService bean = SpringBeanUtil.getBean(FileService.class);
        if (ObjectUtil.isNull(bean)) {
            throw new SystemException(CodeMessage.BEAN_ERROR);
        }
        FileMeta fileMeta = selectById(id);
        if (ObjectUtil.isNull(fileMeta) || fileMeta.getFolderId() == 0 || fileMeta.getDelete()) {
            throw new BusinessException(CodeMessage.INVALID_FILE_ID_ERROR);
        }
        if (ObjectUtil.equals(targetFolderId, id) || ObjectUtil.equals(targetFolderId, fileMeta.getFolderId())) {
            throw new BusinessException(CodeMessage.SAME_FOLDER_ERROR);
            // 如果目标文件夹和当前文件夹相同或是其父文件夹，则不能移动
        }
        FileMeta targetFolder = fileMapper.selectById(targetFolderId);
        if (ObjectUtil.isNull(targetFolder) || targetFolder.getDelete()) {
            throw new BusinessException(CodeMessage.INVALID_FILE_ID_ERROR);
        }
        if (!targetFolder.getFolder()) {
            throw new BusinessException(CodeMessage.PARENT_IS_NOT_FOLDER_ERROR);
        }
        if (!ObjectUtil.equals(targetFolder.getUserId(), fileMeta.getUserId())) {
            throw new BusinessException(CodeMessage.CANNOT_ACCESS_ERROR); // 如果目标文件夹不是自己的
        }
        checkSubFolder(id, targetFolderId);
        bean.updateSizeById(id, false);// 更新当前节点的父目录大小
        bean.updateTimeById(id);// 更新旧目录时间
        fileMeta.setFolderId(targetFolderId);
        String newName = bean.checkSameNameAndUpdate(fileMeta.getName(), targetFolderId, fileMeta.getFolder());
        fileMeta.setName(newName);// 更新文件名
        fileMapper.updateById(fileMeta);// 更新节点
        bean.updateSubFolderPath(id, targetFolder.getPath(), newName);// 如果有重名，更新所有子节点的路径
        bean.updateSizeById(id, true);// 更新目标文件夹的父目录大小
        bean.updateTimeById(id);// 更新新目录时间
    }


    @Transactional
    @Override
    public void updateSubFolderPath(Integer folderId, String path, String newName) {
        FileService bean = SpringBeanUtil.getBean(FileService.class);
        if (ObjectUtil.isNull(bean)) {
            throw new SystemException(CodeMessage.BEAN_ERROR);
        }
        FileMeta fileMeta = fileMapper.selectById(folderId);
        if (ObjectUtil.isNull(fileMeta) || fileMeta.getFolderId() == 0 || fileMeta.getDelete()) {
            throw new BusinessException(CodeMessage.INVALID_FILE_ID_ERROR);
        }
        if (ObjectUtil.isNull(path) || !path.startsWith("/")) {
            throw new BusinessException(CodeMessage.PARAM_ERROR);
        }
//        String oldName = fileMeta.getName();
        if (path.equals("/")) {
            path = "";// 根目录特殊处理
        }
        // 如果是文件，则只更新自己的路径
        if (!fileMeta.getFolder()) {
            fileMeta.setPath(path + "/" + Util.getFullName(newName, fileMeta.getType()));// 更新文件路径
            fileMapper.updateById(fileMeta);// 更新当前文件路径
            return;
        }
        // 当前文件夹的完整路径
        path = path + "/" + newName;
        fileMeta.setPath(path);
        fileMapper.updateById(fileMeta);// 更新当前文件夹路径
        List<FileMeta> children = selectByParentFolderId(folderId);// 获取当前文件夹的子节点，不包括删除的文件
        for (FileMeta child : children) {
            if (child.getFolder()) {
                bean.updateSubFolderPath(child.getId(), path, child.getName());// 递归更新子文件夹路径
            } else {
                child.setPath(path + "/" + Util.getFullName(child.getName(), child.getType()));// 更新文件路径
                fileMapper.updateById(child);
            }
        }
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
    @Transactional
    @Override
    public void deleteById(Integer id) {
        FileMeta fileMeta = fileMapper.selectById(id);
        if (ObjectUtil.isNull(fileMeta) || fileMeta.getFolderId() == 0) {
            throw new BusinessException(CodeMessage.INVALID_FILE_ID_ERROR);
        }

        if (!fileMeta.getFolder()) {
            diskService.decDiskCount(fileMeta.getDiskId());
        }
        fileMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    @Transactional
    @Override
    public void deleteBatch(List<Integer> ids) {
        Set<Integer> idsSet = new HashSet<>(ids);
        for (Integer id : idsSet) {
            FileMeta fileMeta = fileMapper.selectById(id);
            if (ObjectUtil.isNull(fileMeta) || fileMeta.getFolderId() == 0) {
                throw new BusinessException(CodeMessage.INVALID_FILE_ID_ERROR);
            }
            if (!fileMeta.getFolder()) {
                diskService.decDiskCount(fileMeta.getDiskId());
            }
            fileMapper.deleteById(id);
        }
    }

    @Transactional
    @Override
    public void setDeleteById(Integer id) {
        FileMeta fileMeta = new FileMeta();
        fileMeta.setId(id);
        fileMeta.setDelete(true);
        fileMapper.updateById(fileMeta);
    }

    @Transactional
    @Override
    public void setDeleteBatch(List<Integer> ids) {
        FileService bean = SpringBeanUtil.getBean(FileService.class);
        if (ObjectUtil.isNull(bean)) {
            throw new SystemException(CodeMessage.BEAN_ERROR);
        }
        Set<Integer> idsSet = new HashSet<>(ids);
        for (Integer id : idsSet) {
            bean.setDeleteById(id);
        }
    }

    /**
     * 修改
     */
    @Override
    public void updateById(FileMeta diskFiles) {
        fileMapper.updateById(diskFiles);
    }

    /**
     * 根据ID查询
     */
    @Override
    public FileMeta selectById(Integer id) {
        return fileMapper.selectById(id);
    }

    /**
     * 查询所有,不包括删除的文件
     */
    @Override
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
        logger.info(list.toString());
        while (iterator.hasNext()) {
            FileMeta file = iterator.next();
            FileMeta parent = fileMapper.selectById(file.getFolderId());
            if (parent != null && parent.getDelete()) {// 如果父节点是垃圾文件，则删除当前节点
                iterator.remove(); // 使用迭代器的 remove 方法来安全地删除元素
            }
        }
        logger.info(list.toString());
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

    /**
     * 检查同名文件或文件夹,没有重名，则返回原名字，有重名，则加上(1)或(2)等后缀
     *
     * @param fileName       文件名或文件夹名（不包括后缀）
     * @param parentFolderId 父文件夹ID
     * @param isFolder       是否为文件夹
     * @return 修改后的名字，加上(1)或(2)等后缀
     */
    @Override
    @Transactional
    public String checkSameNameAndUpdate(String fileName, Integer parentFolderId, boolean isFolder) {
        FileService bean = SpringBeanUtil.getBean(FileService.class);
        if (ObjectUtil.isNull(bean)) {
            throw new SystemException(CodeMessage.BEAN_ERROR);
        }
        List<FileMeta> fileMetas = bean.selectAllByParentFolderId(parentFolderId);
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

    /**
     * 复制节点（文件或文件夹）到目标文件夹
     * 可以是不同用户之间复制,所以可以用于分享文件
     *
     * @param id             节点ID
     * @param targetFolderId 目标文件夹ID
     */
    @Override
    @Transactional
    public void copyNode(Integer id, Integer targetFolderId) {
        FileService bean = SpringBeanUtil.getBean(FileService.class);
        if (ObjectUtil.isNull(bean)) {
            throw new SystemException(CodeMessage.BEAN_ERROR);
        }
        FileMeta fileMeta = selectById(id);
        FileMeta targetFolder = fileMapper.selectById(targetFolderId);
        if (ObjectUtil.isNull(fileMeta) || fileMeta.getFolderId() == 0 || fileMeta.getDelete()
                || ObjectUtil.isNull(targetFolder) || targetFolder.getDelete()) {
            throw new BusinessException(CodeMessage.INVALID_FILE_ID_ERROR);
        }
        if (!targetFolder.getFolder()) {
            throw new BusinessException(CodeMessage.PARENT_IS_NOT_FOLDER_ERROR);
        }
        //如果目标文件夹是当前文件夹的子文件夹，则不能复制
        checkSubFolder(id, targetFolderId);//只读,不需要事务

        //复制前先检查重名并且记录
        String newName = bean.checkSameNameAndUpdate(fileMeta.getName(), targetFolderId, fileMeta.getFolder());

        //递归复制以后,父目录大小和时间没有更新,子目录路径没有更新,文件重名没有更新
        int newId = bean.copyNodeMethod(id, targetFolderId);
        //更新父目录大小和时间
        bean.updateSizeById(newId, true);
        bean.updateTimeById(newId);

        FileMeta newFileMeta = selectById(newId);
        newFileMeta.setName(newName);
        fileMapper.updateById(newFileMeta);// 更新原文件名

        bean.updateSubFolderPath(newId, targetFolder.getPath(), newName);// 更新所有子节点的路径,同时处理重名
    }

    /**
     * 复制节点的递归方法,不要直接使用此方法
     *
     * @param id             节点ID
     * @param parentFolderId 要复制到的父文件夹ID
     * @return 复制的新节点ID
     */
    @Transactional
    @Override
    public int copyNodeMethod(Integer id, Integer parentFolderId) {
        FileService bean = SpringBeanUtil.getBean(FileService.class);
        if (ObjectUtil.isNull(bean)) {
            throw new SystemException(CodeMessage.BEAN_ERROR);
        }
        // 复制文件或文件夹
        FileMeta newFileMeta = new FileMeta();
        FileMeta old = selectById(id);
        FileMeta parent = fileMapper.selectById(parentFolderId);
        Integer newUserId = parent.getUserId();
        newFileMeta.setUserId(newUserId);
        newFileMeta.setFolderId(parentFolderId);
        if (parent.getFolderId() == 0) {
            newFileMeta.setRootFolderId(parent.getId());//根目录的根目录就是自己
        } else {
            newFileMeta.setRootFolderId(parent.getRootFolderId());
        }
        newFileMeta.setSize(old.getSize());
        newFileMeta.setFolder(old.getFolder());
        newFileMeta.setName(old.getName());
        newFileMeta.setType(old.getType());

        String time = String.valueOf(System.currentTimeMillis());
        newFileMeta.setCreateTime(time);
        newFileMeta.setUpdateTime(old.getUpdateTime());//更新时间还是旧文件的时间,因为复制的是旧文件
        newFileMeta.setDiskId(old.getDiskId());
        newFileMeta.setPath(old.getPath());//路径在全部复制后统一更新
        fileMapper.insert(newFileMeta);// 插入新节点,并获取新节点ID
        if (old.getFolder()) {
            // 复制文件夹
            List<FileMeta> children = selectByParentFolderId(id);
            for (FileMeta child : children) {
                bean.copyNodeMethod(child.getId(), newFileMeta.getId());
            }
        } else {
            // 复制文件
            diskService.incDiskCount(old.getDiskId());
        }
        return newFileMeta.getId();
    }

    /**
     * 检查目标文件夹是否是子文件夹或是相同文件夹
     */
    private void checkSubFolder(Integer id, Integer targetFolderId) {
        if (id.equals(targetFolderId)) {
            throw new BusinessException(CodeMessage.SAME_FOLDER_ERROR);
        }
        while (true) {
            FileMeta target = fileMapper.selectById(targetFolderId);
            if (ObjectUtil.isNull(target) || target.getDelete()) {
                throw new BusinessException(CodeMessage.INVALID_FILE_ID_ERROR);
            }
            if (target.getFolderId() == 0) {
                break; // 到达根目录
            }
            if (ObjectUtil.equals(target.getFolderId(), id)) {
                throw new BusinessException(CodeMessage.SUB_FOLDER_ERROR); // 如果目标文件夹是当前文件夹的子文件夹
            }
            targetFolderId = target.getFolderId();
        }
    }

}
