package com.toby.urlshortener.service;

import com.toby.urlshortener.model.Url;

public interface UrlService {

    Url createShortUrl(String url);

    Url getByShortCode(String shortCode);

    Url updateUrl(String shortCode, String newUrl);

    void deleteUrl(String shortCode);

    Url getStats(String shortCode);

    String getOriginalUrlAndIncreaseCount(String shortCode);
}