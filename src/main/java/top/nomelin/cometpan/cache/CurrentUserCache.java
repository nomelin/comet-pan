package top.nomelin.cometpan.cache;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import top.nomelin.cometpan.pojo.User;

/**
 * 缓存当前用户信息
 *
 * @author nomelin
 */
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
