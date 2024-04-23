package top.nomelin.cometpan.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import top.nomelin.cometpan.common.Result;
import top.nomelin.cometpan.pojo.Share;
import top.nomelin.cometpan.service.FileService;
import top.nomelin.cometpan.service.ShareService;
import top.nomelin.cometpan.util.Util;

import java.util.List;

@RestController
@RequestMapping("/share")
public class ShareController {
    private final static Logger logger = LoggerFactory.getLogger(ShareController.class);
    private final ShareService shareService;
    private final FileService fileService;


    public ShareController(ShareService shareService, FileService fileService) {
        this.shareService = shareService;
        this.fileService = fileService;
    }

    @GetMapping("/{path}")
    public Result selectShare(@PathVariable String path) {
        return Result.success(shareService.selectByPath(path));
    }

    @GetMapping("/user/{userId}")
    public Result selectByUserId(@PathVariable int userId) {
        return Result.success(shareService.selectByUserId(userId));
    }

    @PostMapping("")
    public Result addShare(@RequestBody Share share) {
        logger.info(share.toString());
        Share share1 = shareService.createShare(share.getName(), Util.getArrayInt(share.getFileIds()),
                share.getCode(), share.getLeftDays());
        return Result.success(share1);
    }

    @PostMapping("/merge/{targetId}")

    public Result mergeShare(@RequestBody List<Integer> ids, @PathVariable Integer targetId) {
        logger.info("收到合并分享请求: ids:{}, targetId:{}", ids, targetId);
        for (int id : ids) {
            fileService.copyNode(id, targetId);
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result deleteShare(@PathVariable int id) {
        shareService.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("")
    public Result deleteBatchShare(@RequestBody List<Integer> ids) {
        shareService.deleteBatch(ids);
        return Result.success();
    }

    @DeleteMapping("/clean/{userId}")
    public Result autoClean(@PathVariable Integer userId) {
        shareService.autoClean(userId);
        return Result.success();
    }

}
