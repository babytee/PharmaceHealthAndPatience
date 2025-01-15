package com.pharmacy.intelrx.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StandardResponse<T> {
    private String status;
    private String message;
    private T data;

    public static <T> StandardResponse<T> success(String message, T data) {
        return StandardResponse.<T>builder()
                .status("success")
                .message(message)
                .data(data)
                .build();
    }

    public static <T> StandardResponse<T> success( T data) {
        return StandardResponse.<T>builder()
                .status("success")
                .data(data)
                .build();
    }

    public static <T> StandardResponse<T> success(String message) {
        return StandardResponse.<T>builder()
                .status("success")
                .message(message)
                .build();
    }

    public static <T> StandardResponse<T> error(String message) {
        return StandardResponse.<T>builder()
                .status("error")
                .message(message)
                .data(null)
                .build();
    }
}
