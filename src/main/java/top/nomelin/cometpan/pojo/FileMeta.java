package top.nomelin.cometpan.pojo;

import com.fasterxml.jackson.annotation.JsonAlias;

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
    @JsonAlias({"userid", "user_id"})
    private Integer userId;
    @JsonAlias({"diskid", "disk_id"})
    private Integer diskId;
    /**
     * 文件类型
     */
    private String type;
    /**
     * 文件大小
     */
    private Long size;
    /**
     * 创建时间
     */
    @JsonAlias({"createtime", "create_time"})
    private String createTime;
    /**
     * 修改时间
     */
    @JsonAlias({"updatetime", "update_time"})
    private String updateTime;

    @JsonAlias({"folderid", "folder_id"})
    private Integer folderId;
    @JsonAlias({"rootfolderid", "root_folder_id"})
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

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
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
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", folderId=" + folderId +
                ", rootFolderId=" + rootFolderId +
                ", delete=" + delete +
                '}';
    }
}
