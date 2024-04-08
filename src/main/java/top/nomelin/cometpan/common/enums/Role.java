package top.nomelin.cometpan.common.enums;

public enum Role {
    //ROOT(0),
    ADMIN(1),
    USER(2),
    ;

    public final int roleCode;

    Role(int roleCode) {
        this.roleCode = roleCode;
    }
}
