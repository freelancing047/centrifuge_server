package csi.license;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;

public class InstallerDigestLicenseReader {

    private static final int DIGEST_LENGTH = 20;

    protected byte[] digest;

    protected byte[] licenseData;

    public InstallerLicense read(InputStream stream) {
		try {
			String key = IOUtils.toString(stream, "UTF-8");
			return read(key);
		} catch (IOException e) {
		}

		return null;
	}

    public InstallerLicense read(String key) {
        InstallerLicense license = null;

        try {
            if( key == null ) {
                return null;
            }

            // make sure to keep trim on dropping whitespace!
            key = key.trim().replaceAll( "-", "" );

            byte[] data = new Base32().decode( key );
            digest = new byte[ DIGEST_LENGTH ];
            licenseData = new byte[ data.length - digest.length ];

            System.arraycopy( data, 0, digest, 0, digest.length );
            System.arraycopy( data, digest.length, licenseData, 0, licenseData.length );
            ArrayUtils.reverse( licenseData );

            if( verifyDigest() ) {
                LicenseBuilder builder = new LicenseBuilder();
                license = builder.read( new ByteArrayInputStream( licenseData ) );
            }
        } catch( Throwable t ) {
        	System.out.println(t.getMessage());
        }
        return license;
    }

    private boolean verifyDigest() throws GeneralSecurityException {
		MessageDigest sha = MessageDigest.getInstance("SHA");
		byte[] calculatedDigest = sha.digest(licenseData);
		return Arrays.equals(digest, calculatedDigest);
	}

    class LicenseBuilder {
		public InstallerLicense read(InputStream stream) throws IOException {
			DataInputStream dis = new DataInputStream(stream);

			InstallerLicense license = new InstallerLicense();
			int seed = dis.readInt();
			int seatCount = dis.readInt() - seed;
			long millis = dis.readLong();
			byte nibble = dis.readByte();
            int major = dis.readInt();
            int minor = dis.readInt();
            
            license.set_ver_major(major);
            license.set_ver_minor(minor);
			license.set_user_count(seatCount);
			license.set_expiration_date(new Date(millis));
			license.set_expiring((nibble == 0x1) ? true : false);

			return license;
		}
	}

    static class Base32  {
        private static final String base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        private static final int[] base32Lookup = {
                0xFF, 0xFF, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, // '0', '1', '2', '3', '4', '5', '6', '7'
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // '8', '9', ':', ';', '<', '=', '>', '?'
                0xFF, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, // '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G'
                0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, // 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O'
                0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, // 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W'
                0x17, 0x18, 0x19, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 'X', 'Y', 'Z', '[', '\', ']', '^', '_'
                0xFF, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, // '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g'
                0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, // 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o'
                0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, // 'p', 'q', 'r', 's', 't', 'u', 'v', 'w'
                0x17, 0x18, 0x19, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
        };

        /**
         * Encodes byte array to Base32 String.
         * 
         * @param bytes
         *            Bytes to encode.
         * @return Encoded byte array <code>bytes</code> as a String.
         * 
         */
        public String encode(final byte[] bytes) {
            int i = 0, index = 0, digit = 0;
            int currByte, nextByte;
            StringBuffer base32 = new StringBuffer( ( bytes.length + 7 ) * 8 / 5 );

            while( i < bytes.length ) {
                currByte = ( bytes[ i ] >= 0 ) ? bytes[ i ] : ( bytes[ i ] + 256 ); // unsign

                /* Is the current digit going to span a byte boundary? */
                if( index > 3 ) {
                    if( ( i + 1 ) < bytes.length ) {
                        nextByte = ( bytes[ i + 1 ] >= 0 ) ? bytes[ i + 1 ] : ( bytes[ i + 1 ] + 256 );
                    } else {
                        nextByte = 0;
                    }

                    digit = currByte & ( 0xFF >> index );
                    index = ( index + 5 ) % 8;
                    digit <<= index;
                    digit |= nextByte >> ( 8 - index );
                    i++;
                } else {
                    digit = ( currByte >> ( 8 - ( index + 5 ) ) ) & 0x1F;
                    index = ( index + 5 ) % 8;
                    if( index == 0 )
                        i++;
                }
                base32.append( base32Chars.charAt( digit ) );
            }

            return base32.toString();
        }

        /**
         * Decodes the given Base32 String to a raw byte array.
         * 
         * @param base32
         * @return Decoded <code>base32</code> String as a raw byte array.
         */
        public byte[] decode(final String base32) {
            int i, index, lookup, offset, digit;
            byte[] bytes = new byte[ base32.length() * 5 / 8 ];

            for( i = 0, index = 0, offset = 0; i < base32.length(); i++ ) {
                lookup = base32.charAt( i ) - '0';

                /* Skip chars outside the lookup table */
                if( lookup < 0 || lookup >= base32Lookup.length ) {
                    continue;
                }

                digit = base32Lookup[ lookup ];

                /* If this digit is not in the table, ignore it */
                if( digit == 0xFF ) {
                    continue;
                }

                if( index <= 3 ) {
                    index = ( index + 5 ) % 8;
                    if( index == 0 ) {
                        bytes[ offset ] |= digit;
                        offset++;
                        if( offset >= bytes.length )
                            break;
                    } else {
                        bytes[ offset ] |= digit << ( 8 - index );
                    }
                } else {
                    index = ( index + 5 ) % 8;
                    bytes[ offset ] |= ( digit >>> index );
                    offset++;

                    if( offset >= bytes.length ) {
                        break;
                    }
                    bytes[ offset ] |= digit << ( 8 - index );
                }
            }
            return bytes;
        }
    }

}
