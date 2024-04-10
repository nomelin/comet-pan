package top.nomelin.cometpan.service.impl;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.nomelin.cometpan.dao.FileMapper;
import top.nomelin.cometpan.pojo.FileMeta;
import top.nomelin.cometpan.service.FileService;

import java.util.List;

/**
 * 网盘文件信息表业务处理
 **/
@Service
public class FileServiceImpl implements FileService {


    private final FileMapper fileMapper;

    @Autowired
    public FileServiceImpl(FileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    /**
     * 新增
     */
    public void add(FileMeta diskFiles) {
        fileMapper.insert(diskFiles);
    }

    @Override
    /**
     * 新增根目录
     * 返回根目录ID
     */
    public int addRoot(Integer userId) {
        FileMeta root = new FileMeta();
        root.setUserId(userId);
        root.setFolderId(0);
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
     * 查询所有
     */
    public List<FileMeta> selectAll(FileMeta diskFiles) {
        return fileMapper.selectAll(diskFiles);
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
