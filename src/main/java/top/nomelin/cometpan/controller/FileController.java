package top.nomelin.cometpan.controller;

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

import java.util.ArrayList;
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
     * 获取一个用户的根目录文件,不包括回收站的文件
     */
//    @GetMapping("/page")
    @GetMapping("")
    public Result selectRootFiles(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        User user = currentUserCache.getCurrentUser();
        if (user == null) {
            return Result.error(CodeMessage.USER_NOT_LOGIN_ERROR);
        }
        Integer rootId = user.getRootId();
//        logger.info("获取用户:" + user.getId() + "的根目录文件，用户根目录ID: " + rootId);
//        PageInfo<FileMeta> pageInfo = fileService.selectPagesByFolderId(rootId, pageNum, pageSize);
        List<FileMeta> fileMetas = fileService.selectByParentFolderId(rootId);
        return Result.success(fileMetas);
    }

    /**
     * 获取一个文件夹的子文件,不包括回收站的文件
     */

    //    @GetMapping("/page/folder/{folderId}")
    @GetMapping("/folder/{folderId}")
    public Result selectFolderFiles(@PathVariable Integer folderId,
                                    @RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        User user = currentUserCache.getCurrentUser();
        FileMeta folder = fileService.selectById(folderId);
        if (folder == null) {
            return Result.error(CodeMessage.PARENT_FOLDER_NOT_EXIST_ERROR);
        }
        if (!Objects.equals(folder.getUserId(), user.getId())) {
            return Result.error(CodeMessage.CANNOT_ACCESS_ERROR);
        }
//        logger.info("获取用户:" + user.getId() + "的文件夹:" + folder.getName() + "的文件，文件夹ID: " + folderId);
//        PageInfo<FileMeta> pageInfo = fileService.selectPagesByFolderId(folderId, pageNum, pageSize);
        List<FileMeta> fileMetas = fileService.selectByParentFolderId(folderId);
        return Result.success(fileMetas);
    }

    @PutMapping("/rename")
    public Result updateName(@RequestBody FileMeta fileMeta) {
        fileService.updateName(fileMeta.getId(), fileMeta.getName());
        return Result.success();
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

    @PutMapping("/restore/{id}")
    public Result restoreById(@PathVariable Integer id) {
        fileService.cancelDeleteNode(id);
        return Result.success();
    }

    @PutMapping("/restore/batch")
    public Result restoreBatch(@RequestBody List<Integer> ids) {
        for (Integer id : ids) {
            fileService.cancelDeleteNode(id);
        }
        return Result.success();
    }

    @DeleteMapping("/completely/{id}")
    public Result deleteByIdCompletely(@PathVariable Integer id) {
        fileService.deleteNode(id);
        return Result.success();
    }

    @DeleteMapping("/completely/batch")
    public Result deleteBatchCompletely(@RequestBody List<Integer> ids) {
        for (Integer id : ids) {
            fileService.deleteNode(id);
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


    /**
     * 条件查询,不包括回收站的文件
     */
//    @GetMapping("/page/all")
    @GetMapping("/all")
    public Result selectAll(FileMeta fileMeta,
                            @RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize) {
        fileMeta.setUserId(currentUserCache.getCurrentUser().getId());
//        PageInfo<FileMeta> page = fileService.selectPage(fileMeta, pageNum, pageSize);
        List<FileMeta> fileMetas = fileService.selectAll(fileMeta);
        return Result.success(fileMetas);
    }

    @PostMapping("/share/batch")
    public Result selectShareBatch(@RequestBody List<Integer> ids) {
        logger.info("获取共享文件: " + ids);
        List<FileMeta> fileMetas = new ArrayList<>();
        for (Integer id : ids) {
            fileMetas.add(fileService.selectById(id));
        }
        return Result.success(fileMetas);
    }

    @PostMapping("/copy/{id}/{targetFolderId}")
    public Result copyNode(@PathVariable Integer id, @PathVariable Integer targetFolderId){
        fileService.copyNode(id, targetFolderId);
        return Result.success();
    }


    /**
     * 查询回收站文件,只查询垃圾森林的根节点,不查询出垃圾文件夹嵌套的子节点。
     */
    @GetMapping("/trash")
    public Result selectAllTrash() {
        User user = currentUserCache.getCurrentUser();
        return Result.success(fileService.selectAllTrash(user.getRootId()));
    }

    @GetMapping("/space")
    public Result getUsedSpace() {
        return Result.success(fileService.getUsedSpace(currentUserCache.getCurrentUser().getId()));
    }

    @PostMapping("/move/{id}/{targetFolderId}")
    public Result moveNode(@PathVariable Integer id, @PathVariable Integer targetFolderId) {
        fileService.moveNode(id, targetFolderId);
        return Result.success();
    }

}
