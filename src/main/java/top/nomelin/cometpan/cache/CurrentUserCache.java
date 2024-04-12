package top.nomelin.cometpan.cache;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import top.nomelin.cometpan.pojo.User;

/**
 * 缓存当前用户信息
 *
 * @author nomelin
 */
//TODO 因为是session，所以需要在用户退出后清空缓存，否则会导致登录别的用户时获取到上次的用户信息。直接调用clear方法即可
@Component
@SessionScope
public class CurrentUserCache {
    private User currentUser = null;

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void clear() {
        this.currentUser = null;
    }
}
