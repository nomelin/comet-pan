package top.nomelin.cometpan.service.impl;


import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import top.nomelin.cometpan.pojo.FileChunk;
import top.nomelin.cometpan.pojo.FileChunkResult;
import top.nomelin.cometpan.service.UploaderService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.*;

@Service
public class UploaderServiceImpl implements UploaderService {

    private static final Logger logger = LoggerFactory.getLogger(UploaderServiceImpl.class);
    private final RedisTemplate<String, Object> redisTemplate;
    @Value("${upload.folder}")
    private String UPLOAD_FOLDER;

    public UploaderServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
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
        logger.info("分块的目录 -> {}", chunkFileFolderPath); // 记录分片文件夹路径
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
            logger.info("文件标识:{},chunkNumber:{}", chunkDTO.getIdentifier(), chunkDTO.getChunkNumber()); // 记录文件标识和分片编号
            // 将该分片写入redis
            long size = saveToRedis(chunkDTO); // 将分片信息写入Redis
        } catch (Exception e) {
            e.printStackTrace(); // 处理异常情况
        }
    }


    @Override
    public boolean mergeChunk(String identifier, String fileName, Integer totalChunks) {
        return mergeChunks(identifier, fileName, totalChunks);
    }

    /**
     * 合并分片
     */
    private boolean mergeChunks(String identifier, String filename, Integer totalChunks) {
        String chunkFileFolderPath = getChunkFileFolderPath(identifier); // 获取分片文件夹路径
        String filePath = getFilePath(identifier, filename); // 获取文件路径
        // 检查分片是否都存在
        if (checkChunks(chunkFileFolderPath, totalChunks)) { // 检查分片是否完整
            File chunkFileFolder = new File(chunkFileFolderPath); // 创建分片文件夹对象
            File mergeFile = new File(filePath); // 创建合并后的文件对象
            File[] chunks = chunkFileFolder.listFiles(); // 获取分片文件列表
            // 切片排序1、2/3、---
            if (chunks == null || chunks.length != totalChunks + 1) { // 如果分片数量不正确
                return false; // 返回合并失败
            }
            List<File> fileList = Arrays.asList(chunks); // 将文件数组转换为列表
            fileList.sort((o1, o2) -> { // 对文件列表进行排序
                return Integer.parseInt(o1.getName()) - (Integer.parseInt(o2.getName())); // 按文件名进行升序排序
            });
            try {
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
            } catch (Exception e) { // 处理异常
                logger.error("合并分片失败");
                return false; // 合并失败，返回false
            }
            return true; // 合并成功，返回true
        }
        logger.warn("分片文件没有都存在");
        return false; // 合并失败，返回false
    }


    /**
     * 检查分片是否都存在
     */
    private boolean checkChunks(String chunkFileFolderPath, Integer totalChunks) {
        try {
            for (int i = 1; i <= totalChunks + 1; i++) {
                File file = new File(chunkFileFolderPath + File.separator + i);
                if (!file.exists()) {
                    return false;
                }
            }
        } catch (Exception e) {
            logger.warn("检查分片是否都存在失败");
            return false;
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
            HashMap<String, Object> objectObjectHashMap = new HashMap<>(); // 创建一个HashMap保存分片信息
            objectObjectHashMap.put("uploaded", uploaded); // 将已上传分片信息放入HashMap中
            objectObjectHashMap.put("totalChunks", chunkDTO.getTotalChunks()); // 将总分片数量放入HashMap中
            objectObjectHashMap.put("totalSize", chunkDTO.getTotalSize()); // 将文件总大小放入HashMap中
            objectObjectHashMap.put("path", chunkDTO.getFilename()); // 将文件名（路径）放入HashMap中
            redisTemplate.opsForHash().putAll(chunkDTO.getIdentifier(), objectObjectHashMap); // 将HashMap中的信息保存到Redis中
        } else { // 如果已上传分片信息存在
            uploaded.add(chunkDTO.getChunkNumber()); // 将当前分片编号添加到已上传分片信息中
            redisTemplate.opsForHash().put(chunkDTO.getIdentifier(), "uploaded", uploaded); // 更新Redis中的已上传分片信息
        }
        return uploaded.size(); // 返回已上传分片信息的大小
    }


    /**
     * 得到文件的绝对路径
     */
    private String getFilePath(String identifier, String filename) {
        String ext = filename.substring(filename.lastIndexOf("."));
//        return getFileFolderPath(identifier) + identifier + ext;
        return UPLOAD_FOLDER + File.separator + filename;
    }

    /**
     * 得到文件的相对路径
     */
    private String getFileRelativelyPath(String identifier, String filename) {
        String ext = filename.substring(filename.lastIndexOf("."));
        return "/" + identifier.substring(0, 1) + "/" +
                identifier.substring(1, 2) + "/" +
                identifier + "/" + identifier
                + ext;
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
                identifier.substring(1, 2) + File.separator +
                identifier + File.separator;//取得 identifier 的第一个字符,第二个字符,标识符
//        return uploadFolder;
    }
}
