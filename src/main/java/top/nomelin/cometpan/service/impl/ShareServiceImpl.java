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
    public int createShare(String name, List<Integer> fileIds, String password, Integer days) {
        if (ObjectUtil.isNull(fileIds) || ObjectUtil.isEmpty(fileIds)) {
            throw new BusinessException(CodeMessage.PARAM_ERROR);
        }
        if (ObjectUtil.isNull(name) || StrUtil.isEmpty(name)) {
            name = "未命名";
        }
        if (ObjectUtil.isNull(password) || StrUtil.isEmpty(password)) {
            password = "1234";
        }
        if (ObjectUtil.isNull(days) || days <= 0) {
            days = 7;
        }
        for (int fileId : fileIds) {
            FileMeta fileMeta = fileMapper.selectById(fileId);
            if (Objects.equals(fileMeta.getUserId(), currentUserCache.getCurrentUser().getId())) {
                throw new BusinessException(CodeMessage.INVALID_FILE_ID_ERROR);
            }
        }
        Share share = new Share();
        share.setFileIds(Util.getArrayStr(fileIds));
        share.setCode(password);
        share.setName(name);
        String time = String.valueOf(System.currentTimeMillis());
        share.setShareTime(time);
        share.setEndTime(time + days * 24 * 60 * 60 * 1000);
        FileMeta fileMeta = fileMapper.selectById(fileIds.get(0));
        share.setUserId(fileMeta.getUserId());
        int id = shareMapper.insert(share);
        share.setPath(getSharePath(id));
        shareMapper.updateById(share);
        return id;
    }

    @Override
    public Share selectByPath(String path) {
        int id = getIdBySharePath(path);
        if (id == -1) {
            return null;
        }
        Share share = shareMapper.selectById(id);
        long currentTime = System.currentTimeMillis();
        long endTime = Long.parseLong(share.getEndTime());
        share.setLeftDays(Util.calculateRemainingDays(currentTime, endTime));
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
        List<Share> shares = shareMapper.selectAll(share);
        for (Share s : shares) {
            long currentTime = System.currentTimeMillis();
            long endTime = Long.parseLong(s.getEndTime());
            s.setLeftDays(Util.calculateRemainingDays(currentTime, endTime));
        }
        return shares;
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
        return Util.getRandomStr() + "a" + id;
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
