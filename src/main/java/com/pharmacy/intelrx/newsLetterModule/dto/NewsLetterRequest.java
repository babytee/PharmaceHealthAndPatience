package com.pharmacy.intelrx.newsLetterModule.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewsLetterRequest {
    private Long id;

    @NotBlank(message = "newsType cannot be null or empty")
    @Pattern(regexp = "EMAIL|SMS", message = "News type must be either EMAIL or SMS")
    private String newsType; // EMAIL, SMS

    @NotBlank(message = "sendTo cannot be null or empty")
    @Pattern(regexp = "Wholesalers|Retailers", message = "Send to must be either Wholesalers or Retailers")
    private String sendTo; // Wholesalers, Retailers

    @NotBlank(message = "Schedule time type cannot be null or empty")
    @Pattern(regexp = "Immediately|Later", message = "Schedule time type must be either Immediately or Later")
    private String scheduleTimeType; // Immediately, Later

    @NotBlank(message = "newsStatus cannot be null or empty")
    @Pattern(regexp = "LIVE|DRAFT", message = "Schedule time type must be either Immediately or Later")
    private String newsStatus; // Immediately, Later

    private LocalDateTime scheduleTime;

    @NotBlank(message = "newsLetterTitle cannot be null or empty")
    private String newsLetterTitle;

//    @NotNull(message = "adminId cannot be null")
//    private Long adminId;

    @NotBlank(message = "content cannot be null or empty")
    private String content;

    private LocalDateTime createdAt;
}
