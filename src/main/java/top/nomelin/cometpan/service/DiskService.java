package top.nomelin.cometpan.service;

import java.io.File;
import java.io.OutputStream;

public interface DiskService {
    void uploadAvatar(File file,int userId);

    void uploadFile(File file);

    void deleteFile(File file);

    void downloadFile(String filePath, OutputStream outputStream);

    void changeFile(String filePath, File newFile);
}
