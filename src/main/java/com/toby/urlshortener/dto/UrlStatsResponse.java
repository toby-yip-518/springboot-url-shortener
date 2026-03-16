package com.toby.urlshortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public class UrlStatsResponse {

    @Schema(
            description = "Error Response",
            example = "id: 1, url: https://www.youtube.com, shortCode: eD33Nq, " +
                    "createdAt: 2026-03-13T15:12:12, updatedAt: 2026-03-14T14:37:26, " +
                    "accessCount: 3"
    )

    private Long id;
    private String url;
    private String shortCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long accessCount;

    public UrlStatsResponse() {
    }

    public UrlStatsResponse(Long id, String url, String shortCode,
                            LocalDateTime createdAt, LocalDateTime updatedAt,
                            Long accessCount) {
        this.id = id;
        this.url = url;
        this.shortCode = shortCode;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.accessCount = accessCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(Long accessCount) {
        this.accessCount = accessCount;
    }
}