package com.toby.urlshortener.service;

import com.toby.urlshortener.exception.ShortCodeNotFoundException;
import com.toby.urlshortener.model.Url;
import com.toby.urlshortener.repository.UrlRepository;
import com.toby.urlshortener.util.ShortCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UrlServiceImplTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private ShortCodeGenerator shortCodeGenerator;

    @InjectMocks
    private UrlServiceImpl urlService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("createShortUrl - should create and save a new short URL successfully")
    void createShortUrl_success() {
        String originalUrl = "https://www.google.com";
        String generatedCode = "abc123";

        when(shortCodeGenerator.generate()).thenReturn(generatedCode);
        when(urlRepository.existsByShortCode(generatedCode)).thenReturn(false);

        Url savedUrl = createSampleUrl(1L, originalUrl, generatedCode, 0L);
        when(urlRepository.save(any(Url.class))).thenReturn(savedUrl);

        Url result = urlService.createShortUrl(originalUrl);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(originalUrl, result.getUrl());
        assertEquals(generatedCode, result.getShortCode());
        assertEquals(0L, result.getAccessCount());

        verify(shortCodeGenerator, times(1)).generate();
        verify(urlRepository, times(1)).existsByShortCode(generatedCode);
        verify(urlRepository, times(1)).save(any(Url.class));
    }

    @Test
    @DisplayName("createShortUrl - should regenerate short code when collision happens")
    void createShortUrl_collision_shouldRegenerateCode() {
        String originalUrl = "https://www.google.com";

        when(shortCodeGenerator.generate())
                .thenReturn("abc123")
                .thenReturn("xyz789");

        when(urlRepository.existsByShortCode("abc123")).thenReturn(true);
        when(urlRepository.existsByShortCode("xyz789")).thenReturn(false);

        Url savedUrl = createSampleUrl(1L, originalUrl, "xyz789", 0L);
        when(urlRepository.save(any(Url.class))).thenReturn(savedUrl);

        Url result = urlService.createShortUrl(originalUrl);

        assertNotNull(result);
        assertEquals("xyz789", result.getShortCode());

        verify(shortCodeGenerator, times(2)).generate();
        verify(urlRepository, times(1)).existsByShortCode("abc123");
        verify(urlRepository, times(1)).existsByShortCode("xyz789");
        verify(urlRepository, times(1)).save(any(Url.class));
    }

    @Test
    @DisplayName("createShortUrl - should save correct Url fields")
    void createShortUrl_shouldSaveCorrectFields() {
        String originalUrl = "https://www.github.com";
        String generatedCode = "gh1234";

        when(shortCodeGenerator.generate()).thenReturn(generatedCode);
        when(urlRepository.existsByShortCode(generatedCode)).thenReturn(false);

        Url savedUrl = createSampleUrl(1L, originalUrl, generatedCode, 0L);
        when(urlRepository.save(any(Url.class))).thenReturn(savedUrl);

        urlService.createShortUrl(originalUrl);

        ArgumentCaptor<Url> urlCaptor = ArgumentCaptor.forClass(Url.class);
        verify(urlRepository).save(urlCaptor.capture());

        Url capturedUrl = urlCaptor.getValue();
        assertEquals(originalUrl, capturedUrl.getUrl());
        assertEquals(generatedCode, capturedUrl.getShortCode());
        assertEquals(0L, capturedUrl.getAccessCount());
    }

    @Test
    @DisplayName("getByShortCode - should return URL without incrementing access count")
    void getByShortCode_success_shouldReturnUrlOnly() {
        Url existingUrl = createSampleUrl(1L, "https://www.google.com", "abc123", 6L);

        when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.of(existingUrl));

        Url result = urlService.getByShortCode("abc123");

        assertNotNull(result);
        assertEquals("abc123", result.getShortCode());
        assertEquals(6L, result.getAccessCount());

        verify(urlRepository, times(1)).findByShortCode("abc123");
        verify(urlRepository, never()).save(any(Url.class));
    }

    @Test
    @DisplayName("getByShortCode - should throw exception when short code not found")
    void getByShortCode_notFound_shouldThrowException() {
        when(urlRepository.findByShortCode("notfound")).thenReturn(Optional.empty());

        ShortCodeNotFoundException exception = assertThrows(
                ShortCodeNotFoundException.class,
                () -> urlService.getByShortCode("notfound")
        );

        assertEquals("Short code not found: notfound", exception.getMessage());

        verify(urlRepository, times(1)).findByShortCode("notfound");
        verify(urlRepository, never()).save(any(Url.class));
    }

    @Test
    @DisplayName("updateUrl - should update URL successfully")
    void updateUrl_success() {
        Url existingUrl = createSampleUrl(1L, "https://www.old.com", "abc123", 3L);

        when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.of(existingUrl));
        when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Url result = urlService.updateUrl("abc123", "https://www.new.com");

        assertNotNull(result);
        assertEquals("https://www.new.com", result.getUrl());
        assertEquals("abc123", result.getShortCode());
        assertEquals(3L, result.getAccessCount());

        verify(urlRepository, times(1)).findByShortCode("abc123");
        verify(urlRepository, times(1)).save(existingUrl);
    }

    @Test
    @DisplayName("updateUrl - should throw exception when short code not found")
    void updateUrl_notFound_shouldThrowException() {
        when(urlRepository.findByShortCode("notfound")).thenReturn(Optional.empty());

        ShortCodeNotFoundException exception = assertThrows(
                ShortCodeNotFoundException.class,
                () -> urlService.updateUrl("notfound", "https://www.new.com")
        );

        assertEquals("Short code not found: notfound", exception.getMessage());

        verify(urlRepository, times(1)).findByShortCode("notfound");
        verify(urlRepository, never()).save(any(Url.class));
    }

    @Test
    @DisplayName("deleteUrl - should delete successfully")
    void deleteUrl_success() {
        Url existingUrl = createSampleUrl(1L, "https://www.google.com", "abc123", 2L);

        when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.of(existingUrl));
        doNothing().when(urlRepository).delete(existingUrl);

        assertDoesNotThrow(() -> urlService.deleteUrl("abc123"));

        verify(urlRepository, times(1)).findByShortCode("abc123");
        verify(urlRepository, times(1)).delete(existingUrl);
    }

    @Test
    @DisplayName("deleteUrl - should throw exception when short code not found")
    void deleteUrl_notFound_shouldThrowException() {
        when(urlRepository.findByShortCode("notfound")).thenReturn(Optional.empty());

        ShortCodeNotFoundException exception = assertThrows(
                ShortCodeNotFoundException.class,
                () -> urlService.deleteUrl("notfound")
        );

        assertEquals("Short code not found: notfound", exception.getMessage());

        verify(urlRepository, times(1)).findByShortCode("notfound");
        verify(urlRepository, never()).delete(any(Url.class));
    }

    @Test
    @DisplayName("getStats - should return URL stats successfully")
    void getStats_success() {
        Url existingUrl = createSampleUrl(1L, "https://www.google.com", "abc123", 8L);

        when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.of(existingUrl));

        Url result = urlService.getStats("abc123");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("https://www.google.com", result.getUrl());
        assertEquals("abc123", result.getShortCode());
        assertEquals(8L, result.getAccessCount());

        verify(urlRepository, times(1)).findByShortCode("abc123");
        verify(urlRepository, never()).save(any(Url.class));
    }

    @Test
    @DisplayName("getStats - should throw exception when short code not found")
    void getStats_notFound_shouldThrowException() {
        when(urlRepository.findByShortCode("notfound")).thenReturn(Optional.empty());

        ShortCodeNotFoundException exception = assertThrows(
                ShortCodeNotFoundException.class,
                () -> urlService.getStats("notfound")
        );

        assertEquals("Short code not found: notfound", exception.getMessage());

        verify(urlRepository, times(1)).findByShortCode("notfound");
        verify(urlRepository, never()).save(any(Url.class));
    }

    @Test
    @DisplayName("getOriginalUrlAndIncreaseCount - should increment access count and return original URL")
    void getOriginalUrlAndIncreaseCount_success() {
        Url existingUrl = createSampleUrl(1L, "https://www.google.com", "abc123", 6L);

        when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.of(existingUrl));
        when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = urlService.getOriginalUrlAndIncreaseCount("abc123");

        assertEquals("https://www.google.com", result);
        assertEquals(7L, existingUrl.getAccessCount());

        verify(urlRepository, times(1)).findByShortCode("abc123");
        verify(urlRepository, times(1)).save(existingUrl);
    }

    @Test
    @DisplayName("getOriginalUrlAndIncreaseCount - should throw exception when short code not found")
    void getOriginalUrlAndIncreaseCount_notFound_shouldThrowException() {
        when(urlRepository.findByShortCode("notfound")).thenReturn(Optional.empty());

        ShortCodeNotFoundException exception = assertThrows(
                ShortCodeNotFoundException.class,
                () -> urlService.getOriginalUrlAndIncreaseCount("notfound")
        );

        assertEquals("Short code not found: notfound", exception.getMessage());

        verify(urlRepository, times(1)).findByShortCode("notfound");
        verify(urlRepository, never()).save(any(Url.class));
    }

    private Url createSampleUrl(Long id, String url, String shortCode, Long accessCount) {
        Url sample = new Url();
        sample.setId(id);
        sample.setUrl(url);
        sample.setShortCode(shortCode);
        sample.setAccessCount(accessCount);
        sample.setCreatedAt(LocalDateTime.now());
        sample.setUpdatedAt(LocalDateTime.now());
        return sample;
    }
}