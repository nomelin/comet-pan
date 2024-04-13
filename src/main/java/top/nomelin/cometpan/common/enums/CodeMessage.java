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
    ACCOUNT_CACHE_ERROR("406", "缓存读取失败,请重新登录"),


    USER_NAME_EXIST_ERROR("501", "用户名已存在"),
    USER_NOT_LOGIN_ERROR("502", "用户未登录"),
    USER_ACCOUNT_ERROR("503", "账号或密码错误"),
    USER_NOT_EXIST_ERROR("504", "用户不存在"),
    PARAM_PASSWORD_ERROR("505", "原密码输入错误"),
    USER_NAME_NULL_ERROR("506", "用户名缺失"),
    NEED_ADMIN_ERROR("507", "用户权限不足,需要管理员权限"),
    NEED_USER_ERROR("507", "权限不适配,此处为普通用户权限才能访问"),
    ROLE_ERROR("508", "权限不匹配"),
    INVALID_USER_NAME_ERROR("509", "要操作的用户名不合法，无权限操作"),
    EQUAL_PASSWORD_ERROR("510", "新密码不能与原密码相同"),

    PARENT_FOLDER_NOT_EXIST_ERROR("601", "父文件夹不存在"),
    PARENT_IS_NOT_FOLDER_ERROR("602", "父节点不是文件夹"),
    INVALID_FILE_ID_ERROR("603", "操作的节点id不合法"),
    INVALID_DELETE_ERROR("604", "要彻底删除的节点不在回收站中"),

    SYSTEM_ERROR("800", "系统异常"),
    UNKNOWN_ERROR("900", "未知异常"),
    ;

    public final String code;
    public final String msg;

    CodeMessage(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
