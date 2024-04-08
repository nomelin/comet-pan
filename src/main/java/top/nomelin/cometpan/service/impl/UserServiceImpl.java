package top.nomelin.cometpan.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.nomelin.cometpan.common.Constants;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.enums.Role;
import top.nomelin.cometpan.common.exception.BusinessException;
import top.nomelin.cometpan.dao.UserMapper;
import top.nomelin.cometpan.pojo.Account;
import top.nomelin.cometpan.pojo.User;
import top.nomelin.cometpan.service.UserService;
import top.nomelin.cometpan.util.TokenUtil;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {


    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 新增
     */
    @Override
    public void add(User user) {
        if (ObjectUtil.isNull(user.getUserName())) {
            throw new BusinessException(CodeMessage.USER_NAME_NULL_ERROR);//用户名不能为空
        }
        User dbUser = userMapper.selectByUsername(user.getUserName());
        if (ObjectUtil.isNotNull(dbUser)) {
            throw new BusinessException(CodeMessage.USER_NAME_EXIST_ERROR);//用户名已存在
        }
        if (ObjectUtil.isEmpty(user.getPassword())) {
            user.setPassword(Constants.DEFAULT_PASSWORD);//默认密码
        }
        if (ObjectUtil.isEmpty(user.getName())) {//如果没有设置昵称，则默认昵称是用户名
            user.setName(user.getUserName());//默认昵称是用户名
        }
        user.setRole(Role.USER.roleCode);//默认角色是普通用户
        userMapper.insert(user);//插入数据库
    }

    /**
     * 删除
     */
    @Override
    public void deleteById(Integer id) {
        userMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            userMapper.deleteById(id);
        }
    }

    /**
     * 修改
     */
    @Override
    public void updateById(User user) {
        userMapper.updateById(user);
    }

    /**
     * 根据ID查询
     */
    @Override
    public User selectById(Integer id) {
        return userMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    @Override
    public List<User> selectAll(User user) {
        return userMapper.selectAll(user);
    }

    /**
     * 分页查询
     */
    @Override
    public PageInfo<User> selectPage(User user, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> list = userMapper.selectAll(user);
        return PageInfo.of(list);
    }

    /**
     * 登录
     */
    @Override
    public Account login(Account account) {
        // 根据用户名查询数据库中的用户信息
        Account dbUser = userMapper.selectByUsername(account.getUserName());
        // 如果数据库中不存在该用户，则抛出用户不存在异常
        if (ObjectUtil.isNull(dbUser)) {
            throw new BusinessException(CodeMessage.USER_NOT_EXIST_ERROR);
        }
        // 如果输入的密码与数据库中的密码不匹配，则抛出账号错误异常
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new BusinessException(CodeMessage.USER_ACCOUNT_ERROR);
        }
        // 生成token，格式为用户ID-角色名
        String tokenData = dbUser.getId() + "-" + Role.ADMIN.name();
        String token = TokenUtil.createToken(tokenData, dbUser.getPassword());
        // 将生成的token设置到用户对象中
        dbUser.setToken(token);
        // 返回登录成功的用户信息
        return dbUser;
    }


    /**
     * 注册
     */
    @Override
    public void register(Account account) {
        User user = new User();
        BeanUtils.copyProperties(account, user);
        add(user);
    }

    /**
     * 修改密码
     */
    @Override
    public void updatePassword(Account account, String newPassword) {
        User dbUser = userMapper.selectByUsername(account.getUserName());
        if (ObjectUtil.isNull(dbUser)) {
            throw new BusinessException(CodeMessage.USER_NOT_EXIST_ERROR);
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new BusinessException(CodeMessage.PARAM_PASSWORD_ERROR);//原密码输入错误
        }
        dbUser.setPassword(newPassword);
        userMapper.updateById(dbUser);
    }

}
