package top.nomelin.cometpan.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import top.nomelin.cometpan.cache.CurrentUserCache;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.enums.Role;
import top.nomelin.cometpan.common.exception.BusinessException;
import top.nomelin.cometpan.common.exception.SystemException;
import top.nomelin.cometpan.pojo.User;

@Component
public class AdminInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(AdminInterceptor.class);
    private final CurrentUserCache currentUserCache;

    public AdminInterceptor(CurrentUserCache currentUserCache) {
        this.currentUserCache = currentUserCache;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取请求中的用户信息
        //Account currentUser = (Account) request.getAttribute("currentUser");
        User currentUser = currentUserCache.getCurrentUser();
        if (currentUser == null) {
            throw new SystemException(CodeMessage.ACCOUNT_CACHE_ERROR);
        }
        int role = currentUser.getRole();
        if (Role.ADMIN.roleCode == role) {
            //logger.info("用户+" + currentUser.getName() + "有 admin权限, 允许访问");
            return true;
        } else if (Role.USER.roleCode == role) {
            logger.warn("用户:" + currentUser.getName() + ",id:"+currentUser.getId()+",没有admin权限,,禁止访问：" + request.getRequestURI());
            throw new BusinessException(CodeMessage.NEED_ADMIN_ERROR);
        } else {
            throw new BusinessException(CodeMessage.UNKNOWN_ERROR);
        }

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
