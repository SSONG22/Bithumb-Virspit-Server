package com.virspit.virspitproduct.error.exception;


import com.virspit.virspitproduct.error.ErrorCode;

public class EntityNotFoundException extends BusinessException {

    public EntityNotFoundException(String message) {
        super(message, ErrorCode.ENTITY_NOT_FOUND);
    }
}
