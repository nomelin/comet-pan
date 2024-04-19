package top.nomelin.cometpan.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import top.nomelin.cometpan.common.Result;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.pojo.FileChunk;
import top.nomelin.cometpan.pojo.FileChunkResult;
import top.nomelin.cometpan.service.UploaderService;

@RestController
@RequestMapping("/upload")
public class UploaderController {


    private final UploaderService uploadService;
    private final static Logger logger= LoggerFactory.getLogger(UploaderController.class);

    public UploaderController(UploaderService uploadService) {
        this.uploadService = uploadService;
    }

    /**
     * 检查分片是否存在
     *
     */
    @GetMapping("/chunk")
    public Result checkChunkExist(FileChunk chunkDTO) {
        FileChunkResult fileChunkCheckDTO;
        try {
            fileChunkCheckDTO = uploadService.checkChunkExist(chunkDTO);
            return Result.success(fileChunkCheckDTO);
        } catch (Exception e) {
            return Result.error(CodeMessage.UNKNOWN_ERROR);
        }
    }


    /**
     * 上传文件分片
     *
     */
    @PostMapping("/chunk")
    public Result uploadChunk(FileChunk chunkDTO) {
        try {
            uploadService.uploadChunk(chunkDTO);
            return Result.success(chunkDTO.getIdentifier());
        } catch (Exception e) {
            return Result.error(CodeMessage.UNKNOWN_ERROR);
        }
    }

    /**
     * 请求合并文件分片
     *
     */
    @PostMapping("/merge")
    public Result mergeChunks(@RequestBody FileChunk chunkDTO) {
        try {
            boolean success = uploadService.mergeChunk(chunkDTO.getIdentifier(), chunkDTO.getFilename(), chunkDTO.getTotalChunks());
            logger.info("mergeChunks success: " + success);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(CodeMessage.UNKNOWN_ERROR);
        }
    }

}
