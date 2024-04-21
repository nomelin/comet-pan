package top.nomelin.cometpan.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.nomelin.cometpan.common.Result;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.exception.BusinessException;
import top.nomelin.cometpan.common.exception.SystemException;

/**
 * @author nomelin
 */
@RestControllerAdvice
public class ExceptionHandle {
    private final Logger logger = LoggerFactory.getLogger(ExceptionHandle.class);

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return 结果
     */
    @ExceptionHandler(BusinessException.class)
    public Result doBusinessException(BusinessException e) {
        logger.warn("业务异常->" + e.codeMessage.code + ":" + e.codeMessage.msg);//TODO 仅调试用, 正式环境不打印业务异常信息
        return new Result(e.codeMessage.code, e.codeMessage.msg);
    }

    /**
     * 处理系统异常
     *
     * @param e 系统异常
     * @return 结果
     */
    @ExceptionHandler(SystemException.class)
    public Result doSystemException(SystemException e) {
        logger.warn("系统异常->" + e.codeMessage.code + ":" + e.codeMessage.msg);
        e.printStackTrace();
        return new Result(e.codeMessage.code, e.codeMessage.msg);
    }

    /**
     * 处理其它异常
     *
     * @param e 其它异常
     * @return 结果
     */
    @ExceptionHandler(Throwable.class)
    public Result doException(Throwable e) {
        logger.warn("其它异常->" + e.getMessage());
        e.printStackTrace();
        return new Result(CodeMessage.UNKNOWN_ERROR.code, CodeMessage.UNKNOWN_ERROR.msg);
    }
}
