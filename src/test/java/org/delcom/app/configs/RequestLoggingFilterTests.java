package org.delcom.app.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class RequestLoggingFilterTests {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("Log Cyan for status < 200 (e.g. 100)")
    void testLogCyanFor100() throws ServletException, IOException {
        RequestLoggingFilter filter = new RequestLoggingFilter();
        ReflectionTestUtils.setField(filter, "port", 8080);
        ReflectionTestUtils.setField(filter, "livereload", false);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/info");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(response.getStatus()).thenReturn(100);

        filter.doFilterInternal(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        // \u001B[36m is Cyan
        assertTrue(outContent.toString().contains("\u001B[36m"));
    }

    @Test
    @DisplayName("Log Green for status 200-399")
    void testLogGreenFor200() throws ServletException, IOException {
        RequestLoggingFilter filter = new RequestLoggingFilter();
        ReflectionTestUtils.setField(filter, "port", 8080);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/data");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(response.getStatus()).thenReturn(201);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        // \u001B[32m is Green
        assertTrue(outContent.toString().contains("\u001B[32m"));
    }

    @Test
    @DisplayName("Log Yellow for status 400-499")
    void testLogYellowFor404() throws ServletException, IOException {
        RequestLoggingFilter filter = new RequestLoggingFilter();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/missing");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(response.getStatus()).thenReturn(404);

        filter.doFilterInternal(request, response, chain);
        // \u001B[33m is Yellow
        assertTrue(outContent.toString().contains("\u001B[33m"));
    }

    @Test
    @DisplayName("Log Red for status >= 500")
    void testLogRedFor500() throws ServletException, IOException {
        RequestLoggingFilter filter = new RequestLoggingFilter();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/crash");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(response.getStatus()).thenReturn(500);

        filter.doFilterInternal(request, response, chain);
        // \u001B[31m is Red
        assertTrue(outContent.toString().contains("\u001B[31m"));
    }

    @Test
    @DisplayName("Skip logging for .well-known URIs")
    void testSkipWellKnown() throws ServletException, IOException {
        RequestLoggingFilter filter = new RequestLoggingFilter();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/.well-known/acme-challenge");
        when(response.getStatus()).thenReturn(200);

        filter.doFilterInternal(request, response, chain);

        // Should not print to stdout
        assertTrue(outContent.toString().isEmpty());
    }
}
