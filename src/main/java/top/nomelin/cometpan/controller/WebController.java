package top.nomelin.cometpan.controller;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import top.nomelin.cometpan.cache.CurrentUserCache;
import top.nomelin.cometpan.common.Result;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.pojo.Account;
import top.nomelin.cometpan.pojo.User;
import top.nomelin.cometpan.service.UserService;

/**
 * 基础前端接口
 *
 * @author nomelin
 */
@RestController
public class WebController {
    private final CurrentUserCache currentUserCache;

    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public WebController(CurrentUserCache currentUserCache, UserService userService) {
        this.currentUserCache = currentUserCache;
        this.userService = userService;
    }

    @GetMapping("/")
    public Result hello() {
        return Result.success("访问成功");
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        //int role = account.getRole();
        if (ObjectUtil.isEmpty(account.getUserName()) || ObjectUtil.isEmpty(account.getPassword())) {
            //        || ObjectUtil.isNull(role)) {
            return Result.error(CodeMessage.PARAM_LOST_ERROR);
        }
        account = userService.login(account);
        logger.info("用户登录成功：{}", account);
        return Result.success(account);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody Account account) {
        if (StrUtil.isBlank(account.getUserName()) || StrUtil.isBlank(account.getPassword())) {
            return Result.error(CodeMessage.PARAM_LOST_ERROR);
        }
        User user;
        user = userService.register(account);
        logger.info("注册成功：{}", account);
        return Result.success(user);
    }

    /**
     * 修改密码
     */
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody JSONObject requestBody) {
        String userName = requestBody.getStr("userName");
        String password = requestBody.getStr("password");
        String newPassword = requestBody.getStr("newPassword");
        if (ObjectUtil.isNull(newPassword)) {
            newPassword = requestBody.getStr("new_password");// 兼容前端传参的下划线形式
        }
        if (StrUtil.isBlank(userName) || StrUtil.isBlank(password)
                || ObjectUtil.isNull(newPassword) || StrUtil.isBlank(newPassword)) {
            return Result.error(CodeMessage.PARAM_LOST_ERROR);// 参数缺失
        }
        logger.info("更改密码请求：userName:{},password:{},newPassword:{}", userName, password, newPassword);
        if (!currentUserCache.getCurrentUser().getUserName().equals(userName)) {
            return Result.error(CodeMessage.INVALID_USER_NAME_ERROR);// 非当前用户修改密码
        }
        if (currentUserCache.getCurrentUser().getPassword().equals(newPassword)) {
            return Result.error(CodeMessage.EQUAL_PASSWORD_ERROR);// 新密码不能与旧密码相同
        }
        Account account = new Account();
        account.setUserName(userName);
        account.setPassword(password);
        userService.updatePassword(account, newPassword);// 修改密码
        return Result.success();
    }

}

