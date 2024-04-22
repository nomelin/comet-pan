package top.nomelin.cometpan.pojo;

import java.io.Serial;
import java.io.Serializable;

public class Share implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    private Integer id;
    /**
     * 名称
     */
    private String path;
    /**
     * 文件ID
     */
    private String fileIds;
    /**
     * 分享时间
     */
    private String shareTime;
    /**
     * 到期时间
     */
    private String endTime;
    /**
     * 访问次数
     */
    private Integer count;
    /**
     * 分享人ID
     */
    private Integer userId;

    /**
     * 访问密码（4位）
     */
    private String code;
    /**
     * 分享的名字
     */
    private String name;
    /**
     * 剩余天数,数据库不保存，每次访问时计算。
     */
    private Integer leftDays;

    public String getFileIds() {
        return fileIds;
    }

    public void setFileIds(String fileIds) {
        this.fileIds = fileIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public String getShareTime() {
        return shareTime;
    }

    public void setShareTime(String shareTime) {
        this.shareTime = shareTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getLeftDays() {
        return leftDays;
    }

    public void setLeftDays(Integer leftDays) {
        this.leftDays = leftDays;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
