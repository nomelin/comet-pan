package top.nomelin.cometpan.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import top.nomelin.cometpan.common.Constants;
import top.nomelin.cometpan.common.Result;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.service.DiskService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class DiskController {
    private final Logger logger = LoggerFactory.getLogger(DiskController.class);
    private final DiskService diskService;

    public DiskController(DiskService diskService) {
        this.diskService = diskService;
    }

    @GetMapping("/avatar/{userid}")
    public ResponseEntity<Resource> getUserAvatar(@PathVariable("userid") String userId) {
        // 构建头像文件路径
        Path avatarPath = Paths.get(Constants.AVATAR_FOLDER, userId + ".jpg"); //

        // 检查头像文件是否存在
        if (!Files.exists(avatarPath)) {
            avatarPath = Paths.get(Constants.AVATAR_FOLDER, "default.jpg");//默认头像文件
        }
        logger.info("avatarPath: " + avatarPath);
        Resource resource = new FileSystemResource(avatarPath);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // 设置响应内容类型
                .body(resource);
    }

    @PostMapping("/avatar/{userid}")
    public Result updateUserAvatar(@PathVariable("userid") int userId, @RequestParam("file") MultipartFile file) throws IOException {
        // 检查文件是否为空
        if (file.isEmpty()) {
            return Result.error(CodeMessage.FILE_UPLOAD_ERROR);
        }
        logger.info("userId: " + userId + "更新头像");
        Resource resource = file.getResource();
        diskService.uploadAvatar(resource, userId);
        logger.info("userId: "+userId+"头像上传成功");
        return Result.success(CodeMessage.SUCCESS);
    }


}
