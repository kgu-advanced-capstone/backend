package kr.ac.kyonggi.domain.user;

public enum Role {
    USER, ADMIN;

    public String getGrantedAuthority() {
        return "ROLE_" + this.name();
    }
}
