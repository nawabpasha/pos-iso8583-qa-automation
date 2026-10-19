package com.poc.tests;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.ISO87APackager;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JposTcpTest {

   private static final String HOST =
        System.getenv("TCP_HOST");

private static final int PORT =
        Integer.parseInt(
                System.getenv().getOrDefault("TCP_PORT", "8583")
        );

    @Test
    void testJposMessageOverTcp() throws Exception {

        // Create ISO8583 request
        ISOMsg request = new ISOMsg();
        request.setPackager(new ISO87APackager());

        request.setMTI("0200");

        request.set(3, "189090");
        request.set(4, "000000002500");
        request.set(11, "004321");
        request.set(22, "030");
        request.set(25, "0");
        request.set(32, "63005555");
        request.set(41, "20260929");
        request.set(42, "6300555TESTSTOR");
        request.set(48, "SKU9001");
        request.set(49, "124");
        

        byte[] requestData = request.pack();

        System.out.println("\nJPOS CLIENT REQUEST:");
        request.dump(System.out, "");

        System.out.println(
                "\nConnecting to jPOS server "
                        + HOST + ":" + PORT);

        try (Socket socket = new Socket(HOST, PORT)) {
                socket.setSoTimeout(90000);
            DataOutputStream output =
                    new DataOutputStream(socket.getOutputStream());

            DataInputStream input =
                    new DataInputStream(socket.getInputStream());

            // Send length + ISO8583 message
            output.writeShort(requestData.length);
            output.write(requestData);
            output.flush();

            System.out.println(
                    "Sent ISO8583 request: "
                            + requestData.length
                            + " bytes");

            // Receive response length
            int responseLength = input.readUnsignedShort();

            System.out.println(
                    "Received ISO8583 response length: "
                            + responseLength);

            // Read response
            byte[] responseData = new byte[responseLength];

            input.readFully(responseData);

            // Unpack response
            ISOMsg response = new ISOMsg();
            response.setPackager(new ISO87APackager());
            response.unpack(responseData);

            System.out.println("\nJPOS CLIENT RESPONSE:");
            response.dump(System.out, "");

            // Validate response
            assertEquals("0210", response.getMTI());

            assertEquals(
                    "189090",
                    response.getString(3));

            assertEquals(
                    "000000002500",
                    response.getString(4));

            assertEquals(
                    "004321",
                    response.getString(11));

            assertEquals(
                    "00",
                    response.getString(39));

            System.out.println(
                    "\n0210 response validation PASSED.");

        }
    }
}
