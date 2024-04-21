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
     * 文件在硬盘中的id
     */
    private Integer diskId;


    public FileChunkResult(Boolean skipUpload, Set<Integer> uploaded) {
        this.skipUpload = skipUpload;
        this.uploaded = uploaded;
    }

    public FileChunkResult(Boolean skipUpload) {
        this.skipUpload = skipUpload;
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


    public Integer getDiskId() {
        return diskId;
    }

    public void setDiskId(Integer diskId) {
        this.diskId = diskId;
    }

}


