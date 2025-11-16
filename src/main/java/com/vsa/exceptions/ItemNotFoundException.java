package com.vsa.exceptions;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(String id) {
        super("Item not found: " + id);
    }
}
