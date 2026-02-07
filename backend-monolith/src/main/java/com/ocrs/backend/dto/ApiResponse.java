package com.ocrs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        /**
         * Create a successful ApiResponse containing a message and payload.
         *
         * @param message human-readable message describing the result
         * @param data payload of the response
         * @param <T> type of the payload
         * @return an ApiResponse with success set to true, the provided message, and the provided data
         */
        public static <T> ApiResponse<T> success(String message, T data) {
                return ApiResponse.<T>builder()
                                .success(true)
                                .message(message)
                                .data(data)
                                .build();
        }

        /**
         * Create a successful ApiResponse with the given message and no data.
         *
         * @param message a human-readable message describing the success
         * @return an ApiResponse with {@code success} set to {@code true}, the provided message, and {@code null} data
         */
        public static <T> ApiResponse<T> success(String message) {
                return ApiResponse.<T>builder()
                                .success(true)
                                .message(message)
                                .build();
        }

        /**
         * Create an error ApiResponse populated with the provided message.
         *
         * @param message the error message describing the failure
         * @param <T> the type of the response payload
         * @return an ApiResponse with success set to false, the provided message, and no data
         */
        public static <T> ApiResponse<T> error(String message) {
                return ApiResponse.<T>builder()
                                .success(false)
                                .message(message)
                                .build();
        }
}