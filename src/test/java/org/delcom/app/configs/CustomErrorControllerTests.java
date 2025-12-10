package org.delcom.app.configs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

class CustomErrorControllerTests {

        @Test
        @DisplayName("Handle Error returns 500 when status is missing or 500")
        void testHandleErrorReturns500() {
                // Mock dependencies
                ErrorAttributes errorAttributes = Mockito.mock(ErrorAttributes.class);
                // Default behavior: returns empty map -> status defaults to 500
                Mockito.when(errorAttributes.getErrorAttributes(any(ServletWebRequest.class),
                                any(ErrorAttributeOptions.class)))
                                .thenReturn(Map.of());

                CustomErrorController controller = new CustomErrorController(errorAttributes);
                ServletWebRequest webRequest = new ServletWebRequest(Mockito.mock(HttpServletRequest.class),
                                Mockito.mock(HttpServletResponse.class));

                ResponseEntity<Map<String, Object>> response = controller.handleError(webRequest);

                assertEquals(500, response.getStatusCode().value());
                assertEquals("error", response.getBody().get("status"));
                assertEquals("Unknown Error", response.getBody().get("error"));
                assertEquals("unknown", response.getBody().get("path"));
        }

        @Test
        @DisplayName("Handle Error returns 404 correctly")
        void testHandleErrorReturns404() {
                ErrorAttributes errorAttributes = Mockito.mock(ErrorAttributes.class);
                Map<String, Object> attributes = Map.of(
                                "status", 404,
                                "error", "Not Found",
                                "path", "/missing");
                Mockito.when(errorAttributes.getErrorAttributes(any(ServletWebRequest.class),
                                any(ErrorAttributeOptions.class)))
                                .thenReturn(attributes);

                CustomErrorController controller = new CustomErrorController(errorAttributes);
                ServletWebRequest webRequest = new ServletWebRequest(Mockito.mock(HttpServletRequest.class),
                                Mockito.mock(HttpServletResponse.class));

                ResponseEntity<Map<String, Object>> response = controller.handleError(webRequest);

                assertEquals(404, response.getStatusCode().value());
                assertEquals("fail", response.getBody().get("status"));
                assertEquals("Not Found", response.getBody().get("error"));
                assertEquals("/missing", response.getBody().get("path"));
        }
}
