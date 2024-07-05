package csi.server.common.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.persistence.Embeddable;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.enumerations.AclResourceType;

@Embeddable
public class CsiUUID implements Serializable, IsSerializable {

    private static final Random random = new Random();
    private static final long serialVersionUID = 1L;
    private static Map<Character, Character> _hexMap = new TreeMap<Character, Character>();
    private static Character[] _hexCharacters =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    static {
        _hexMap.put('0', '0');
        _hexMap.put('1', '1');
        _hexMap.put('2', '2');
        _hexMap.put('3', '3');
        _hexMap.put('4', '4');
        _hexMap.put('5', '5');
        _hexMap.put('6', '6');
        _hexMap.put('7', '7');
        _hexMap.put('8', '8');
        _hexMap.put('9', '9');
        _hexMap.put('a', 'a');
        _hexMap.put('b', 'b');
        _hexMap.put('c', 'c');
        _hexMap.put('d', 'd');
        _hexMap.put('e', 'e');
        _hexMap.put('f', 'f');
        _hexMap.put('A', 'a');
        _hexMap.put('B', 'b');
        _hexMap.put('C', 'c');
        _hexMap.put('D', 'd');
        _hexMap.put('E', 'e');
        _hexMap.put('F', 'f');
    }

    private String uuid;

    public static String extractId(String stringIn) {

        return ((null != stringIn) && (0 <= stringIn.length())) ? stringIn.substring(0, UUID.stringLength()) : null;
    }

    public static String randomUUID() {
        return UUID.uuid();
    }

    /**
     * Preferred constructor
     */
    public CsiUUID() {
        this.uuid = randomUUID();
    }

    /**
     * @param uuid
     */
    // PCL: removed deprecation. This method is needed for deep clones and csi.server.common.model.ModelObject.setUuid(String)
    public CsiUUID(String uuid) {
        checkNotNull(uuid, "Cannot set CSIUUID to null");
        this.uuid = uuid.toLowerCase();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CsiUUID other = (CsiUUID) obj;
        if (uuid == null) {
            if (other.uuid != null) {
                return false;
            }
        } else if (!uuid.equalsIgnoreCase(other.uuid)) {
            return false;
        }
        return true;
    }

    public String getUuid() {
        return uuid == null ? null : uuid.toLowerCase();
    }

    @Override
    public int hashCode() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(uuid.getBytes());
            int hash;
            hash = (bytes[3] & 0xFF) |
                    ((bytes[2] & 0xFF) << 8) |
                    ((bytes[1] & 0xFF) << 16) |
                    ((bytes[0] & 0xFF) << 24);

            return hash;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        //NOTE: apparently one should not just take the first four bytes http://programmers.stackexchange.com/a/145633
        // Leaving this here as something to fall back on
        int hash = 0;
        // since uuid is already random we can use a piece of that
        try {
            hash = Integer.valueOf(uuid.substring(0, 8), 16);
        } catch (Exception e) {
            // do nothing
            hash = super.hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        return getUuid();
    }

    void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public static String removeFormat(String uuidIn) {

        StringBuilder myBuffer = new StringBuilder(64);
        int len = uuidIn.length();

        for (int i = 0; i < len; i++) {

            Character myCharacter = _hexMap.get(uuidIn.charAt(i));

            if (null != myCharacter) {

                myBuffer.append(myCharacter);
            }
        }
        return myBuffer.toString();
    }

    public static String getImageIconId(String imageIn) {

        String myMd5 = ((null != imageIn) && (0 < imageIn.length())) ? md5(imageIn) : randomString();

        return getMd5IconId(myMd5);
    }

    public static String getMd5IconId(String md5In) {

        String myInput = (null != md5In) ? md5In.toLowerCase() : randomString();

        return myInput.substring(0, 8) + "-" + myInput.substring(8, 16)
                + "-" + myInput.substring(16, 24) + "-" + myInput.substring(24, 32);
    }

    public static String md5(String sourceIn) {

        if (null != sourceIn) {

            try {

                MessageDigest myDigestMaker = MessageDigest.getInstance("MD5");
                byte[] myDigest = null;

                myDigestMaker.update(sourceIn.getBytes());
                myDigest = myDigestMaker.digest();
                return (null != myDigest) ? formatBytes(myDigest) : randomString();

            } catch (Exception IGNORE) {

            }
        }
        return null;
    }

    public static String formatThemeId(String uuidIn) {

        String myHash = (null != uuidIn) ? removeFormat(uuidIn) : randomString();

        return myHash.substring(0, 12) + "-" + myHash.substring(12, 20) + "-" + myHash.substring(20, 32);
    }

    public static String formatMapId(String uuidIn) {

        return uuidIn;
    }

    public static String formatId(String uuidIn, AclResourceType typeIn) {

        switch (typeIn) {

            case GRAPH_THEME:
            case MAP_THEME:
            case THEME:

                return formatThemeId(uuidIn);

            default:

                return uuidIn;
        }
    }

    private static String randomString() {

        byte[] myBytes = new byte[16];

        random.nextBytes(myBytes);
        return formatBytes(myBytes);
    }

    private static String formatBytes(byte[] bytesIn) {

        StringBuilder myBuffer = new StringBuilder(64);

        if ((null != bytesIn) && (0 < bytesIn.length)) {

            for (int i = 0; bytesIn.length > i; i++) {

                int myByte = (bytesIn[i]) & 255;

                myBuffer.append(_hexCharacters[myByte / 16]);
                myBuffer.append(_hexCharacters[myByte % 16]);
            }
        }
        return myBuffer.toString();
    }
}
