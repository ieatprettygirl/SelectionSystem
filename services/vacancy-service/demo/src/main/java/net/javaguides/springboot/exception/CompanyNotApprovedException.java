package net.javaguides.springboot.exception;

public class CompanyNotApprovedException extends RuntimeException {
    public CompanyNotApprovedException(String message) {
        super(message);
    }
}