package top.nomelin.cometpan.controller;

import org.springframework.web.bind.annotation.*;
import top.nomelin.cometpan.cache.CurrentUserCache;
import top.nomelin.cometpan.common.Result;
import top.nomelin.cometpan.pojo.Share;
import top.nomelin.cometpan.service.ShareService;
import top.nomelin.cometpan.util.Util;

import java.util.List;

@RestController
@RequestMapping("/share")
public class ShareController {
    private final ShareService shareService;


    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @GetMapping("/{path}")
    public Result selectShare(@PathVariable String path) {
        return Result.success(shareService.selectByPath(path));
    }

    @GetMapping("/user/{userId}")
    public Result selectByUserId(@PathVariable int userId) {
        return Result.success(shareService.selectByUserId(userId));
    }

    @PostMapping("/")
    public Result addShare(@RequestBody Share share) {
        int id = shareService.createShare(share.getName(), Util.getArrayInt(share.getFileIds()),
                share.getCode(), share.getLeftDays());
        return Result.success(id);
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

}
