package top.nomelin.cometpan.service.impl;


import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import top.nomelin.cometpan.cache.CurrentUserCache;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.exception.SystemException;
import top.nomelin.cometpan.dao.DiskMapper;
import top.nomelin.cometpan.pojo.FileChunk;
import top.nomelin.cometpan.pojo.FileChunkResult;
import top.nomelin.cometpan.service.UploaderService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UploaderServiceImpl implements UploaderService {

    private static final Logger logger = LoggerFactory.getLogger(UploaderServiceImpl.class);

    private final CurrentUserCache currentUserCache;
    private final RedisTemplate<String, Object> redisTemplate;

    private final DiskMapper diskMapper;

    @Value("${upload.folder}")
    private String UPLOAD_FOLDER;

    public UploaderServiceImpl(CurrentUserCache currentUserCache, RedisTemplate<String, Object> redisTemplate, DiskMapper diskMapper) {
        this.currentUserCache = currentUserCache;
        this.redisTemplate = redisTemplate;
        this.diskMapper = diskMapper;
    }

    /**
     * 清除缓存文件夹
     *
     * @param folderPath 缓存文件夹路径
     */
    private static void clearCacheFolder(String folderPath) {
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            // 清空文件夹
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                if (!file.delete()) {
                    logger.warn("删除文件失败,file:{}", file.getAbsolutePath());
                }
            }
        }
        //删除chunks文件夹
        if (!folder.delete()) {
            logger.warn("删除文件失败,file:{}", folder.getAbsolutePath());
        }
        //删除【md5】文件夹
        folder = folder.getParentFile();
        if (!folder.delete()) {
            logger.warn("删除文件失败,file:{}", folder.getAbsolutePath());
        }
    }

    /**
     * 检查文件是否存在，如果存在则跳过该文件的上传，如果不存在，返回需要上传的分片集合
     * 检查分片是否存在
     * ○ 检查目录下的文件是否存在。
     * ○ 检查redis存储的分片是否存在。
     * ○ 判断分片数量和总分片数量是否一致。
     * 如果文件存在并且分片上传完毕，标识已经完成附件的上传，可以进行秒传操作。
     * 如果文件不存在或者分片为上传完毕，则返回false并返回已经上传的分片信息。
     */
    @Override
    public FileChunkResult checkChunkExist(FileChunk chunkDTO) {
        //1.检查文件是否已上传过
        //1.1)检查在磁盘中是否存在
        String fileFolderPath = getFileFolderPath(chunkDTO.getIdentifier()); // 获取文件夹路径
        logger.info("fileFolderPath-->{}", fileFolderPath); // 记录文件夹路径
        String filePath = getFilePath(chunkDTO.getIdentifier(), chunkDTO.getFilename()); // 获取文件路径
        File file = new File(filePath); // 根据文件路径创建文件对象
        boolean exists = file.exists(); // 检查文件是否存在
        //1.2)检查Redis中是否存在,并且所有分片已经上传完成。
        Set<Integer> uploaded = (Set<Integer>) redisTemplate.opsForHash().get(chunkDTO.getIdentifier(), "uploaded"); // 从Redis中获取已上传的分片信息
        if (uploaded != null && uploaded.size() == chunkDTO.getTotalChunks() && exists) { // 如果已上传分片数量和总分片数量相等且文件存在
            return new FileChunkResult(true); // 返回文件已上传的标识
        }
        File fileFolder = new File(fileFolderPath); // 创建文件夹对象
        if (!fileFolder.exists()) { // 如果文件夹不存在
            boolean mkdirs = fileFolder.mkdirs(); // 创建文件夹
            logger.info("准备工作,创建文件夹,fileFolderPath:{},mkdirs:{}", fileFolderPath, mkdirs); // 记录文件夹创建信息
        }
        // 断点续传，返回已上传的分片
        return new FileChunkResult(false, uploaded); // 返回未完成上传的文件分片信息
    }

    /**
     * 上传分片
     * 上传附件分片
     * ○ 判断目录是否存在，如果不存在则创建目录。
     * ○ 进行切片的拷贝，将切片拷贝到指定的目录。
     * ○ 将该分片写入redis
     *
     * @param chunkDTO 分片信息对象
     */
    @Override
    public void uploadChunk(FileChunk chunkDTO) {
        // 分块的目录
        String chunkFileFolderPath = getChunkFileFolderPath(chunkDTO.getIdentifier()); // 获取分片文件夹路径
//        logger.info("分块的目录 -> {}", chunkFileFolderPath); // 记录分片文件夹路径
        File chunkFileFolder = new File(chunkFileFolderPath); // 创建分片文件夹对象
        if (!chunkFileFolder.exists()) { // 如果分片文件夹不存在
            boolean mkdirs = chunkFileFolder.mkdirs(); // 创建分片文件夹
            logger.info("创建分片文件夹:{}", mkdirs); // 记录分片文件夹创建情况
        }
        // 写入分片
        try (
                InputStream inputStream = chunkDTO.getFile().getInputStream(); // 获取分片文件输入流
                FileOutputStream outputStream = new FileOutputStream(chunkFileFolderPath + chunkDTO.getChunkNumber()) // 创建分片文件输出流
        ) {
            IOUtils.copy(inputStream, outputStream); // 将输入流内容拷贝到输出流
//            logger.info("文件标识:{},chunkNumber:{}", chunkDTO.getIdentifier(), chunkDTO.getChunkNumber()); // 记录文件标识和分片编号
            // 将该分片写入redis
            long size = saveToRedis(chunkDTO); // 将分片信息写入Redis
        } catch (Exception e) {
            e.printStackTrace(); // 处理异常情况
            throw new SystemException(CodeMessage.FILE_UPLOAD_ERROR); // 抛出上传分片异常
        }
    }

    @Override
    public boolean mergeChunk(String identifier, String fileName, Integer totalChunks) throws IOException {
        boolean result = mergeChunks(identifier, fileName, totalChunks);
        if (!result) {
            throw new SystemException(CodeMessage.CHUNK_NOT_FULL_ERROR); // 抛出合并分片异常
        }
        logger.info("合并分片成功,identifier:{},filename:{},totalChunks:{}", identifier, fileName, totalChunks);
        //清空缓存

        return true;
    }

    /**
     * 合并分片
     */
    private boolean mergeChunks(String identifier, String filename, Integer totalChunks) throws IOException {
        logger.info("开始合并分片,identifier:{},filename:{},totalChunks:{}", identifier, filename, totalChunks);
        String chunkFileFolderPath = getChunkFileFolderPath(identifier); // 获取分片文件夹路径
        String filePath = getFilePath(identifier, filename); // 获取文件路径
        // 检查分片是否都存在
        // 创建文件路径对象
        Path path = Paths.get(filePath);
        // 获取文件所在的文件夹路径
        Path directoryPath = path.getParent();
        // 创建文件夹（包括必要的父文件夹）
        Files.createDirectories(directoryPath);
        if (checkChunks(chunkFileFolderPath, totalChunks)) { // 检查分片是否完整
            File chunkFileFolder = new File(chunkFileFolderPath); // 创建分片文件夹对象
            File mergeFile = new File(filePath); // 创建合并后的文件对象
            File[] chunks = chunkFileFolder.listFiles(); // 获取分片文件列表
            // 切片排序1、2/3、---
            if (chunks == null || chunks.length != totalChunks + 1) { // 如果分片数量不正确
                if (chunks != null) {
                    logger.error("分片数量不正确,chunks:{},totalChunks:{}", chunks.length, totalChunks);
                } else {
                    logger.error("分片数量不正确,chunks:null,totalChunks:{}", totalChunks);
                }
                return false; // 返回合并失败
            }
            List<File> fileList = Arrays.asList(chunks); // 将文件数组转换为列表
            fileList.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getName()))); // 按文件名进行升序排序
            RandomAccessFile randomAccessFileWriter = new RandomAccessFile(mergeFile, "rw"); // 创建合并后文件的随机访问文件对象
            byte[] bytes = new byte[1024]; // 创建字节数组缓冲区
            for (File chunk : chunks) { // 遍历分片文件列表
                RandomAccessFile randomAccessFileReader = new RandomAccessFile(chunk, "r"); // 创建分片文件的随机访问文件对象
                int len;
                while ((len = randomAccessFileReader.read(bytes)) != -1) { // 读取分片文件内容到缓冲区
                    randomAccessFileWriter.write(bytes, 0, len); // 将缓冲区内容写入合并后的文件
                }
                randomAccessFileReader.close(); // 关闭分片文件的随机访问文件对象
            }
            randomAccessFileWriter.close(); // 关闭合并后文件的随机访问文件对象
            logger.info("合并分片成功,identifier:{},filename:{},totalChunks:{}", identifier, filename, totalChunks);
            // 清空分片
            clearCacheFolder(chunkFileFolderPath);
            //清空redis
            redisTemplate.delete(identifier);

            return true; // 合并成功，返回true
        }
        throw new SystemException(CodeMessage.CHUNK_NOT_FULL_ERROR); // 抛出分片不完整异常
    }

    /**
     * 检查分片是否都存在
     */
    private boolean checkChunks(String chunkFileFolderPath, Integer totalChunks) {
        for (int i = 1; i <= totalChunks + 1; i++) {
            File file = new File(chunkFileFolderPath + File.separator + i);
            if (!file.exists()) {
                throw new SystemException(CodeMessage.CHUNK_NOT_FULL_ERROR);
            }
        }

        return true;
    }

    /**
     * 分片写入Redis
     * 判断切片是否已存在，如果未存在，则创建基础信息，并保存。
     */
    private synchronized long saveToRedis(FileChunk chunkDTO) {
        Set<Integer> uploaded = (Set<Integer>) redisTemplate.opsForHash().get(chunkDTO.getIdentifier(), "uploaded"); // 获取已上传分片信息
        if (uploaded == null) { // 如果未上传分片信息不存在
            uploaded = new HashSet<>(Collections.singletonList(chunkDTO.getChunkNumber())); // 创建一个新的Set集合并添加当前分片编号
            HashMap<String, Object> hashMap = new HashMap<>(); // 创建一个HashMap保存分片信息
            hashMap.put("uploaded", uploaded); // 将已上传分片信息放入HashMap中
            hashMap.put("totalChunks", chunkDTO.getTotalChunks()); // 将总分片数量放入HashMap中
            hashMap.put("totalSize", chunkDTO.getTotalSize()); // 将文件总大小放入HashMap中
            hashMap.put("path", chunkDTO.getFilename()); // 将文件名（路径）放入HashMap中
            redisTemplate.opsForHash().putAll(chunkDTO.getIdentifier(), hashMap); // 将HashMap中的信息保存到Redis中
            // 设置过期时间
            redisTemplate.expire(chunkDTO.getIdentifier(), 2, TimeUnit.DAYS); // 设置键的过期时间
        } else { // 如果已上传分片信息存在
            uploaded.add(chunkDTO.getChunkNumber()); // 将当前分片编号添加到已上传分片信息中
            redisTemplate.opsForHash().put(chunkDTO.getIdentifier(), "uploaded", uploaded); // 更新Redis中的已上传分片信息
        }
        return uploaded.size(); // 返回已上传分片信息的大小
    }


    /**
     * 得到文件的保存路径
     */
    private String getFilePath(String identifier, String filename) {
        String ext = filename.substring(filename.lastIndexOf("."));
//        return getFileFolderPath(identifier) + identifier + ext;
        return UPLOAD_FOLDER + File.separator + "files" + File.separator +
                currentUserCache.getCurrentUser().getId() + File.separator + filename;
    }


    /**
     * 得到分块文件所属的目录
     */
    private String getChunkFileFolderPath(String identifier) {
        return getFileFolderPath(identifier) + "chunks" + File.separator;
    }

    /**
     * 得到文件所属的目录
     */
    private String getFileFolderPath(String identifier) {
        return UPLOAD_FOLDER + File.separator + identifier.substring(0, 1) + File.separator +
                identifier + File.separator;//取得 identifier 的第一个字符,第二个字符,标识符
//        return uploadFolder;
    }
}
