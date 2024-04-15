package top.nomelin.cometpan.controller;

import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.nomelin.cometpan.cache.CurrentUserCache;
import top.nomelin.cometpan.common.Result;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.pojo.FileMeta;
import top.nomelin.cometpan.pojo.User;
import top.nomelin.cometpan.service.FileService;
import top.nomelin.cometpan.service.UserService;

import java.util.List;
import java.util.Objects;

/**
 * 网盘文件前端操作接口
 *
 * @author nomelin
 **/
@RestController
@RequestMapping("/files")
public class FileController {


    private final FileService fileService;
    private final UserService userService;

    private final CurrentUserCache currentUserCache;
    private final Logger logger = LoggerFactory.getLogger(FileController.class);


    @Autowired
    public FileController(FileService fileService, UserService userService, CurrentUserCache currentUserCache) {
        this.fileService = fileService;
        this.userService = userService;
        this.currentUserCache = currentUserCache;
    }


    @PostMapping("/folder")
    public Result addFolder(@RequestBody FileMeta fileMeta) {
        fileService.addFolder(fileMeta.getName(), fileMeta.getFolderId());
        return Result.success();
    }

    /**
     * 获取一个用户的根目录文件,分页
     */
    @GetMapping("/page")
    public Result selectRootFiles(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        User user = currentUserCache.getCurrentUser();
        if (user == null) {
            return Result.error(CodeMessage.USER_NOT_LOGIN_ERROR);
        }
        Integer rootId = user.getRootId();
        logger.info("获取用户:" + user.getId() + "的根目录文件，用户根目录ID: " + rootId);
        PageInfo<FileMeta> pageInfo = fileService.selectPagesByFolderId(rootId, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    @GetMapping("/page/folder/{folderId}")
    public Result selectFolderFiles(@PathVariable Integer folderId,
                                    @RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize){
        User user = currentUserCache.getCurrentUser();
        FileMeta folder = fileService.selectById(folderId);
        if (folder == null) {
            return Result.error(CodeMessage.PARENT_FOLDER_NOT_EXIST_ERROR);
        }
        if (!Objects.equals(folder.getUserId(), user.getId())) {
            return Result.error(CodeMessage.CANNOT_ACCESS_ERROR);
        }
        logger.info("获取用户:" + user.getId() + "的文件夹:" + folder.getName() + "的文件，文件夹ID: " + folderId);
        PageInfo<FileMeta> pageInfo = fileService.selectPagesByFolderId(folderId, pageNum, pageSize);
        return Result.success(pageInfo);
    }


    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Integer id) {
        fileService.setDeleteNode(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        for (Integer id : ids) {
            fileService.setDeleteNode(id);
        }
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
    @GetMapping("/file/{id}")
    public Result selectById(@PathVariable Integer id) {
        FileMeta fileMeta = fileService.selectById(id);
        return Result.success(fileMeta);
    }

//    /**
//     * 查询所有
//     */
//    @GetMapping("")
//    private Result selectAll(FileMeta fileMeta) {
//        List<FileMeta> list = fileService.selectAll(fileMeta);
//        return Result.success(list);
//    }

    /**
     * 分页查询
     */
    @GetMapping("/page/all")
    public Result selectPage(FileMeta fileMeta,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        fileMeta.setUserId(currentUserCache.getCurrentUser().getId());
        PageInfo<FileMeta> page = fileService.selectPage(fileMeta, pageNum, pageSize);
        return Result.success(page);
    }

}
