package com.toby.urlshortener.controller;

import com.toby.urlshortener.exception.GlobalExceptionHandler;
import com.toby.urlshortener.exception.ShortCodeNotFoundException;
import com.toby.urlshortener.service.UrlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RedirectController.class)
@Import(GlobalExceptionHandler.class)
class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    @Test
    @DisplayName("GET /r/{shortCode} - should redirect to original URL successfully")
    void redirectToOriginalUrl_success() throws Exception {
        given(urlService.getOriginalUrlAndIncreaseCount("abc123"))
                .willReturn("https://www.google.com");

        mockMvc.perform(get("/r/abc123"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "https://www.google.com"));

        verify(urlService).getOriginalUrlAndIncreaseCount("abc123");
    }

    @Test
    @DisplayName("GET /r/{shortCode} - should redirect to another valid URL successfully")
    void redirectToOriginalUrl_success_anotherUrl() throws Exception {
        given(urlService.getOriginalUrlAndIncreaseCount("gh789x"))
                .willReturn("https://github.com");

        mockMvc.perform(get("/r/gh789x"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "https://github.com"));

        verify(urlService).getOriginalUrlAndIncreaseCount("gh789x");
    }

    @Test
    @DisplayName("GET /r/{shortCode} - should return 404 when short code not found")
    void redirectToOriginalUrl_notFound() throws Exception {
        given(urlService.getOriginalUrlAndIncreaseCount("nf1234"))
                .willThrow(new ShortCodeNotFoundException("Short code not found: nf1234"));

        mockMvc.perform(get("/r/nf1234"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Short code not found: nf1234"))
                .andExpect(jsonPath("$.path").value("/r/nf1234"));

        verify(urlService).getOriginalUrlAndIncreaseCount("nf1234");
    }

    @Test
    @DisplayName("GET /r/{shortCode} - should return 500 when stored URL is invalid")
    void redirectToOriginalUrl_invalidStoredUrl() throws Exception {
        given(urlService.getOriginalUrlAndIncreaseCount("badurl"))
                .willReturn("ht!tp://invalid-url");

        mockMvc.perform(get("/r/badurl"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.path").value("/r/badurl"));

        verify(urlService).getOriginalUrlAndIncreaseCount("badurl");
    }
}