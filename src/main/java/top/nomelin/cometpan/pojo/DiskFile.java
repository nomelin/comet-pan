package top.nomelin.cometpan.pojo;

/**
 * 物理磁盘文件
 *
 * @author nomelin
 */
public class DiskFile {
    private int id;
    private int count;
    private String path;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
