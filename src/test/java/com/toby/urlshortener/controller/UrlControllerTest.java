package com.toby.urlshortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toby.urlshortener.dto.CreateUrlRequest;
import com.toby.urlshortener.exception.GlobalExceptionHandler;
import com.toby.urlshortener.exception.ShortCodeNotFoundException;
import com.toby.urlshortener.model.Url;
import com.toby.urlshortener.service.UrlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UrlController.class)
@Import(GlobalExceptionHandler.class)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UrlService urlService;

    @Test
    @DisplayName("POST /shorten - should create short URL successfully")
    void createShortUrl_success() throws Exception {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setUrl("https://www.google.com");

        Url savedUrl = createSampleUrl(
                1L,
                "https://www.google.com",
                "abc123",
                0L
        );

        given(urlService.createShortUrl(anyString())).willReturn(savedUrl);

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.url").value("https://www.google.com"))
                .andExpect(jsonPath("$.shortCode").value("abc123"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("POST /shorten - should return 400 when URL is blank")
    void createShortUrl_blankUrl_shouldReturnBadRequest() throws Exception {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setUrl("");

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("URL must not be blank"))
                .andExpect(jsonPath("$.path").value("/shorten"));
    }

    @Test
    @DisplayName("POST /shorten - should return 400 when URL format is invalid")
    void createShortUrl_invalidUrl_shouldReturnBadRequest() throws Exception {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setUrl("abc");

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("URL format is invalid"))
                .andExpect(jsonPath("$.path").value("/shorten"));
    }

    @Test
    @DisplayName("GET /shorten/{shortCode} - should return URL successfully")
    void getUrl_success() throws Exception {
        Url url = createSampleUrl(
                1L,
                "https://www.google.com",
                "abc123",
                1L
        );

        given(urlService.getByShortCode("abc123")).willReturn(url);

        mockMvc.perform(get("/shorten/abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.url").value("https://www.google.com"))
                .andExpect(jsonPath("$.shortCode").value("abc123"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andExpect(jsonPath("$.accessCount").doesNotExist());
    }

    @Test
    @DisplayName("GET /shorten/{shortCode} - should return 404 when short code not found")
    void getUrl_notFound() throws Exception {
        given(urlService.getByShortCode("notfound"))
                .willThrow(new ShortCodeNotFoundException("Short code not found: notfound"));

        mockMvc.perform(get("/shorten/notfound"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Short code not found: notfound"))
                .andExpect(jsonPath("$.path").value("/shorten/notfound"));
    }

    @Test
    @DisplayName("PUT /shorten/{shortCode} - should update URL successfully")
    void updateUrl_success() throws Exception {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setUrl("https://www.github.com");

        Url updatedUrl = createSampleUrl(
                1L,
                "https://www.github.com",
                "abc123",
                3L
        );

        given(urlService.updateUrl(eq("abc123"), eq("https://www.github.com")))
                .willReturn(updatedUrl);

        mockMvc.perform(put("/shorten/abc123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.url").value("https://www.github.com"))
                .andExpect(jsonPath("$.shortCode").value("abc123"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andExpect(jsonPath("$.accessCount").doesNotExist());
    }

    @Test
    @DisplayName("PUT /shorten/{shortCode} - should return 400 when URL is invalid")
    void updateUrl_invalidUrl_shouldReturnBadRequest() throws Exception {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setUrl("invalid-url");

        mockMvc.perform(put("/shorten/abc123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("URL format is invalid"))
                .andExpect(jsonPath("$.path").value("/shorten/abc123"));
    }

    @Test
    @DisplayName("PUT /shorten/{shortCode} - should return 404 when short code not found")
    void updateUrl_notFound() throws Exception {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setUrl("https://www.github.com");

        given(urlService.updateUrl(eq("notfound"), eq("https://www.github.com")))
                .willThrow(new ShortCodeNotFoundException("Short code not found: notfound"));

        mockMvc.perform(put("/shorten/notfound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Short code not found: notfound"))
                .andExpect(jsonPath("$.path").value("/shorten/notfound"));
    }

    @Test
    @DisplayName("DELETE /shorten/{shortCode} - should delete successfully")
    void deleteUrl_success() throws Exception {
        willDoNothing().given(urlService).deleteUrl("abc123");

        mockMvc.perform(delete("/shorten/abc123"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /shorten/{shortCode} - should return 404 when short code not found")
    void deleteUrl_notFound() throws Exception {
        org.mockito.BDDMockito.willThrow(
                new ShortCodeNotFoundException("Short code not found: notfound")
        ).given(urlService).deleteUrl("notfound");

        mockMvc.perform(delete("/shorten/notfound"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Short code not found: notfound"))
                .andExpect(jsonPath("$.path").value("/shorten/notfound"));
    }

    @Test
    @DisplayName("GET /shorten/{shortCode}/stats - should return stats successfully")
    void getStats_success() throws Exception {
        Url url = createSampleUrl(
                1L,
                "https://www.google.com",
                "abc123",
                5L
        );

        given(urlService.getStats("abc123")).willReturn(url);

        mockMvc.perform(get("/shorten/abc123/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.url").value("https://www.google.com"))
                .andExpect(jsonPath("$.shortCode").value("abc123"))
                .andExpect(jsonPath("$.accessCount").value(5))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("GET /shorten/{shortCode}/stats - should return 404 when short code not found")
    void getStats_notFound() throws Exception {
        given(urlService.getStats("notfound"))
                .willThrow(new ShortCodeNotFoundException("Short code not found: notfound"));

        mockMvc.perform(get("/shorten/notfound/stats"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Short code not found: notfound"))
                .andExpect(jsonPath("$.path").value("/shorten/notfound/stats"));
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