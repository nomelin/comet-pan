package top.nomelin.cometpan.service.impl;


import cn.hutool.core.util.ObjectUtil;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.nomelin.cometpan.cache.CurrentUserCache;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.exception.SystemException;
import top.nomelin.cometpan.dao.DiskMapper;
import top.nomelin.cometpan.pojo.DiskFile;
import top.nomelin.cometpan.pojo.FileChunk;
import top.nomelin.cometpan.pojo.FileChunkResult;
import top.nomelin.cometpan.service.FileService;
import top.nomelin.cometpan.service.UploaderService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class UploaderServiceImpl implements UploaderService {

    private static final Logger logger = LoggerFactory.getLogger(UploaderServiceImpl.class);

    private final CurrentUserCache currentUserCache;
    private final RedisTemplate<String, Object> redisTemplate;

    private final DiskMapper diskMapper;

    private final FileService fileService;

    @Value("${upload.folder.root}")
    private String UPLOAD_FOLDER;

    @Value("${upload.folder.cache}")
    private String UPLOAD_CACHE_FOLDER;

    @Value("${upload.chunks.timeout}")
    private long redisChunksTimeout;

    @Value("${upload.buffer-size}")
    private int bufferSize;

    public UploaderServiceImpl(CurrentUserCache currentUserCache, RedisTemplate<String, Object> redisTemplate, DiskMapper diskMapper, FileService fileService) {
        this.currentUserCache = currentUserCache;
        this.redisTemplate = redisTemplate;
        this.diskMapper = diskMapper;
        this.fileService = fileService;
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
        //删除[md5]文件夹
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
    @Transactional
    public FileChunkResult checkChunkExist(FileChunk chunkDTO) {
        testRedisConnection(); // 测试Redis连接
        // 秒传
        DiskFile diskFile = new DiskFile();
        diskFile.setHash(chunkDTO.getIdentifier());
        List<DiskFile> diskFiles = diskMapper.selectAll(diskFile);
        for (DiskFile file : diskFiles) {
            if (Objects.equals(file.getLength(), chunkDTO.getTotalSize())) {
                //如果MD5相同，且文件大小相同，则认为是相同文件，可以秒传。
                logger.info("分片：filename:{},totalChunks:{},diskFileId:{},totalSize:{}",
                        chunkDTO.getFilename(), chunkDTO.getTotalChunks(), file.getId(), chunkDTO.getTotalSize());
                logger.info("秒传成功,file:{}", file.getPath());
                FileChunkResult fileChunkResult = new FileChunkResult(true);
                fileChunkResult.setDiskId(file.getId());
                return fileChunkResult;
            }
        }
        String fileFolderPath = getChunkFileFolderPath(chunkDTO.getIdentifier()); // 获取文件夹路径
        logger.info("fileFolderPath-->{}", fileFolderPath); // 记录文件夹路径
        File fileFolder = new File(fileFolderPath); // 创建文件夹对象
        if (!fileFolder.exists()) { // 如果文件夹不存在
            boolean mkdirs = fileFolder.mkdirs(); // 创建文件夹
            logger.info("准备工作,创建文件夹,fileFolderPath:{},mkdirs:{}", fileFolderPath, mkdirs); // 记录文件夹创建信息
        }
        Set<Integer> uploaded = (Set<Integer>) redisTemplate.opsForHash().get(chunkDTO.getIdentifier(), "uploaded"); // 从Redis中获取已上传的分片信息
        logger.info("fileFolderPath:{},chunks:{},totalChunks:{}", fileFolderPath, uploaded, chunkDTO.getTotalChunks()); // 记录文件路径和文件是否存在信息
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
        testRedisConnection();
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


    /**
     * 合并分片
     */
    @Override
    @Transactional
    public boolean mergeChunkAndUpdateDatabase(String identifier, String filename, Integer totalChunks, Integer targetFolderId) {
        logger.info("开始合并分片,identifier:{},filename:{},totalChunks:{},targetFolderId:{}", identifier, filename, totalChunks, targetFolderId);
        String chunkFileFolderPath = getChunkFileFolderPath(identifier); // 获取分片文件夹路径
        testRedisConnection(); // 测试Redis连接
        // 检查分片是否都存在
        checkChunks(chunkFileFolderPath, totalChunks);
        File chunkFileFolder = new File(chunkFileFolderPath); // 创建分片文件夹对象
        File[] chunks = chunkFileFolder.listFiles(); // 获取分片文件列表
        if (ObjectUtil.isNull(chunks)) {
            throw new SystemException(CodeMessage.FILE_UPLOAD_ERROR);
        }
        // 更新数据库
        //如果有执行到合并文件这一步，一定不是秒传,所以一定是新文件
        Long size = (Long) redisTemplate.opsForHash().get(identifier, "totalSize");
        if (ObjectUtil.isNull(size)) {
            size = 0L;
        }
        DiskFile diskFile = new DiskFile();
        diskFile.setCount(1);
        diskFile.setHash(identifier);
        diskFile.setLength(size);
        diskMapper.insert(diskFile); // 插入数据库，得到id
        fileService.addFile(filename, targetFolderId,size, diskFile.getId());
        //合并文件
        String filePath = getFilePath(identifier, diskFile.getId()); // 获取文件路径
        diskFile.setPath(filePath);
        diskMapper.updateById(diskFile);//先插入，得到id，然后根据id得到path，然后再根据id保存path到数据库。

        Path path = Paths.get(filePath);// 创建文件路径对象
        Path directoryPath = path.getParent();// 获取文件所在的文件夹路径
        try {
            Files.createDirectories(directoryPath);// 创建文件夹（包括必要的父文件夹）
            File mergeFile = new File(filePath); // 创建合并后的文件对象

            List<File> fileList = Arrays.asList(chunks); // 将分片文件数组转换为列表
            fileList.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getName()))); // 按文件名进行升序排序

            logger.info("准备合并分片");
            // 使用 try-with-resources 语句创建随机访问文件对象
            try (RandomAccessFile randomAccessFileWriter = new RandomAccessFile(mergeFile, "rw")) {
                byte[] bytes = new byte[bufferSize]; // 创建字节数组缓冲区
                // 遍历分片文件列表
                for (File chunk : chunks) {
                    // 使用 try-with-resources 语句创建分片文件的随机访问文件对象
                    try (RandomAccessFile randomAccessFileReader = new RandomAccessFile(chunk, "r")) {
                        int len;
                        // 读取分片文件内容到缓冲区，并写入合并后的文件
                        while ((len = randomAccessFileReader.read(bytes)) != -1) {
                            randomAccessFileWriter.write(bytes, 0, len);
                        }
                    }//// try-with-resources 语句结束，自动关闭随机访问文件对象
                }
                logger.info("合并分片成功,identifier:{},filename:{},totalChunks:{}", identifier, filename, totalChunks);
            } // try-with-resources 语句结束，自动关闭随机访问文件对象
        } catch (IOException e) {
            e.printStackTrace();
            throw new SystemException(CodeMessage.FILE_UPLOAD_ERROR);
        }

        // 清空分片
        clearCacheFolder(chunkFileFolderPath);

        //清空redis
        redisTemplate.delete(identifier);
        return true; // 合并成功，返回true
    }


    /**
     * 检查分片是否都存在
     */
    private void checkChunks(String chunkFileFolderPath, Integer totalChunks) {
        for (int i = 1; i <= totalChunks + 1; i++) {
            File file = new File(chunkFileFolderPath + File.separator + i);
            if (!file.exists()) {
                throw new SystemException(CodeMessage.CHUNK_NOT_FULL_ERROR);
            }
        }
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
            hashMap.put("filename", chunkDTO.getFilename()); // 将文件名（路径）放入HashMap中
            redisTemplate.opsForHash().putAll(chunkDTO.getIdentifier(), hashMap); // 将HashMap中的信息保存到Redis中
            // 设置过期时间
            redisTemplate.expire(chunkDTO.getIdentifier(), redisChunksTimeout, TimeUnit.HOURS); // 设置键的过期时间
        } else { // 如果已上传分片信息存在
            uploaded.add(chunkDTO.getChunkNumber()); // 将当前分片编号添加到已上传分片信息中
            redisTemplate.opsForHash().put(chunkDTO.getIdentifier(), "uploaded", uploaded); // 更新Redis中的已上传分片信息
        }
        return uploaded.size(); // 返回已上传分片信息的大小
    }


    /**
     * 得到真正文件的保存路径
     */
    private String getFilePath(String identifier, int id) {
        return UPLOAD_FOLDER + File.separator +
                identifier.charAt(0) + File.separator + identifier + File.separator + id;
    }

    /**
     * 得到分块文件所属的目录
     */
    private String getChunkFileFolderPath(String identifier) {
        return UPLOAD_CACHE_FOLDER + File.separator + identifier.charAt(0) + File.separator +
                identifier + File.separator;
    }

    private void testRedisConnection() {
        try {
            String pingResult = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().ping();
            logger.info("redis连接成功: " + pingResult);
        } catch (Exception e) {
            logger.warn("redis连接失败: " + e.getMessage());
            throw new SystemException(CodeMessage.REDIS_CONNECTION_ERROR);
        }
    }
}
