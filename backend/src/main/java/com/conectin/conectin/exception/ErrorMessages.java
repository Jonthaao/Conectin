package com.conectin.conectin.exception;

public class ErrorMessages {
    public static final String INVALID_CREDENTIALS = "Usuário ou senha inválido";
    public static final String INVALID_CREDENTIALS_CODE = "AUTH_ERROR_001";

    public static final String USER_NOT_FOUND = "Usuário não encontrado";
    public static final String USER_NOT_FOUND_CODE = "USER_ERROR_001";

    public static final String USER_ALREADY_EXISTS = "Usuário já existe";
    public static final String USER_ALREADY_EXISTS_CODE = "USER_ERROR_002";

    public static final String USER_NOT_AUTHORIZED = "Usuário não autorizado";
    public static final String USER_NOT_AUTHORIZED_CODE = "USER_ERROR_003";

    public static final String EMAIL_ALREADY_EXISTS = "E-mail já existe";
    public static final String EMAIL_ALREADY_EXISTS_CODE = "USER_ERROR_004";

    public static final String INVALID_EMAIL = "E-mail inválido";
    public static final String INVALID_EMAIL_CODE = "USER_ERROR_005";

    public static final String INVALID_TOKEN = "Token inválido ou ausente";
    public static final String INVALID_TOKEN_CODE = "AUTH_ERROR_002";

    public static final String EXPIRED_TOKEN = "Token expirado ou inválido";
    public static final String EXPIRED_TOKEN_CODE = "AUTH_ERROR_003";

    private ErrorMessages() {
        // Construtor privado para evitar instância
    }
}
