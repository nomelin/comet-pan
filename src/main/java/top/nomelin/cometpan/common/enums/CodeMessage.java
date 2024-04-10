package top.nomelin.cometpan.common.enums;

/**
 * @author nomelin
 */
public enum CodeMessage {
    SUCCESS("200", "成功"),

    PARAM_ERROR("400", "参数异常"),
    TOKEN_INVALID_ERROR("401", "无效的token"),
    TOKEN_CHECK_ERROR("402", "token验证失败，请重新登录"),
    TOKEN_PARSING_ERROR("403", "token解析失败，请重新登录"),
    NOT_FOUND_ERROR("404", "资源不存在"),
    PARAM_LOST_ERROR("405", "参数缺失"),
    HTTP_ERROR("406", "http请求异常,请重新登录"),


    USER_NAME_EXIST_ERROR("501", "用户名已存在"),
    USER_NOT_LOGIN("502", "用户未登录"),
    USER_ACCOUNT_ERROR("503", "账号或密码错误"),
    USER_NOT_EXIST_ERROR("504", "用户不存在"),
    PARAM_PASSWORD_ERROR("505", "原密码输入错误"),
    USER_NAME_NULL_ERROR("506", "用户名缺失"),
    NEED_ADMIN_ERROR("507", "用户权限不足,需要管理员权限"),
    NEED_USER_ERROR("507", "权限不适配,此处为普通用户权限才能访问"),
    ROLE_ERROR("508", "权限不匹配"),

    SYSTEM_ERROR("600", "系统异常"),
    UNKNOWN_ERROR("700", "未知异常"),
    ;

    public final String code;
    public final String msg;

    CodeMessage(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
