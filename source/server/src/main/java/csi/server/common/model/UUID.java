package csi.server.common.model;

import java.util.Random;

public class UUID {

    private static final Random random = new Random();

    public static int stringLength() {

        return 36;
    }
    /**
     * pass through method for compatibility with java.util.UUID
     * @author bmurray
     */
    public static String randomUUID() {
        return uuid();
    }

    /**
     * Generate a RFC4122, version 4 using random numbers
     * @author Patrick
     */
    public static String uuid() {
        char[] uuid = new char[36];
        byte[] bytes = new byte[16];

        // Set all the other bits to randomly (or pseudo-randomly) chosen
        // values.
        random.nextBytes(bytes);

        // Set the two most significant bits (bits 6 and 7) of the
        // clock_seq_hi_and_reserved to zero and one, respectively.
        bytes[8] = (byte) ((bytes[8] & 0xBF) | 0x80);

        // Set the four most significant bits (bits 12 through 15) of the
        // time_hi_and_version field to the 4-bit version number from
        // Section 4.1.3.
        bytes[6] = (byte) ((bytes[6] & 0xF) | 0x40);
        char[] chars = encodeBytes(bytes);
        // rfc4122 requires these characters
        uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
        int charsIndex = 0;
        for (int i = 0; i < 36; i++) {
            if (uuid[i] == '\0') {
                uuid[i] = chars[charsIndex];
                charsIndex++;
            }
        }
        return new String(uuid);
    }

    private final static char[] encodingArray = {//
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private static char[] encodeBytes(byte[] bytes) {
        char[] encoded = new char[32];
        int j = 0;
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xff;
            encoded[j] = encodingArray[(v >>> 4)];
            j++;
            encoded[j] = encodingArray[(v & 0xf)];
            j++;
        }
        return encoded;
    }
}
