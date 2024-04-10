package top.nomelin.cometpan.common;

import top.nomelin.cometpan.common.enums.CodeMessage;

/**
 * @author nomelin
 */
public class Result {
    private String code;
    private String msg;
    private Object data;

    public Result(String code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Result(String code, String msg) {
        this.code = code;
        this.msg = msg;
        this.data = null;
    }

    /**
     * 成功结果
     *
     * @return 成功结果
     */
    public static Result success() {
        return new Result(CodeMessage.SUCCESS.code, CodeMessage.SUCCESS.msg);
    }

    /**
     * 成功结果
     *
     * @param data 数据
     * @return 成功结果
     */
    public static Result success(Object data) {
        return new Result(CodeMessage.SUCCESS.code, CodeMessage.SUCCESS.msg, data);
    }


    public static Result error(CodeMessage codeMessage, Object data) {
        return new Result(codeMessage.code, codeMessage.msg, data);
    }

    public static Result error(CodeMessage codeMessage) {
        return new Result(codeMessage.code, codeMessage.msg);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
