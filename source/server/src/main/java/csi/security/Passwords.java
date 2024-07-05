package csi.security;

import java.security.MessageDigest;

import csi.server.util.HexUtils;

public class Passwords {

    public static String Digest(String credentials, String algorithm, String encoding) {

        try {
            MessageDigest md = (MessageDigest) MessageDigest.getInstance(algorithm).clone();

            if (encoding == null) {
                md.update(credentials.getBytes());
            } else {
                md.update(credentials.getBytes(encoding));
            }

            return (HexUtils.convert(md.digest()));
        } catch (Exception ex) {
            return credentials;
        }

    }
}
