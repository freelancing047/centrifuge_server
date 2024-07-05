package csi.server.common.dto.resource;

import java.nio.charset.StandardCharsets;

/**
 * Created by centrifuge on 4/30/2019.
 */
public class ExportImportConstants {
   public static final String NAME_TAG = "name";
   public static final String UUID_TAG = "uuid";
   public static final String OWNER_TAG = "owner";
   public static final String VERSION_TAG = "version";
   public static final String COUNT_TAG = "count";
   public static final String RESOURCE_MARKER = "    <";
   public static final String NAME_MARKER = NAME_TAG + "=\"";
   public static final String UUID_MARKER = UUID_TAG + "=\"";
   public static final String OWNER_MARKER = OWNER_TAG + "=\"";
   public static final String VERSION_MARKER = VERSION_TAG + "=\"";
   public static final String COUNT_MARKER = COUNT_TAG + "=\"";

   public static final CharSequence AMPERSAND = "&amp;".subSequence(0, "&amp;".length());
   public static final CharSequence LESS_THAN = "&lt;".subSequence(0, "&lt;".length());
   public static final CharSequence MORE_THAN = "&gt;".subSequence(0, "&gt;".length());
   public static final CharSequence QUOTE = "&quot;".subSequence(0, "&quot;".length());
   public static final CharSequence APOSTROPHE = "&apos;".subSequence(0, "&apos;".length());

   public static final byte[] XML_HEADER =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes(StandardCharsets.UTF_8);
   public static final String PNG_EXTENSION = "png";
   public static final String XML_EXTENSION = "xml";
   public static final String ZIP_EXTENSION = "zip";
   public static final String EXPORT_EXTENSION = "export";
   public static final String PNG_SUFFIX = "." + PNG_EXTENSION;
   public static final String XML_SUFFIX = "." + XML_EXTENSION;
   public static final String ZIP_SUFFIX = "." + ZIP_EXTENSION;
   public static final String EXPORT_SUFFIX = "." + EXPORT_EXTENSION;

   public static final String[] URL_CHAR_MAP = {
      "%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07", "%08", "%09", "%0A", "%0B", "%0C", "%0D", "%0E", "%0F",
      "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17", "%18", "%19", "%1A", "%1B", "%1C", "%1D", "%1E", "%1F",
      "%20",   "!", "%22",   "#",   "$", "%25",   "&",   "'",   "(",   ")", "%2A",   "+",   ",",   "-", "%2E", "%2F",
        "0",   "1",   "2",   "3",   "4",   "5",   "6",   "7",   "8",  "9",  "%3A",   ";", "%3C",   "=", "%3E", "%3F",
        "@",   "A",   "B",   "C",   "D",   "E",   "F",   "G",   "H",   "I",   "J",   "K",   "L",   "M",   "N",   "O",
        "P",   "Q",   "R",   "S",   "T",   "U",   "V",   "W",   "X",   "Y",   "Z",   "[", "%5C",   "]",   "^",   "_",
        "`",   "a",   "b",   "c",   "d",   "e",   "f",   "g",   "h",   "i",   "j",   "k",   "l",   "m",   "n",   "o",
        "p",   "q",   "r",   "s",   "t",   "u",   "v",   "w",   "x",   "y",   "z",   "{", "%7C",   "}",   "~", "%7F"
   };
}
