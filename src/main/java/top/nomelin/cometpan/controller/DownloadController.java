package top.nomelin.cometpan.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.exception.BusinessException;
import top.nomelin.cometpan.service.DownloadService;

@Controller
@RequestMapping("/download")
public class DownloadController {
    private final static Logger logger = LoggerFactory.getLogger(DownloadController.class);
    private final DownloadService downloadService;

    public DownloadController(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @GetMapping("/{diskId}/{isAttachment}")

    public void download(@PathVariable Integer diskId, @PathVariable Integer isAttachment, HttpServletResponse response) {
        logger.info("download diskId: " + diskId);
        if (isAttachment == 1) {
            downloadService.download(diskId, true, response);
        } else if (isAttachment == 0) {
            downloadService.download(diskId, false, response);
        } else {
            logger.warn("isAttachment is not 0 or 1");
            throw new BusinessException(CodeMessage.PARAM_ERROR);
        }
    }
}
