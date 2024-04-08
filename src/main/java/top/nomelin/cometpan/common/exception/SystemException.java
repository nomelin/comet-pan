package top.nomelin.cometpan.common.exception;

import top.nomelin.cometpan.common.enums.CodeMessage;

public class SystemException extends RuntimeException {
    public final CodeMessage codeMessage;

    public SystemException(CodeMessage codeMessage) {
        this.codeMessage = codeMessage;
    }

}
