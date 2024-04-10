package top.nomelin.cometpan.service;

import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.nomelin.cometpan.pojo.Account;
import top.nomelin.cometpan.pojo.User;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nomelin
 */
@SpringBootTest
class UserServiceTest {
    private final UserService userService;

    @Autowired
    UserServiceTest(UserService userService) {
        this.userService = userService;
    }

    @Test
    void add() {
        User user = new User();
        user.setUserName("哈喽");
        userService.add(user);
    }

    @Test
    void deleteById() {
        userService.deleteById(15);
    }

    @Test
    void deleteBatch() {
        List<Integer> ids = new ArrayList<>();
        userService.deleteBatch(ids);
    }

    @Test
    void updateById() {
        User user = new User();
        user.setId(10);
        user.setName("小米");
        user.setUserName("小米小米小米小米小米小米");
        userService.updateById(user);
    }

    @Test
    void selectById() {
        System.out.println(userService.selectById(1));
    }

    @Test
    void selectAll() {
        User user = new User();
        List<User> users = userService.selectAll(user);
        for (User u : users) {
            System.out.println(u);
        }
    }

    @Test
    void selectPage() {
        User user = new User();
        PageInfo<User> users = userService.selectPage(user, 1, 5);
        System.out.println("总记录数" + users.getTotal());
        System.out.println("总页数" + users.getPages());
        for (User u : users.getList()) {
            System.out.println(u);
        }
        System.out.println("---------------------");
        users = userService.selectPage(user, users.getPages(), 5);
        for (User u : users.getList()) {
            System.out.println(u);
        }
        System.out.println("---------------------");
        users = userService.selectPage(user, users.getPages() + 1, 5);
        for (User u : users.getList()) {
            System.out.println(u);
        }

    }

    @Test
    void login() {
        Account account = new Account();
        account.setUserName("test2");
        account.setPassword("123456");
        System.out.println(userService.login(account));
        account.setUserName("test1");
        account.setPassword("12345678");
        System.out.println(userService.login(account));

    }

    @Test
    void register() {
//        Account account =new Account();
//        account.setUserName("哈哈哈哈哈哈哈");
//        userService.register(account);
    }

    @Test
    void updatePassword() {
        User user = new User();
        user.setUserName("test1");
        user.setPassword("12345678");
        userService.updatePassword(user, "12345678");
    }
}