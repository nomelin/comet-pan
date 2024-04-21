package top.nomelin.cometpan.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import top.nomelin.cometpan.common.Result;
import top.nomelin.cometpan.pojo.FileChunk;
import top.nomelin.cometpan.pojo.FileChunkResult;
import top.nomelin.cometpan.pojo.InstantUploadDTO;
import top.nomelin.cometpan.service.FileService;
import top.nomelin.cometpan.service.UploaderService;

import java.io.IOException;

@RestController
@RequestMapping("/upload")
public class UploaderController {


    private final static Logger logger = LoggerFactory.getLogger(UploaderController.class);
    private final UploaderService uploadService;

    private final FileService fileService;

    public UploaderController(UploaderService uploadService, FileService fileService) {
        this.uploadService = uploadService;
        this.fileService = fileService;
    }

    /**
     * 检查分片是否存在
     */
    @GetMapping("/chunk")
    public Result checkChunkExist(FileChunk chunkDTO) {
        FileChunkResult fileChunkCheckDTO;

        fileChunkCheckDTO = uploadService.checkChunkExist(chunkDTO);
        return Result.success(fileChunkCheckDTO);

    }


    /**
     * 上传文件分片
     */
    @PostMapping("/chunk")
    public Result uploadChunk(FileChunk chunkDTO) throws IOException {

        uploadService.uploadChunk(chunkDTO);
        return Result.success(chunkDTO.getIdentifier());

    }

    /**
     * 请求合并文件分片
     */
    @PostMapping("/merge")
    public Result mergeChunks(@RequestBody FileChunk chunk) throws IOException {

        boolean success = uploadService.mergeChunkAndUpdateDatabase(
                chunk.getIdentifier(), chunk.getFilename(), chunk.getTotalChunks(), chunk.getTargetFolderId());
        logger.info("mergeChunks 成功: " + success);
        return Result.success(success);

    }

    @PostMapping("/instant")
    public Result instantUpload(@RequestBody InstantUploadDTO dto) {
        fileService.addFile(dto.getFilename(), dto.getTargetFolderId(), Math.toIntExact(dto.getTotalSize()), dto.getDiskId());
        return Result.success();
    }

}
