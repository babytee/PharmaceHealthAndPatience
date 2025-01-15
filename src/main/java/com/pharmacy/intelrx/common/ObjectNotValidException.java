package com.pharmacy.intelrx.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ObjectNotValidException extends RuntimeException {
    private final Map<String, String> fieldErrors;
}

