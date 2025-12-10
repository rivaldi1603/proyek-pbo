package org.delcom.app.configs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTests {

    @Test
    @DisplayName("Menggunakan konstruktor ApiResponse dengan benar")
    void testMenggunakanKonstruktorApiResponse() {
        ApiResponse<String> response = new ApiResponse<>("success", "Operasi berhasil", "Data hasil");

        assertEquals("success", response.getStatus());
        assertEquals("Operasi berhasil", response.getMessage());
        assertEquals("Data hasil", response.getData());
    }

    @Test
    @DisplayName("Menguji ApiResponse dengan data null")
    void testApiResponseDenganDataNull() {
        ApiResponse<Object> response = new ApiResponse<>("fail", "Terjadi kesalahan", null);

        assertEquals("fail", response.getStatus());
        assertEquals("Terjadi kesalahan", response.getMessage());
        assertNull(response.getData());
    }
}
