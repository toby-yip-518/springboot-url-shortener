package com.toby.urlshortener.controller;

import com.toby.urlshortener.dto.CreateUrlRequest;
import com.toby.urlshortener.dto.UrlResponse;
import com.toby.urlshortener.dto.UrlStatsResponse;
import com.toby.urlshortener.model.Url;
import com.toby.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shorten")
@Tag(name = "URL Shortener", description = "Endpoints for managing shortened URLs")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a short URL", description = "Creates a new shortened URL from the provided original URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Short URL created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or URL format")
    })
    public UrlResponse createShortURL(@Valid @RequestBody CreateUrlRequest request) {
        Url url = urlService.createShortUrl(request.getUrl());
        return toUrlResponse(url);
    }

    @GetMapping("/{shortCode}")
    @Operation(summary = "Get original URL by short code", description = "Retrieves the URL by short code and increments access count")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL found"),
            @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    public UrlResponse getUrl(@PathVariable String shortCode) {
        Url url = urlService.getByShortCode(shortCode);
        return toUrlResponse(url);
    }

    @PutMapping("/{shortCode}")
    @Operation(summary = "Update URL", description = "Updates the original URL for a given short code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or URL format"),
            @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    public UrlResponse updateUrl(@PathVariable String shortCode,
                                 @Valid @RequestBody CreateUrlRequest request) {
        Url url = urlService.updateUrl(shortCode, request.getUrl());
        return toUrlResponse(url);
    }

    @DeleteMapping("/{shortCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete URL", description = "Deletes the URL mapping for a given short code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "URL deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    public void deleteUrl(@PathVariable String shortCode) {
        urlService.deleteUrl(shortCode);
    }

    @GetMapping("/{shortCode}/stats")
    @Operation(summary = "Get URL statistics", description = "Returns metadata and access count for a given short code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stats retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    public UrlStatsResponse getStats(@PathVariable String shortCode) {
        Url url = urlService.getStats(shortCode);
        return toUrlStatsResponse(url);
    }

    private UrlResponse toUrlResponse(Url url) {
        return new UrlResponse(
                url.getId(),
                url.getUrl(),
                url.getShortCode(),
                url.getCreatedAt(),
                url.getUpdatedAt()
        );
    }

    private UrlStatsResponse toUrlStatsResponse(Url url) {
        return new UrlStatsResponse(
                url.getId(),
                url.getUrl(),
                url.getShortCode(),
                url.getCreatedAt(),
                url.getUpdatedAt(),
                url.getAccessCount()
        );
    }
}