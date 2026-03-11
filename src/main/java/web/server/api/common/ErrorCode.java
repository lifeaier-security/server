package web.server.api.common;

public enum ErrorCode {

    AUTHENTICATION_FAILED("AUTHENTICATION_FAILED"),
    MAIL_NOT_VERIFIED("MAIL_NOT_VERIFIED"),
    ACCESS_DENIED("ACCESS_DENIED"),
    TOKEN_EXPIRED("TOKEN_EXPIRED");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
