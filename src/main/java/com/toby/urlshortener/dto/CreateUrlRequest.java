package com.toby.urlshortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public class CreateUrlRequest {

    @Schema(
            description = "Original URL to shorten",
            example = "https://www.google.com"
    )
    @NotBlank(message = "URL must not be blank")
    @URL(message = "URL format is invalid")
    private String url;

    public CreateUrlRequest() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}