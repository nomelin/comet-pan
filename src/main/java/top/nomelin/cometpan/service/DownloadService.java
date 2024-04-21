package top.nomelin.cometpan.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface DownloadService {


    void download(Integer id, Boolean isAttachment, HttpServletRequest request, HttpServletResponse response);
}
