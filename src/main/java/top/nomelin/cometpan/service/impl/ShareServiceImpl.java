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
        if (id == -1) {
            return null;
        }
        //        long currentTime = System.currentTimeMillis();
//        long endTime = Long.parseLong(share.getEndTime());
//        share.setLeftDays(Util.calculateRemainingDays(currentTime, endTime));
        Share share = shareMapper.selectById(id);
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


    private String getSharePath(int id) {
        String str = Util.getRandomStr() + "a" + id;
        return str.substring(str.length() - 50);//取最后50位作为路径
    }

    private int getIdBySharePath(String path) {
        int index = path.lastIndexOf("a");
        if (index == -1) {
            return -1;
        }
        String idStr = path.substring(index + 1);
        try {
            return Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
