package com.toby.urlshortener.service;

import com.toby.urlshortener.exception.ShortCodeNotFoundException;
import com.toby.urlshortener.model.Url;
import com.toby.urlshortener.repository.UrlRepository;
import com.toby.urlshortener.util.ShortCodeGenerator;
import org.springframework.stereotype.Service;

@Service
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    private final ShortCodeGenerator shortCodeGenerator;

    public UrlServiceImpl(UrlRepository urlRepository,
                          ShortCodeGenerator shortCodeGenerator) {
        this.urlRepository = urlRepository;
        this.shortCodeGenerator = shortCodeGenerator;
    }

    @Override
    public Url createShortUrl(String url) {
        String shortCode = shortCodeGenerator.generate();

        while (urlRepository.existsByShortCode(shortCode)) {
            shortCode = shortCodeGenerator.generate();
        }

        Url newUrl = new Url();
        newUrl.setUrl(url);
        newUrl.setShortCode(shortCode);
        newUrl.setAccessCount(0L);

        return urlRepository.save(newUrl);
    }

    @Override
    public Url getByShortCode(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException("Short code not found: " + shortCode));
    }

    @Override
    public Url updateUrl(String shortCode, String newUrl) {
        Url existingUrl = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException("Short code not found: " + shortCode));

        existingUrl.setUrl(newUrl);

        return urlRepository.save(existingUrl);
    }

    @Override
    public void deleteUrl(String shortCode) {
        Url existingUrl = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException("Short code not found: " + shortCode));

        urlRepository.delete(existingUrl);
    }

    @Override
    public Url getStats(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException("Short code not found: " + shortCode));
    }

    @Override
    public String getOriginalUrlAndIncreaseCount(String shortCode) {
        Url existingUrl = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException("Short code not found: " + shortCode));

        existingUrl.setAccessCount(existingUrl.getAccessCount() + 1);
        urlRepository.save(existingUrl);

        return existingUrl.getUrl();
    }
}