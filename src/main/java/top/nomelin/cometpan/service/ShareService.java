package top.nomelin.cometpan.service;

import org.springframework.transaction.annotation.Transactional;
import top.nomelin.cometpan.pojo.Share;

import java.util.List;

public interface ShareService {
    @Transactional
    Share createShare(String name, List<Integer> fileIds, String password, Integer days);

    Share selectByPath(String path);

    List<Share> selectByUserId(Integer userId);

    void deleteById(Integer id);

    void deleteBatch(List<Integer> ids);

    void deleteByPath(String path);
}
