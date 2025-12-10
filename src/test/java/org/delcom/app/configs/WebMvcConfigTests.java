package org.delcom.app.configs;

import org.delcom.app.interceptors.AuthInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WebMvcConfigTests {

    @Mock
    private AuthInterceptor authInterceptor;

    @InjectMocks
    private WebMvcConfig webMvcConfig;

    public WebMvcConfigTests() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Registers AuthInterceptor correctly")
    void testAddInterceptors() {
        InterceptorRegistry registry = mock(InterceptorRegistry.class);
        InterceptorRegistration registration = mock(InterceptorRegistration.class);

        when(registry.addInterceptor(any(AuthInterceptor.class))).thenReturn(registration);
        when(registration.addPathPatterns(anyString())).thenReturn(registration);
        when(registration.excludePathPatterns(anyString())).thenReturn(registration);
        when(registration.excludePathPatterns(any(String[].class))).thenReturn(registration);

        webMvcConfig.addInterceptors(registry);

        verify(registry).addInterceptor(authInterceptor);
        verify(registration).addPathPatterns("/**");
    }

    @Test
    @DisplayName("Registers Resource Handlers correctly")
    void testAddResourceHandlers() {
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);

        when(registry.addResourceHandler(anyString())).thenReturn(registration);
        when(registration.addResourceLocations(anyString())).thenReturn(registration);

        webMvcConfig.addResourceHandlers(registry);

        verify(registry).addResourceHandler("/uploads/**");
        verify(registration).addResourceLocations("file:./uploads/");
    }
}
