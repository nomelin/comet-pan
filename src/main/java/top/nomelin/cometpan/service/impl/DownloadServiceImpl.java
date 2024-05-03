package top.nomelin.cometpan.service.impl;

import cn.hutool.core.util.ObjectUtil;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.exception.BusinessException;
import top.nomelin.cometpan.common.exception.SystemException;
import top.nomelin.cometpan.dao.DiskMapper;
import top.nomelin.cometpan.pojo.DiskFile;
import top.nomelin.cometpan.pojo.DownloadFileInfo;
import top.nomelin.cometpan.pojo.FileInfo;
import top.nomelin.cometpan.pojo.FileMeta;
import top.nomelin.cometpan.service.DownloadService;
import top.nomelin.cometpan.service.FileService;
import top.nomelin.cometpan.util.Util;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DownloadServiceImpl implements DownloadService {
    private final static Logger logger = LoggerFactory.getLogger(DownloadServiceImpl.class);
    /**
     * 分片下载每一片大小为50M
     */
    private static final Long PER_SLICE = 1024 * 1024 * 50L;
    /**
     * final string
     */
    private static final String RANGE = "Range";
    private final DiskMapper diskMapper;
    private final FileService fileService;
    /**
     * 定义分片下载线程池
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

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

    @Override
    public ResponseEntity<FileSystemResource> downloadByBrowser(Integer diskId, Integer fileId) throws FileNotFoundException {
        DiskFile diskFile = diskMapper.selectById(diskId);
        FileMeta fileMeta = fileService.selectById(fileId);
        if (ObjectUtil.isNull(diskFile) || ObjectUtil.isNull(fileMeta)) {
            throw new SystemException(CodeMessage.INVALID_FILE_ID_ERROR);
        }
        String paths = diskFile.getPath();
        logger.info("downloadFile paths: " + paths);
        // 根据paths参数找到文件
        File file = new File(paths);
        if (!file.exists()) {
            throw new BusinessException(CodeMessage.NOT_FOUND_ERROR);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", "attachment; filename="
                + Util.getFullName(fileMeta.getName(), fileMeta.getType()));
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Last-Modified", new Date().toString());
        headers.add("ETag", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new FileSystemResource(file));
    }


    /**
     * 下载文件
     */
    @Override
    public void downloadNormal(Integer id, Boolean isAttachment, HttpServletRequest request, HttpServletResponse response) {
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

    /**
     * 文件下载
     */
    @Override
    public void downloadNormal(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String downloadFile = "D:\\lib\\分布式计算\\exp3\\report.md";
        // 获取文件
        File file = new File(downloadFile);
        // 获取下载文件信息
        DownloadFileInfo downloadFileInfo = getDownloadFileInfo(file.length(), request, response);
        // 设置响应头
        setResponse(response, file.getName(), downloadFileInfo);
        // 下载文件
        try (InputStream is = new BufferedInputStream(new FileInputStream(file));
             OutputStream os = new BufferedOutputStream(response.getOutputStream())) {
            // 跳过已经读取文件
            is.skip(downloadFileInfo.getPos());
            byte[] buffer = new byte[1024];
            long sum = 0;
            // 读取
            while (sum < downloadFileInfo.getRangeLength()) {
                int length = is.read(buffer, 0, (downloadFileInfo.getRangeLength() - sum) <= buffer.length ?
                        (int) (downloadFileInfo.getRangeLength() - sum) : buffer.length);
                sum = sum + length;
                os.write(buffer, 0, length);
            }
        }

    }

    /**
     * 有两个map，我要去判断里面相同键的值一致不一致，除了双重for循环，有没有别的好办法
     */
    private DownloadFileInfo getDownloadFileInfo(long fSize, HttpServletRequest request, HttpServletResponse response) {
        long pos = 0;
        long last = fSize - 1;
        // 判断前端是否需要分片下载
        if (request.getHeader(RANGE) != null) {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            String numRange = request.getHeader(RANGE).replace("bytes=", "");
            String[] strRange = numRange.split("-");
            if (strRange.length == 2) {
                pos = Long.parseLong(strRange[0].trim());
                last = Long.parseLong(strRange[1].trim());
                // 若结束字节超出文件大小，取文件大小
                if (last > fSize - 1) {
                    last = fSize - 1;
                }
            } else {
                // 若只给一个长度，开始位置一直到结束
                pos = Long.parseLong(numRange.replace("-", "").trim());
            }
        }
        long rangeLength = last - pos + 1;
        String contentRange = "bytes " + pos + "-" + last + "/" + fSize;
        return new DownloadFileInfo(fSize, pos, last, rangeLength, contentRange);
    }

    /**
     * 分片下载
     */
    @Override
    public void downloads() throws IOException {
        String downloadPath = "D:\\lib\\分布式计算\\exp3\\编译后jar包";
        File file = new File(downloadPath);
        // 如果文件夹不存在则创建
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }
        // 探测下载，获取文件相关信息
        FileInfo fileInfoDto = sliceDownload(1, 10, -1, null);
        // 如果不为空，执行分片下载
        if (fileInfoDto != null) {
            // 计算有多少分片
            long pages = fileInfoDto.getFileSize() / PER_SLICE;
            // 适配最后一个分片
            for (long i = 0; i <= pages; i++) {
                long start = i * PER_SLICE;
                long end = (i + 1) * PER_SLICE - 1;
                executorService.execute(new SliceDownloadRunnable(start, end, i, fileInfoDto.getFileName()));
            }
        }
    }

    /**
     * 分片下载
     *
     * @param start 分片起始位置
     * @param end   分片结束位置
     * @param page  第几个分片, page=-1时是探测下载
     */
    private FileInfo sliceDownload(long start, long end, long page, String fName) throws IOException {
        String downloadPath = "D:\\lib\\分布式计算\\exp3\\编译后jar包";

        // 断点下载
        File file = new File(downloadPath, page + "-" + fName);
        // 如果当前文件已经存在，并且不是探测任务，并且文件的长度等于分片的大小，那么不用下载当前文件
        if (file.exists() && page != -1 && file.length() == PER_SLICE) {
            return null;
        }
        // 创建HttpClient
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://localhost:12345/file/download");
        httpGet.setHeader(RANGE, "bytes=" + start + "-" + end);
        HttpResponse httpResponse = client.execute(httpGet);
        String fSize = httpResponse.getFirstHeader("fSize").getValue();
        fName = URLDecoder.decode(httpResponse.getFirstHeader("fName").getValue(), StandardCharsets.UTF_8);
        HttpEntity entity = httpResponse.getEntity();
        // 下载
        try (InputStream is = entity.getContent();
             FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int ch;
            while ((ch = is.read(buffer)) != -1) {
                fos.write(buffer, 0, ch);
            }
            fos.flush();
        }
        // 判断是否是最后一个分片，如果是那么合并
        if (end - Long.parseLong(fSize) > 0) {
            mergeFile(fName, page);
        }
        return new FileInfo(Long.parseLong(fSize), fName);
    }

    private void mergeFile(String fName, long page) throws IOException {
        String downloadPath = "D:\\lib\\分布式计算\\exp3\\编译后jar包";

        File file = new File(downloadPath, fName);

        try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
            for (int i = 0; i <= page; i++) {
                File tempFile = new File(downloadPath, i + "-" + fName);
                // 文件不存在或文件没写完
                while (!tempFile.exists() || (i != page && tempFile.length() < PER_SLICE)) {
                    Thread.sleep(100);
                }
                byte[] bytes = FileUtils.readFileToByteArray(tempFile);
                os.write(bytes);
                os.flush();
                tempFile.delete();
            }
            // 删除文件
            File f = new File(downloadPath, "-1" + "-null");
            if (f.exists()) {
                f.delete();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置响应头
     */
    private void setResponse(HttpServletResponse response, String fileName, DownloadFileInfo downloadFileInfo) {
        response.setCharacterEncoding(String.valueOf(StandardCharsets.UTF_8));
//        response.setContentType("application/x-download");
        response.setContentType("application/octet-stream");
        response.setContentLength((int) downloadFileInfo.getFSize());
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        // 支持分片下载
        response.setHeader("Accept-Range", "bytes");
        response.setHeader("fSize", String.valueOf(downloadFileInfo.getFSize()));
        response.setHeader("fName", URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        // range响应头
        response.setHeader("Content-Range", downloadFileInfo.getContentRange());
        response.setHeader("Content-Length", String.valueOf(downloadFileInfo.getRangeLength()));
    }

    private class SliceDownloadRunnable implements Runnable {
        private final long start;
        private final long end;
        private final long page;
        private final String fName;

        private SliceDownloadRunnable(long start, long end, long page, String fName) {
            this.start = start;
            this.end = end;
            this.page = page;
            this.fName = fName;
        }

        @Override
        public void run() {
            try {
                sliceDownload(start, end, page, fName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

