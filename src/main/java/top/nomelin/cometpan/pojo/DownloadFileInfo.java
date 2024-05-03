package top.nomelin.cometpan.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class DownloadFileInfo {

    /**
     * 文件总大小
     */
    private long fSize;

    /**
     * 断点起始位置
     */
    private long pos;

    /**
     * 断点结束位置
     */
    private long last;

    /**
     * rang响应
     */
    private long rangeLength;

    /**
     * range响应
     */
    private String contentRange;

}
