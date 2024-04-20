package top.nomelin.cometpan.service;


import top.nomelin.cometpan.pojo.FileChunk;
import top.nomelin.cometpan.pojo.FileChunkResult;

import java.io.IOException;

public interface UploaderService {

    /**
     * 检查文件是否存在，如果存在则跳过该文件的上传，如果不存在，返回需要上传的分片集合
     */
    FileChunkResult checkChunkExist(FileChunk chunkDTO);


    /**
     * 上传文件分片到缓存文件夹
     */
    void uploadChunk(FileChunk chunkDTO) throws IOException;


    /**
     * 合并文件分片,并更新数据库
     *
     * @param identifier     上传文件的唯一标识符,md5值
     * @param fileName       上传文件名
     * @param totalChunks    上传文件总分片数
     * @param targetFolderId 上传文件目标文件夹id
     */
    boolean mergeChunkAndUpdateDatabase(
            String identifier, String fileName, Integer totalChunks, Integer targetFolderId) throws IOException;
}

