package top.nomelin.cometpan.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface DownloadService {


    /**
     * 通过调用浏览器的下载管理器下载文件，可以下载大文件
     */
    ResponseEntity<FileSystemResource> downloadByBrowser(Integer diskId, Integer fileId) throws FileNotFoundException;

    @Deprecated(forRemoval = true)
    void downloadNormal(Integer id, Boolean isAttachment, HttpServletRequest request, HttpServletResponse response);

    @Deprecated(forRemoval = true)
    void downloadNormal(HttpServletRequest request, HttpServletResponse response) throws IOException;

    @Deprecated(forRemoval = true)
    void downloads() throws IOException;
}
