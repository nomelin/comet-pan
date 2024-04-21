package top.nomelin.cometpan.service;

import jakarta.servlet.http.HttpServletResponse;

public interface DownloadService {


    void download(Integer id, Boolean isAttachment, HttpServletResponse response);
}
