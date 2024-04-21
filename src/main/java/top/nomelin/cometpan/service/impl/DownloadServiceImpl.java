package top.nomelin.cometpan.service.impl;

import cn.hutool.core.util.ObjectUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.exception.BusinessException;
import top.nomelin.cometpan.common.exception.SystemException;
import top.nomelin.cometpan.dao.DiskMapper;
import top.nomelin.cometpan.pojo.DiskFile;
import top.nomelin.cometpan.pojo.FileMeta;
import top.nomelin.cometpan.service.DownloadService;
import top.nomelin.cometpan.service.FileService;
import top.nomelin.cometpan.util.Util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class DownloadServiceImpl implements DownloadService {
    private final static Logger logger = LoggerFactory.getLogger(DownloadServiceImpl.class);
    private final DiskMapper diskMapper;

    private final FileService fileService;

    public DownloadServiceImpl(DiskMapper diskMapper, FileService fileService) {
        this.diskMapper = diskMapper;
        this.fileService = fileService;
    }

    /**
     * 下载文件
     */
    @Override
    public void download(Integer id, Boolean isAttachment, HttpServletResponse response) {
        if (ObjectUtil.isNull(id)) {
            throw new SystemException(CodeMessage.INVALID_FILE_ID_ERROR);
        }
        FileMeta fileMeta = fileService.selectById(id);
        if (ObjectUtil.isNull(fileMeta)) {
            throw new SystemException(CodeMessage.INVALID_FILE_ID_ERROR);
        }
        DiskFile diskFile = diskMapper.selectById(fileMeta.getDiskId());
        if (ObjectUtil.isNull(diskFile)) {
            throw new SystemException(CodeMessage.INVALID_DISK_ID_ERROR);
        }
        String path = diskFile.getPath();
        // 清空response
        //response.reset(); //这句代码是罪魁祸首，他会清空响应的一些信息，包括全局的跨域配置
        // 设置response的Header
//        response.setCharacterEncoding("UTF-8");

        //Content-Disposition的作用：告知浏览器以何种方式显示响应返回的文件，用浏览器打开还是以附件的形式下载到本地保存
        //attachment表示以附件方式下载   inline表示在线打开   "Content-Disposition: inline; filename=文件名.mp3"
        // filename表示文件的默认名称，因为网络传输只支持URL编码的相关字符，
        // 因此需要将文件名URL编码后进行传输,前端收到后需要反编码才能获取到真正的名称
        String attachment = isAttachment ? "attachment" : "inline";
        response.addHeader("Content-Disposition",
                attachment + ";filename=" + URLEncoder.encode(
                        Util.getFullName(fileMeta.getName(), fileMeta.getType()), StandardCharsets.UTF_8));
        response.setContentType("application/jpg");

        try (OutputStream outputStream = response.getOutputStream()) {
            try (InputStream inputStream = new FileInputStream(path)) {// 文件的存放路径
                byte[] buffer = new byte[1024];
                logger.info("文件路径：" + path);
                int len;
                //从输入流中读取一定数量的字节，并将其存储在缓冲区字节数组中，读到末尾返回-1
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
            }
        } catch (IOException e) {
            logger.warn("文件下载失败");
            throw new BusinessException(CodeMessage.DOWNLOAD_FILE_ERROR);
        }
    }

}
