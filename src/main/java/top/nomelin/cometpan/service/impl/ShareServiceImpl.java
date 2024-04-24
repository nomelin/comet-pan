package top.nomelin.cometpan.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.nomelin.cometpan.cache.CurrentUserCache;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.exception.BusinessException;
import top.nomelin.cometpan.dao.FileMapper;
import top.nomelin.cometpan.dao.ShareMapper;
import top.nomelin.cometpan.pojo.FileMeta;
import top.nomelin.cometpan.pojo.Share;
import top.nomelin.cometpan.service.ShareService;
import top.nomelin.cometpan.util.Util;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ShareServiceImpl implements ShareService {
    private static final Logger logger = LoggerFactory.getLogger(ShareServiceImpl.class);
    private final CurrentUserCache currentUserCache;
    private final ShareMapper shareMapper;
    private final FileMapper fileMapper;

    public ShareServiceImpl(CurrentUserCache currentUserCache, ShareMapper shareMapper, FileMapper fileMapper) {
        this.currentUserCache = currentUserCache;
        this.shareMapper = shareMapper;
        this.fileMapper = fileMapper;
    }

    private static String getSharePath(int id) {
        String str = Util.getRandomStr("CDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz00112233445566778899");
        StringBuilder builder = new StringBuilder(str);
        Random random = new Random();
        int insertIndex = random.nextInt(Math.min(40, str.length())); // 获取随机插入位置，
        // 最大为40或原始字符串长度，以防超出原始字符串长度
        builder.insert(insertIndex, "A" + id + "B");
        return builder.substring(0, 50);
    }

    private static int getIdBySharePath(String path) {
        // 匹配A数字B形式的正则表达式
        String regex = "A(\\d+)B";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(path);
        // 查找匹配的数字并返回第一个匹配到的数字
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            // 如果未找到匹配的数字，可以根据实际需求返回一个默认值，或者抛出异常等
            throw new BusinessException(CodeMessage.PARAM_ERROR);
        }
    }


    @Transactional
    @Override
    public Share createShare(String name, List<Integer> fileIds, String password, Integer days) {
        logger.info("创建分享:createShare name:{}, fileIds:{}, password:{}, days:{}", name, fileIds, password, days);
        if (ObjectUtil.isNull(fileIds) || ObjectUtil.isEmpty(fileIds)) {
            throw new BusinessException(CodeMessage.PARAM_ERROR);
        }
        if (ObjectUtil.isNull(name) || StrUtil.isEmpty(name)) {
            name = "未命名";
        }
        for (int fileId : fileIds) {
            FileMeta fileMeta = fileMapper.selectById(fileId);
            if (!Objects.equals(fileMeta.getUserId(), currentUserCache.getCurrentUser().getId())) {
//                logger.info(fileMeta.getUserId() + " " + currentUserCache.getCurrentUser().getId());
                throw new BusinessException(CodeMessage.INVALID_FILE_ID_ERROR);
            }
        }
        Share share = new Share();
        share.setFileIds(Util.getArrayStr(fileIds));
        share.setCode(password);
        share.setName(name);
        long currentTime = System.currentTimeMillis();
        String time = String.valueOf(currentTime);
        share.setShareTime(time);
        share.setEndTime(String.valueOf(currentTime + days * 24 * 60 * 60 * 1000 + 60 * 1000));//多加一分钟
        if (days == -1) {
            share.setEndTime("-1");
        }
        FileMeta fileMeta = fileMapper.selectById(fileIds.get(0));
        share.setUserId(fileMeta.getUserId());
        shareMapper.insert(share);
        share.setPath(getSharePath(share.getId()));
        shareMapper.updateById(share);
        return share;
    }

    @Override
    public Share selectByPath(String path) {
        int id = getIdBySharePath(path);
        Share share = shareMapper.selectById(id);
        if (ObjectUtil.isNull(share)) {
            throw new BusinessException(CodeMessage.SHARE_NOT_EXIST_ERROR);
        }
        autoClean(share.getUserId());//自动清理过期分享
        share = shareMapper.selectById(id);//再次获取,看看是否被删除
        if (ObjectUtil.isNull(share)) {
            throw new BusinessException(CodeMessage.SHARE_NOT_EXIST_ERROR);
        }
        share.setCount(share.getCount() + 1);//如果通过链接访问，则访问次数加1
        shareMapper.updateById(share);
        return share;
    }

    @Override
    public List<Share> selectByUserId(Integer userId) {
        if (ObjectUtil.isNull(userId)) {
            throw new BusinessException(CodeMessage.PARAM_ERROR);
        }
        if (!userId.equals(currentUserCache.getCurrentUser().getId())) {
            throw new BusinessException(CodeMessage.INVALID_USER_NAME_ERROR);
        }
        Share share = new Share();
        share.setUserId(userId);
        //        for (Share s : shares) {
//            long currentTime = System.currentTimeMillis();
//            long endTime = Long.parseLong(s.getEndTime());
//            s.setLeftDays(Util.calculateRemainingDays(currentTime, endTime));
//        }
        return shareMapper.selectAll(share);
    }

    /**
     * 自动清理过期分享
     *
     * @param userId 用户id
     */
    @Override
    public void autoClean(Integer userId) {
        //不必使用事务,因为删除的行之间没关系,不需要还原.
        Share share = new Share();
        share.setUserId(userId);
        List<Share> shares = shareMapper.selectAll(share);
        long currentTime = System.currentTimeMillis();
        for (Share s : shares) {
            long endTime = Long.parseLong(s.getEndTime());
            if (s.getEndTime().equals("-1")) {
                continue;//永久有效的分享不清理
            }
            if (currentTime > endTime) {
                logger.info("自动清理过期分享:autoClean userId:{}, shareId:{}", userId, s.getId());
                shareMapper.deleteById(s.getId());
            }
        }

    }

    @Override
    public void deleteById(Integer id) {
        shareMapper.deleteById(id);
    }

    @Override
    public void deleteBatch(List<Integer> ids) {
        for (int id : ids) {
            shareMapper.deleteById(id);
        }
    }

    @Override
    public void deleteByPath(String path) {
        int id = getIdBySharePath(path);
        if (id == -1) {
            return;
        }
        shareMapper.deleteById(id);
    }
}
