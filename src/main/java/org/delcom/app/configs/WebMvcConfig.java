package org.delcom.app.configs;

import org.delcom.app.interceptors.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**") // Terapkan ke semua endpoint
                .excludePathPatterns("/api/auth/**", "/auth/**") // Kecuali endpoint auth
                .excludePathPatterns("/api/public/**") // Dan endpoint public
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/uploads/**", "/webjars/**"); // Dan static
                                                                                                       // resources
    }

    @Override
    public void addResourceHandlers(
            org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }
}