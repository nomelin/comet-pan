package top.nomelin.cometpan.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.nomelin.cometpan.common.Constants;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.enums.Role;
import top.nomelin.cometpan.common.exception.BusinessException;
import top.nomelin.cometpan.dao.FileMapper;
import top.nomelin.cometpan.dao.UserMapper;
import top.nomelin.cometpan.pojo.Account;
import top.nomelin.cometpan.pojo.User;
import top.nomelin.cometpan.service.FileService;
import top.nomelin.cometpan.service.UserService;
import top.nomelin.cometpan.util.TokenUtil;

import java.util.List;

/**
 * @author nomelin
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {


    private final static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserMapper userMapper;
    private final FileService fileService;

    @Autowired
    public UserServiceImpl(UserMapper userMapper, FileMapper fileMapper, FileService fileService) {
        this.userMapper = userMapper;
        this.fileService = fileService;
    }

    /**
     * 新增
     */
    @Override
    public int add(User user) {
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
        int fileId = fileService.addRoot(user.getId());//创建用户目录,id自动获取
        logger.info("新增用户，：" + user.getId() + "，根文件夹ID：" + fileId);
        user.setRootId(fileId);//设置用户目录ID
        updateById(user);//更新用户信息
        return user.getId();
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
        // 生成token，格式为用户ID-角色-时间戳
        // 其中时间戳用于防止重放攻击
        // TODO 没有用，不能防止重放攻击，因为token在有效期内始终不变。必须客户端加上时间戳验证，然后私钥签名。
        String tokenData = dbUser.getId() + "-" + dbUser.getRole();//+ "-" + System.currentTimeMillis();
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
    public User register(Account account) {
        User user = new User();
        BeanUtils.copyProperties(account, user);
        int id = add(user);
        return selectById(id);
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
