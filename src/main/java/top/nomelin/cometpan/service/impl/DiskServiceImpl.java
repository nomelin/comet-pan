package top.nomelin.cometpan.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.nomelin.cometpan.common.Constants;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.exception.BusinessException;
import top.nomelin.cometpan.service.DiskService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
public class DiskServiceImpl implements DiskService {
    private static final Logger logger = LoggerFactory.getLogger(DiskServiceImpl.class);

    @Override
    public void uploadAvatar(Resource resource, int userId) throws IOException {
        String filename = resource.getFilename();
        if (Objects.isNull(filename)) {
            throw new BusinessException(CodeMessage.FILE_UPLOAD_ERROR);
        }
        // 获取文件名和后缀名
        filename = StringUtils.cleanPath(filename);
        String extension = filename.substring(filename.lastIndexOf("."));
        if (extension.equals(".jpg") || extension.equals(".png") || extension.equals(".jpeg")) {
            extension = ".jpg";
        } else {
            throw new BusinessException(CodeMessage.INVALID_AVATAR_ERROR);
        }
        // 创建目录
        Path directory = Paths.get(Constants.AVATAR_FOLDER);
        Path filePath = directory.resolve(userId + extension);
        logger.info("filepath:"+ filePath);
        Files.copy(resource.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);// 上传文件, 覆盖已存在的文件
    }

    @Override
    public void uploadFile(Resource resource) {

    }

    @Override
    public void deleteFile(String filePath) {

    }

    @Override
    public void downloadFile(String filePath, OutputStream outputStream) {

    }

    @Override
    public void updateFile(String filePath, Resource resource) {

    }
}
