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
     * 上传文件分片
     */
    void uploadChunk(FileChunk chunkDTO) throws IOException;


    /**
     * 合并文件分片
     */
    boolean mergeChunk(String identifier,String fileName,Integer totalChunks)throws IOException;
}

