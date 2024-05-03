package top.nomelin.cometpan.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.exception.BusinessException;
import top.nomelin.cometpan.service.DownloadService;

import java.io.IOException;

@Controller
@RequestMapping("/download")
public class DownloadController {
    private final static Logger logger = LoggerFactory.getLogger(DownloadController.class);
    private final DownloadService downloadService;

    public DownloadController(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @PostMapping("/{diskId}/{isAttachment}")
    public void download(@PathVariable Integer diskId, @PathVariable Integer isAttachment, HttpServletRequest request, HttpServletResponse response) {
        logger.info("download diskId: " + diskId);
        if (isAttachment == 1) {
            downloadService.downloadNormal(diskId, true, request, response);
        } else if (isAttachment == 0) {
            downloadService.downloadNormal(diskId, false, request, response);
        } else {
            logger.warn("isAttachment is not 0 or 1");
            throw new BusinessException(CodeMessage.PARAM_ERROR);
        }
    }

    /**
     * 普通文件下载
     */
    @GetMapping("/download")
    public void download(HttpServletRequest request, HttpServletResponse response) throws IOException {
        downloadService.downloadNormal(request, response);
    }

    @GetMapping("/test/{diskId}/{fileId}")
    public ResponseEntity<FileSystemResource> downloadFile(@PathVariable Integer diskId, @PathVariable Integer fileId) throws IOException {
        logger.info("download diskId: " + diskId+", fileId: "+fileId);
        return downloadService.downloadByBrowser(diskId, fileId);
    }
}
