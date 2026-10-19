
package com.poc.tests;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.ISO87APackager;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JposParameterizedTest {

    private static final String HOST =
        System.getenv("TCP_HOST");

private static final int PORT =
        Integer.parseInt(
                System.getenv().getOrDefault("TCP_PORT", "8583")
        );

    @ParameterizedTest(name = "ISO8583 amount={0}, STAN={1}")
    @CsvSource({
        "000000002500, 004321",
        "000000005000, 004322",
        "000000010000, 004323"
    })
    void testIso8583Transaction(String amount, String stan)
            throws Exception {

        ISOMsg request = new ISOMsg();
        request.setPackager(new ISO87APackager());
        request.setMTI("0200");

        request.set(3, "189090");
        request.set(4, amount);
        request.set(11, stan);
        request.set(22, "030");
        request.set(25, "00");
        request.set(32, "63005555");
        request.set(41, "20260929");
        request.set(42, "6300555TESTSTOR");
        request.set(48, "SKU9001");
        request.set(49, "124");

        byte[] requestData = request.pack();

        System.out.println("\nTesting amount=" + amount
                + ", STAN=" + stan);

        try (Socket socket = new Socket()) {

            socket.connect(
                new InetSocketAddress(HOST, PORT), 10000);
            socket.setSoTimeout(90000);

            DataOutputStream output =
                new DataOutputStream(socket.getOutputStream());

            DataInputStream input =
                new DataInputStream(socket.getInputStream());

            // Send 2-byte length followed by ISO8583 bytes
            output.writeShort(requestData.length);
            output.write(requestData);
            output.flush();

            System.out.println("Request sent: "
                    + requestData.length + " bytes");

            int responseLength = input.readUnsignedShort();

            byte[] responseData = new byte[responseLength];
            input.readFully(responseData);

            ISOMsg response = new ISOMsg();
            response.setPackager(new ISO87APackager());
            response.unpack(responseData);

            // Validate the ISO8583 response
            assertEquals("0210", response.getMTI());
            assertEquals("189090", response.getString(3));
            assertEquals(amount, response.getString(4));
            assertEquals(stan, response.getString(11));
            assertEquals("00", response.getString(39));

            System.out.println("PASSED: amount=" + amount
                    + ", STAN=" + stan
                    + ", response code="
                    + response.getString(39));
        }
    }
}

