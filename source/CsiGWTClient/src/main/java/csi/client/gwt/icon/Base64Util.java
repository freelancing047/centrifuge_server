package csi.client.gwt.icon;


public class Base64Util {

  
  private static final char[] base64Chars = new char[] {
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
      'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b',
      'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
      'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
      '4', '5', '6', '7', '8', '9', '$', '_'};

  private static final byte[] base64Values = new byte[128];

  static {
    // Invert the mapping (i -> base64Chars[i])
    for (int i = 0; i < base64Chars.length; i++) {
      base64Values[base64Chars[i]] = (byte) i;
    }
  }


  public static byte[] fromBase64(String data) {
    if (data == null) {
      return null;
    }

    int len = data.length();
    assert (len % 4) == 0;

    if (len == 0) {
      return new byte[0];
    }

    char[] chars = new char[len];
    data.getChars(0, len, chars, 0);

    int olen = 3 * (len / 4);
    if (chars[len - 2] == '=') {
      --olen;
    }
    if (chars[len - 1] == '=') {
      --olen;
    }

    byte[] bytes = new byte[olen];

    int iidx = 0;
    int oidx = 0;
    while (iidx < len) {
      int c0 = base64Values[chars[iidx++] & 0xff];
      int c1 = base64Values[chars[iidx++] & 0xff];
      int c2 = base64Values[chars[iidx++] & 0xff];
      int c3 = base64Values[chars[iidx++] & 0xff];
      int c24 = (c0 << 18) | (c1 << 12) | (c2 << 6) | c3;

      bytes[oidx++] = (byte) (c24 >> 16);
      if (oidx == olen) {
        break;
      }
      bytes[oidx++] = (byte) (c24 >> 8);
      if (oidx == olen) {
        break;
      }
      bytes[oidx++] = (byte) c24;
    }

    return bytes;
  }


  public static long longFromBase64(String value) {
    int pos = 0;
    long longVal = base64Values[value.charAt(pos++)];
    int len = value.length();
    while (pos < len) {
      longVal <<= 6;
      longVal |= base64Values[value.charAt(pos++)];
    }
    return longVal;
  }


  public static String toBase64(byte[] data) {
    if (data == null) {
      return null;
    }

    int len = data.length;
    if (len == 0) {
      return "";
    }

    int olen = 4 * ((len + 2) / 3);
    char[] chars = new char[olen];

    int iidx = 0;
    int oidx = 0;
    int charsLeft = len;
    while (charsLeft > 0) {
      int b0 = data[iidx++] & 0xff;
      int b1 = (charsLeft > 1) ? data[iidx++] & 0xff : 0;
      int b2 = (charsLeft > 2) ? data[iidx++] & 0xff : 0;
      int b24 = (b0 << 16) | (b1 << 8) | b2;

      int c0 = (b24 >> 18) & 0x3f;
      int c1 = (b24 >> 12) & 0x3f;
      int c2 = (b24 >> 6) & 0x3f;
      int c3 = b24 & 0x3f;

      chars[oidx++] = base64Chars[c0];
      chars[oidx++] = base64Chars[c1];
      chars[oidx++] = (charsLeft > 1) ? base64Chars[c2] : '=';
      chars[oidx++] = (charsLeft > 2) ? base64Chars[c3] : '=';

      charsLeft -= 3;
    }

    return new String(chars);
  }


  public static String toBase64(long value) {
    // Convert to ints early to avoid need for long ops
    int low = (int) (value & 0xffffffff);
    int high = (int) (value >> 32);

    StringBuilder sb = new StringBuilder();
    boolean haveNonZero = base64Append(sb, (high >> 28) & 0xf, false);
    haveNonZero = base64Append(sb, (high >> 22) & 0x3f, haveNonZero);
    haveNonZero = base64Append(sb, (high >> 16) & 0x3f, haveNonZero);
    haveNonZero = base64Append(sb, (high >> 10) & 0x3f, haveNonZero);
    haveNonZero = base64Append(sb, (high >> 4) & 0x3f, haveNonZero);
    int v = ((high & 0xf) << 2) | ((low >> 30) & 0x3);
    haveNonZero = base64Append(sb, v, haveNonZero);
    haveNonZero = base64Append(sb, (low >> 24) & 0x3f, haveNonZero);
    haveNonZero = base64Append(sb, (low >> 18) & 0x3f, haveNonZero);
    haveNonZero = base64Append(sb, (low >> 12) & 0x3f, haveNonZero);
    base64Append(sb, (low >> 6) & 0x3f, haveNonZero);
    base64Append(sb, low & 0x3f, true);

    return sb.toString();
  }

  private static boolean base64Append(StringBuilder sb, int digit,
      boolean haveNonZero) {
    if (digit > 0) {
      haveNonZero = true;
    }
    if (haveNonZero) {
      sb.append(base64Chars[digit]);
    }
    return haveNonZero;
  }
}