package top.nomelin.cometpan.controller;

import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.nomelin.cometpan.common.Constants;
import top.nomelin.cometpan.common.Result;
import top.nomelin.cometpan.pojo.FileMeta;
import top.nomelin.cometpan.service.FileService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 网盘文件前端操作接口
 **/
@RestController
@RequestMapping("/files")
public class FileController {


    private final FileService fileService;
    private final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/files/avatar/{userid}")
    public ResponseEntity<Resource> getUserAvatar(@PathVariable("userid") String userId) throws IOException {
        // 构建头像文件路径
        Path avatarPath = Paths.get(Constants.AVATAR_FOLDER, userId + ".jpg"); // 假设头像文件格式为PNG格式

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


    @PostMapping("")
    private Result add(@RequestBody FileMeta fileMeta) {
        fileService.add(fileMeta);
        return Result.success();
    }



    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Integer id) {
        fileService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        fileService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("")
    public Result updateById(@RequestBody FileMeta fileMeta) {
        fileService.updateById(fileMeta);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    public Result selectById(@PathVariable Integer id) {
        FileMeta fileMeta = fileService.selectById(id);
        return Result.success(fileMeta);
    }

    /**
     * 查询所有
     */
    @GetMapping("")
    private Result selectAll(FileMeta fileMeta) {
        List<FileMeta> list = fileService.selectAll(fileMeta);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    private Result selectPage(FileMeta fileMeta,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<FileMeta> page = fileService.selectPage(fileMeta, pageNum, pageSize);
        return Result.success(page);
    }

}
