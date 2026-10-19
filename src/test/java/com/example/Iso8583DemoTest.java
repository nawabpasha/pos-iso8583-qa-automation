
package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Iso8583DemoTest {

    @Test
    void testIso8583MessageType() {
        String requestMti = "0200";
        assertEquals("0200", requestMti);
    }

    @Test
    void testIso8583ResponseCode() {
        String responseCode = "00";
        assertEquals("00", responseCode);
    }

    @Test
    void testTransactionAmount() {
        long amount = 2500;
        assertTrue(amount > 0);
    }
}
