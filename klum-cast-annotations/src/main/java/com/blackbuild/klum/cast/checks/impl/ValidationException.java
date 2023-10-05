package com.blackbuild.klum.cast.checks.impl;

import org.codehaus.groovy.ast.ASTNode;

public class ValidationException extends RuntimeException {

    private final ASTNode position;
    public ValidationException(String message) {
        this(message, null, null);
    }

    public ValidationException(String message, ASTNode position) {
        this(message, null, position);
    }

    public ValidationException(String message, Throwable cause) {
        this(message, cause, null);
    }

    public ValidationException(String message, Throwable cause, ASTNode position) {
        super(message, cause);
        this.position = position;
    }

    public ValidationException(Throwable cause) {
        this(cause.getMessage(), cause, null);
    }

    public KlumCastCheck.Error toError(ASTNode position) {
        return new KlumCastCheck.Error(getMessage(), this.position != null ? this.position : position);
    }
}
