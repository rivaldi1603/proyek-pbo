package org.delcom.app.configs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class StartupInfoLoggerTests {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    private StartupInfoLogger logger;
    private ApplicationReadyEvent event;
    private ConfigurableEnvironment environment;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        logger = new StartupInfoLogger();

        event = mock(ApplicationReadyEvent.class);
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        environment = mock(ConfigurableEnvironment.class);

        when(event.getApplicationContext()).thenReturn(context);
        when(context.getEnvironment()).thenReturn(environment);
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("Displays default startup info")
    void testDefaultStartupInfo() {
        when(environment.getProperty("server.port", "8080")).thenReturn("8080");
        when(environment.getProperty("server.servlet.context-path", "/")).thenReturn("/");
        when(environment.getProperty("spring.devtools.livereload.enabled", Boolean.class, false)).thenReturn(false);
        when(environment.getProperty("server.address", "localhost")).thenReturn("localhost");

        logger.onApplicationEvent(event);

        String output = outContent.toString();
        assertTrue(output.contains("Application started successfully!"));
        assertTrue(output.contains("http://localhost:8080"));
        assertTrue(output.contains("LiveReload: DISABLED"));
    }

    @Test
    @DisplayName("Displays context path and LiveReload enabled")
    void testCustomContextAndLiveReload() {
        when(environment.getProperty("server.port", "8080")).thenReturn("9090");
        when(environment.getProperty("server.servlet.context-path", "/")).thenReturn("/app");
        when(environment.getProperty("spring.devtools.livereload.enabled", Boolean.class, false)).thenReturn(true);
        when(environment.getProperty("spring.devtools.livereload.port", "35729")).thenReturn("12345");
        when(environment.getProperty("server.address", "localhost")).thenReturn("127.0.0.1");

        logger.onApplicationEvent(event);

        String output = outContent.toString();
        assertTrue(output.contains("http://127.0.0.1:9090/app"));
        // Assuming the logger prints ENABLED (port 12345)
        assertTrue(output.contains("LiveReload: ENABLED (port 12345)"));
    }

    @Test
    @DisplayName("Handles null context path gracefully")
    void testNullContextPath() {
        when(environment.getProperty("server.port", "8080")).thenReturn("8080");
        when(environment.getProperty("server.servlet.context-path", "/")).thenReturn(null);
        when(environment.getProperty("spring.devtools.livereload.enabled", Boolean.class, false)).thenReturn(false);
        when(environment.getProperty("server.address", "localhost")).thenReturn("localhost");

        logger.onApplicationEvent(event);

        String output = outContent.toString();
        // Should print http://localhost:8080 without "null" appended
        assertTrue(output.contains("http://localhost:8080"));
        assertTrue(!output.contains("http://localhost:8080null"));
    }
}
