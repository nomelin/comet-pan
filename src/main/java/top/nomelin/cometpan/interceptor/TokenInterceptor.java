package top.nomelin.cometpan.interceptor;


import cn.hutool.core.util.ObjectUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import top.nomelin.cometpan.cache.CurrentUserCache;
import top.nomelin.cometpan.common.Constants;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.exception.BusinessException;
import top.nomelin.cometpan.pojo.User;
import top.nomelin.cometpan.service.UserService;

/**
 * 验证token的拦截器
 *
 * @author nomelin
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TokenInterceptor.class);

    private final UserService userService;
    private final CurrentUserCache currentUserCache;

    @Autowired
    public TokenInterceptor(UserService userService, CurrentUserCache currentUserCache) {
        this.userService = userService;
        this.currentUserCache = currentUserCache;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession();
        logger.info("session id:"+session.getId());
        if(!ObjectUtil.isNull(currentUserCache.getCurrentUser())){
            logger.info("从session缓存bean中取到用户信息，跳过token解析。");
            return true;
        }
        //从http请求的header中获取token
        String token = request.getHeader(Constants.TOKEN);
        if (ObjectUtil.isEmpty(token)) {
            // 如果没拿到，从参数里再拿一次
            token = request.getParameter(Constants.TOKEN);
        }
        if (ObjectUtil.isEmpty(token)) {
            throw new BusinessException(CodeMessage.TOKEN_INVALID_ERROR);// token为空
        }
        // 2. 验证token
        User account;
        int role;
        String userId;
        try {
            // 解析token获取存储的数据
            String userRole = JWT.decode(token).getAudience().get(0);// 获取用户
            userId = userRole.split("-")[0];
            role = Integer.parseInt(userRole.split("-")[1]);
            logger.info("token解析成功，用户id:" + userId + ",角色:" + role);
        } catch (Exception e) {
            throw new BusinessException(CodeMessage.TOKEN_PARSING_ERROR);
        }
        // 根据userId查询数据库
        account = userService.selectById(Integer.valueOf(userId));
        if (ObjectUtil.isNull(account)) {
            throw new BusinessException(CodeMessage.USER_NOT_EXIST_ERROR);
        }
        try {
            // 用户密码加签验证 token
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(account.getPassword())).build();// 构建JWT验证器
            jwtVerifier.verify(token); // 验证token
        } catch (TokenExpiredException e) {
            // Token过期异常处理
            throw new BusinessException(CodeMessage.TOKEN_EXPIRED_ERROR);
        }catch (JWTVerificationException e) {
            throw new BusinessException(CodeMessage.TOKEN_CHECK_ERROR);
        }
        logger.info("token验证通过，用户:" + account);
        //request.setAttribute("currentUser", account);// 将用户信息存储到request中,以便下一个拦截器使用
        currentUserCache.setCurrentUser(account);// 将用户信息存储到缓存中,以便使用
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}