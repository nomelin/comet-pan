package top.nomelin.cometpan.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public interface DiskService {
    void uploadAvatar(Resource resource, int userId) throws IOException;

    void decDiskCount(int diskId);
}
