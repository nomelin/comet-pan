package top.nomelin.cometpan.service;

import com.github.pagehelper.PageInfo;
import top.nomelin.cometpan.pojo.Account;
import top.nomelin.cometpan.pojo.User;

import java.util.List;

public interface UserService {
    int add(User user);

    void deleteById(Integer id);

    void deleteBatch(List<Integer> ids);

    void updateById(User user);

    User selectById(Integer id);

    List<User> selectAll(User user);

    PageInfo<User> selectPage(User user, Integer pageNum, Integer pageSize);

    Account login(Account account);

    User register(Account account);

    void updatePassword(Account account, String newPassword);
}
