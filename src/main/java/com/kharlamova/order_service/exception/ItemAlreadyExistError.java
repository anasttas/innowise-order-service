package com.kharlamova.order_service.exception;

public class ItemAlreadyExistError extends RuntimeException {
    public ItemAlreadyExistError(String message) {
        super(message);
    }
}
