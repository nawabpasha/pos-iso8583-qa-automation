
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

    private static final String HOST = System.getenv("TCP_HOST");

    private static final int PORT = Integer.parseInt(
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

        System.out.println("\n==================================");
        System.out.println("ISO8583 TEST CASE");
        System.out.println("Amount: " + amount);
        System.out.println("STAN: " + stan);
        System.out.println("==================================");

        System.out.println("\n----- ISO8583 REQUEST -----");
        printMessage(request);

        byte[] requestData = request.pack();

        try (Socket socket = new Socket()) {

            socket.connect(
                new InetSocketAddress(HOST, PORT), 10000
            );
            socket.setSoTimeout(90000);

            DataOutputStream output =
                new DataOutputStream(socket.getOutputStream());

            DataInputStream input =
                new DataInputStream(socket.getInputStream());

            output.writeShort(requestData.length);
            output.write(requestData);
            output.flush();

            System.out.println("\nRequest sent: "
                    + requestData.length + " bytes");

            int responseLength = input.readUnsignedShort();

            byte[] responseData = new byte[responseLength];
            input.readFully(responseData);

            ISOMsg response = new ISOMsg();
            response.setPackager(new ISO87APackager());
            response.unpack(responseData);

            System.out.println("\n----- ISO8583 RESPONSE -----");
            printMessage(response);

            assertEquals("0210", response.getMTI(), "Invalid MTI");
            assertEquals("189090", response.getString(3),
                    "Processing code mismatch");
            assertEquals(amount, response.getString(4),
                    "Amount mismatch");
            assertEquals(stan, response.getString(11),
                    "STAN mismatch");
            assertEquals("00", response.getString(39),
                    "Transaction response code mismatch");

            System.out.println("\nVALIDATION: PASSED");
            System.out.println("MTI: " + response.getMTI());
            System.out.println("DE039: " + response.getString(39));
            System.out.println("==================================\n");
        }
    }

    private static void printMessage(ISOMsg message)
            throws Exception {

        System.out.println("MTI = " + message.getMTI());

        for (int field = 1; field <= message.getMaxField(); field++) {
            if (message.hasField(field)) {
                String value = message.getString(field);

                // Mask potentially sensitive card data.
                if (field == 2 || field == 35
                        || field == 45 || field == 52
                        || field == 55) {
                    value = "[MASKED]";
                }

                System.out.printf(
                    "DE%03d = %s%n", field, value
                );
            }
        }
    }
}
