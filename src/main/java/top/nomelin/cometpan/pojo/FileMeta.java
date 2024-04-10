package top.nomelin.cometpan.pojo;

/**
 * 文件元数据
 *
 * @author nomelin
 */
public class FileMeta {
    /**
     * ID
     */
    private Integer id;
    /**
     * 文件名称
     */
    private String name;
    /**
     * 是否文件夹
     */
    private Boolean folder;
    /**
     * 文件路径
     */
    private String path;
    /**
     * 创建人ID
     */
    private Integer userId;

    private Integer diskId;
    /**
     * 文件类型
     */
    private String type;
    /**
     * 文件大小
     */
    private Integer size;
    /**
     * 创建时间
     */
    private String crateTime;
    /**
     * 修改时间
     */
    private String updateTime;

    private Integer folderId;
    private Integer rootFolderId;

    private Boolean delete;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getFolder() {
        return folder;
    }

    public void setFolder(Boolean folder) {
        this.folder = folder;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getCrateTime() {
        return crateTime;
    }

    public void setCrateTime(String crateTime) {
        this.crateTime = crateTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getFolderId() {
        return folderId;
    }

    public void setFolderId(Integer folderId) {
        this.folderId = folderId;
    }

    public Integer getRootFolderId() {
        return rootFolderId;
    }

    public void setRootFolderId(Integer rootFolderId) {
        this.rootFolderId = rootFolderId;
    }

    public Boolean getDelete() {
        return delete;
    }

    public void setDelete(Boolean deleted) {
        this.delete = deleted;
    }

    public Integer getDiskId() {
        return diskId;
    }

    public void setDiskId(Integer diskId) {
        this.diskId = diskId;
    }

    @Override
    public String toString() {
        return "FileMeta{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", folder=" + folder +
                ", path='" + path + '\'' +
                ", userId=" + userId +
                ", diskId=" + diskId +
                ", type='" + type + '\'' +
                ", size=" + size +
                ", crateTime='" + crateTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", folderId=" + folderId +
                ", rootFolderId=" + rootFolderId +
                ", delete=" + delete +
                '}';
    }
}
