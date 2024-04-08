package top.nomelin.cometpan.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.nomelin.cometpan.common.Result;
import top.nomelin.cometpan.common.enums.CodeMessage;
import top.nomelin.cometpan.common.exception.BusinessException;
import top.nomelin.cometpan.common.exception.SystemException;

@RestControllerAdvice
public class ExceptionHandle {
    private final Logger logger = LoggerFactory.getLogger(ExceptionHandle.class);

    @ExceptionHandler(BusinessException.class)
    public Result doBusinessException(BusinessException e) {
        return new Result(e.codeMessage.code, e.codeMessage.msg);
    }

    @ExceptionHandler(SystemException.class)
    public Result doSystemException(SystemException e) {
        logger.warn("系统异常->" + e.codeMessage.code + ":" + e.codeMessage.msg);
        return new Result(e.codeMessage.code, e.codeMessage.msg);
    }

    @ExceptionHandler(Exception.class)
    public Result doException(Exception e) {
        logger.warn("其它异常->" + e.getMessage());
        e.printStackTrace();
        return new Result(CodeMessage.UNKNOWN_ERROR.code, CodeMessage.UNKNOWN_ERROR.msg);
    }
}
