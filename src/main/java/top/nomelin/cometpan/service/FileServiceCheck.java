package top.nomelin.cometpan.service;

import org.springframework.transaction.annotation.Transactional;

public interface FileServiceCheck {
    @Transactional
    String checkSameNameAndUpdate(String fileName, Integer parentFolderId, boolean isFolder);
}
