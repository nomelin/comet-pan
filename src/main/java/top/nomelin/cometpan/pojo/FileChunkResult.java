package top.nomelin.cometpan.pojo;


import java.util.Set;

public class FileChunkResult {
    /**
     * 是否跳过上传
     */
    private Boolean skipUpload;

    /**
     * 已上传分片的集合
     */
    private Set<Integer> uploaded;

    /**
     * 文件名
     */
    private String fileName;
    /**
     * 文件在硬盘中的id
     */
    private Integer diskId;
    /**
     * 文件大小
     * 单位：字节
     */
    private Long size;
    /**
     * 消息版本号，用于前端判断发送秒传请求。因为前端会请求多个分片，所以需要版本号来区分
     */
    private Integer version;

    public FileChunkResult(Boolean skipUpload, Set<Integer> uploaded) {
        this.skipUpload = skipUpload;
        this.uploaded = uploaded;
    }

    public FileChunkResult(Boolean skipUpload) {
        this.skipUpload = skipUpload;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean getSkipUpload() {
        return skipUpload;
    }

    public void setSkipUpload(Boolean skipUpload) {
        this.skipUpload = skipUpload;
    }

    public Set<Integer> getUploaded() {
        return uploaded;
    }

    public void setUploaded(Set<Integer> uploaded) {
        this.uploaded = uploaded;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getDiskId() {
        return diskId;
    }

    public void setDiskId(Integer diskId) {
        this.diskId = diskId;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }
}


