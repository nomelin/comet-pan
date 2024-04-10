package top.nomelin.cometpan.pojo;

public class User extends Account {
    private String phone;
    private String email;
    private Long usedSpace;
    private Integer rootId;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getUsedSpace() {
        return usedSpace;
    }

    public void setUsedSpace(Long usedSpace) {
        this.usedSpace = usedSpace;
    }

    @Override
    public String toString() {
        return "User{" + super.toString() + ", phone='" + phone + '\'' + ", email='" + email + '\'' + ", usedSpace=" + usedSpace + ", rootId=" + rootId + '}';
    }

    public Integer getRootId() {
        return rootId;
    }

    public void setRootId(Integer rootId) {
        this.rootId = rootId;
    }
}
