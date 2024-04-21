package top.nomelin.cometpan.service.impl;

import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.exception.BusinessException;
import top.nomelin.cometpan.dao.DiskMapper;
import top.nomelin.cometpan.pojo.DiskFile;
import top.nomelin.cometpan.service.DiskService;
import top.nomelin.cometpan.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
@Transactional
public class DiskServiceImpl implements DiskService {
    private static final Logger logger = LoggerFactory.getLogger(DiskServiceImpl.class);
    private final DiskMapper diskMapper;
    @Value("${avatar.folder}")
    private String AVATAR_FOLDER;

    public DiskServiceImpl(DiskMapper diskMapper) {
        this.diskMapper = diskMapper;
    }

    @Override
    public void uploadAvatar(Resource resource, int userId) throws IOException {
        String filename = resource.getFilename();
        if (Objects.isNull(filename)) {
            throw new BusinessException(CodeMessage.FILE_UPLOAD_ERROR);
        }
        // 获取文件名和后缀名
        filename = StringUtils.cleanPath(filename);
        String extension = Util.getType(filename);
        if (StrUtil.equals(extension, "jpg") || StrUtil.equals(extension, "png") || StrUtil.equals(extension, "jpeg")) {
            extension = ".jpg";
        } else {
            throw new BusinessException(CodeMessage.INVALID_AVATAR_ERROR);
        }
        // 创建目录
        Path directory = Paths.get(AVATAR_FOLDER);
        Path filePath = directory.resolve(userId + extension);
        logger.info("filepath:" + filePath);
        Files.copy(resource.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);// 上传文件, 覆盖已存在的文件
    }

    /**
     * 将一个disk的引用计数减1，当引用计数为0时，删除该disk记录,同时删除磁盘文件
     *
     * @param diskId disk id
     */
    @Override
    @Transactional
    public void decDiskCount(int diskId) {
        DiskFile diskFile = diskMapper.selectById(diskId);
        if (Objects.isNull(diskFile)) {
            throw new BusinessException(CodeMessage.INVALID_DISK_ID_ERROR);
        }
        logger.info("diskId:" + diskId + " count:" + diskFile.getCount() + "->" + (diskFile.getCount() - 1));
        if (diskFile.getCount() == 1) {
            logger.info("删除磁盘文件：" + diskFile.getPath());
            // 删除磁盘文件
            File file = new File(diskFile.getPath());
            if (file.exists()) {
                if (!file.delete()) {
                    logger.warn("删除磁盘文件失败：" + diskFile.getPath());
                }
                // 删除[md5]文件夹，如果文件夹为空，则删除
                File parentFile = file.getParentFile();
                if (isEmptyDirectory(parentFile)) {
                    if (!parentFile.delete()) {
                        logger.warn("删除磁盘文件夹失败：" + diskFile.getPath());
                    }
                }
            }
            // 删除数据库记录
            diskMapper.deleteById(diskId);
        } else {
            // 引用计数减1
            diskFile.setCount(diskFile.getCount() - 1);
            diskMapper.updateById(diskFile);
        }
    }

    @Override
    public void incDiskCount(int diskId) {
        DiskFile diskFile = diskMapper.selectById(diskId);
        if (Objects.isNull(diskFile)) {
            throw new BusinessException(CodeMessage.INVALID_DISK_ID_ERROR);
        }
        logger.info("diskId:" + diskId + " count:" + diskFile.getCount() + "->" + (diskFile.getCount() + 1));
        diskFile.setCount(diskFile.getCount() + 1);
        diskMapper.updateById(diskFile);
    }

    // 判断文件夹是否为空
    private boolean isEmptyDirectory(File directory) {
        if (directory.isDirectory()) {
            String[] files = directory.list();
            return files == null || files.length == 0;
        }
        return false;
    }

}
