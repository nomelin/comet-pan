package top.nomelin.cometpan.pojo;

public class InstantUploadDTO {
    private String filename;
    private Long totalSize;
    private Integer targetFolderId;
    private Integer diskId;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public Integer getTargetFolderId() {
        return targetFolderId;
    }

    public void setTargetFolderId(Integer targetFolderId) {
        this.targetFolderId = targetFolderId;
    }

    public Integer getDiskId() {
        return diskId;
    }

    public void setDiskId(Integer diskId) {
        this.diskId = diskId;
    }
}
