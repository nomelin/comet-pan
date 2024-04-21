package top.nomelin.cometpan.service.impl;

import cn.hutool.core.util.ObjectUtil;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.exception.SystemException;
import top.nomelin.cometpan.dao.DiskMapper;
import top.nomelin.cometpan.pojo.DiskFile;
import top.nomelin.cometpan.pojo.FileMeta;
import top.nomelin.cometpan.service.DownloadService;
import top.nomelin.cometpan.service.FileService;
import top.nomelin.cometpan.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
     * 获取URL编码后的原始文件名
     *
     * @param request          ：客户端请求
     * @param originalFileName ：原始文件名
     * @return ：
     */

    private static String getEncodedFilename(HttpServletRequest request, String originalFileName) {
        String encodedFilename = null;
        String agent = request.getHeader("User-Agent");
        if (agent.contains("MSIE")) {
            // IE浏览器
            encodedFilename = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8);
            encodedFilename = encodedFilename.replace("+", " ");
        } else if (agent.contains("Firefox")) {
            // 火狐浏览器
            encodedFilename = "=?utf-8?B?" + Base64.getEncoder().encodeToString(originalFileName.getBytes(StandardCharsets.UTF_8)) + "?=";
        } else {
            // 其他浏览器
            encodedFilename = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8);
        }
        return encodedFilename;
    }


    /**
     * 下载文件
     */
    @Override
    public void download(Integer id, Boolean isAttachment, HttpServletRequest request, HttpServletResponse response) {
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
        String encodedFilename = getEncodedFilename(request, Util.getFullName(fileMeta.getName(), fileMeta.getType()));

        //设置Access-Control-Expose-Headers避免前端调用获取Content-Disposition出现Refused to get unsafe header异常
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        response.addHeader("Content-Disposition",
                attachment + ";filename=" + encodedFilename);
        response.setContentType("application/octet-stream");
        response.setContentLength(Math.toIntExact(diskFile.getLength()));
        logger.info("download file: " + path);
        FileInputStream fileInputStream = null;
        ServletOutputStream outputStream = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                throw new SystemException(CodeMessage.NOT_FOUND_ERROR);
            }
            //获取文件输入流
            fileInputStream = new FileInputStream(file);
            //创建数据缓冲区
            byte[] buffers = new byte[1024];
            //通过response中获取ServletOutputStream输出流
            outputStream = response.getOutputStream();
            int length;
            while ((length = fileInputStream.read(buffers)) > 0) {
                //写入到输出流中
                outputStream.write(buffers, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new SystemException(CodeMessage.DOWNLOAD_FILE_ERROR);
        } finally {
            //流的关闭
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
