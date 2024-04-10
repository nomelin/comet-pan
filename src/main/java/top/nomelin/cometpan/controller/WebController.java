package top.nomelin.cometpan.controller;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import top.nomelin.cometpan.common.Result;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.enums.Role;
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


    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public WebController(UserService userService) {
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
        int role = account.getRole();
        if (ObjectUtil.isEmpty(account.getUserName()) || ObjectUtil.isEmpty(account.getPassword())
                || ObjectUtil.isNull(role)) {
            return Result.error(CodeMessage.PARAM_LOST_ERROR);
        }
        if (Role.ADMIN.roleCode == account.getRole()) {// 管理员登录
            account = userService.login(account);
        } else if (Role.USER.roleCode == account.getRole()) {// 用户登录
            account = userService.login(account);
        } else {
            return Result.error(CodeMessage.UNKNOWN_ERROR);
        }
        if (role != account.getRole()) {
            logger.info("用户权限不匹配：{}", account);
            return Result.error(CodeMessage.ROLE_ERROR);
        }
        logger.info("用户登录成功：{}", account);
        return Result.success(account);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody Account account) {
        if (StrUtil.isBlank(account.getUserName()) || StrUtil.isBlank(account.getPassword())
                || ObjectUtil.isEmpty(account.getRole())) {
            return Result.error(CodeMessage.PARAM_LOST_ERROR);
        }
        User user;
        if (Role.ADMIN.roleCode == account.getRole() || Role.USER.roleCode == account.getRole()) {
            user = userService.register(account);
        } else {
            return Result.error(CodeMessage.UNKNOWN_ERROR);
        }
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
        logger.info("更改密码请求：userName:{},password:{},newPassword:{}", userName, password, newPassword);
        if (StrUtil.isBlank(userName) || StrUtil.isBlank(password)
                || ObjectUtil.isNull(newPassword) || StrUtil.isBlank(newPassword)) {
            return Result.error(CodeMessage.PARAM_LOST_ERROR);
        }
        Account account = new Account();
        account.setUserName(userName);
        account.setPassword(password);
        userService.updatePassword(account, newPassword);// 修改密码
        return Result.success();
    }

}

