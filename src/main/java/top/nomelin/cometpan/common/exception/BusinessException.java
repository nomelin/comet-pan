package top.nomelin.cometpan.common.exception;

import top.nomelin.cometpan.common.enums.CodeMessage;

public class BusinessException extends RuntimeException {
    public final CodeMessage codeMessage;

    public BusinessException(CodeMessage codeMessage) {
        this.codeMessage = codeMessage;
    }


}
