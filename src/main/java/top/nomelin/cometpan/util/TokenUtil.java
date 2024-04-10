package top.nomelin.cometpan.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.nomelin.cometpan.common.Constants;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.exception.BusinessException;
import top.nomelin.cometpan.pojo.Account;
import top.nomelin.cometpan.service.UserService;

import java.util.Date;
import java.util.Objects;

/**
 * Token工具类
 *
 * @author nomelin
 */
@Component
public class TokenUtil {

    private static final Logger log = LoggerFactory.getLogger(TokenUtil.class);
    private static UserService userService;

    /**
     * 生成token
     */
    public static String createToken(String data, String sign) {
        return JWT.create().withAudience(data) // 将 userId-role 保存到 token 里面,作为载荷
                .withIssuer("cometPan")// 设置 token 的签发者
                .withIssuedAt(new Date()) // 设置 token 的签发时间
                .withExpiresAt(DateUtil.offsetHour(new Date(), 2)) // 2小时后token过期
                .sign(Algorithm.HMAC256(sign)); // 以 password 作为 token 的密钥
    }

    /**
     * 获取当前登录的用户信息
     */
    public static Account getCurrentUser() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(
                    RequestContextHolder.getRequestAttributes())).getRequest();// 获取当前请求
            String token = request.getHeader(Constants.TOKEN);  // 获取token
            if (ObjectUtil.isNotEmpty(token)) {
                String userRole = JWT.decode(token).getAudience().get(0);// 获取载荷
                String userId = userRole.split("-")[0];  // 获取用户id
                //String role = userRole.split("-")[1];    // 获取角色
                return userService.selectById(Integer.valueOf(userId));
            }
        } catch (Exception e) {
            log.warn("获取当前用户信息出错", e);
        }
        throw new BusinessException(CodeMessage.USER_NOT_LOGIN);  // 用户未登录
        //return new Account();  // 返回空的账号对象
    }

    @Autowired
    public void setUserService(UserService userService) {
        TokenUtil.userService = userService;
    }
}
