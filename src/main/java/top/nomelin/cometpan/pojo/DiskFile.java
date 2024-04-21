package top.nomelin.cometpan.pojo;

/**
 * 物理磁盘文件
 *
 * @author nomelin
 */
public class DiskFile {
    private Integer id;
    private Integer count;
    private String path;
    private String hash;
    private Long length;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "DiskFile{" +
                "id=" + id +
                ", count=" + count +
                ", path='" + path + '\'' +
                ", hash='" + hash + '\'' +
                ", length=" + length +
                '}';
    }
}
