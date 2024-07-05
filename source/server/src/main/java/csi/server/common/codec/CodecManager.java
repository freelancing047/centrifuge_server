package csi.server.common.codec;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.XStream;

import csi.server.common.codec.xstream.XStreamCodec;
import csi.server.common.codec.xstream.XStreamHelper;

/**
 * Centralize management of codecs by service name and codec type (XML, JSON, etc).
 */
public class CodecManager {
   public static final String COMMON_SERVICE_CODECS = "CommonServiceCodecs";

    /** The Service-Codec Map that holds the Service as key and its corresponding Codecs Map as values. */
    private static Map<String, Map<CodecType, Codec>> codecMap = new HashMap<String, Map<CodecType, Codec>>();

    public static void registerCodec(String usageName, Codec codec) {
        Map<CodecType, Codec> codecs = codecMap.get(usageName);
        if (codecs == null) {
            codecs = new HashMap<CodecType, Codec>();
            codecMap.put(usageName, codecs);
        }

        Codec c = codecs.get(codec.getType());
        if (c == null) {
            codecs.put(codec.getType(), codec);
        }
    }

    /**
     * Unregister the specified Codec for the given <code>serviceName</code>.
     * 
     * @param usageName
     *            the Name of the Service for which the codec needs to be unregistered
     * @param codec
     *            the codec that needs to be unregistered
     */
    public static void unregisterCodec(String usageName, Codec codec) {
        Map<CodecType, Codec> codecs = codecMap.get(usageName);
        if (codecs != null) {
            codecs.remove(codec.getType());
        }
    }

    public static Codec getCodec(String usageName, CodecType type) {
        if (type == null) {
            return null;
        }

        Codec c = null;

        Map<CodecType, Codec> codecs = codecMap.get(usageName);
        if (codecs != null) {
            c = codecs.get(type);
        }

        return c;
    }

    public static Codec getCommonCodec(CodecType codecType) {
        // this should be used going forward...to keep a common codec throughout the app
        Codec codec = CodecManager.getCodec(CodecManager.COMMON_SERVICE_CODECS, codecType);
        if (codec == null) {
            if (codecType.isXml()) {
                XStream xstream = XStreamHelper.createBaseMarshaller(codecType);
                codec = new XStreamCodec(codecType, xstream);
            } else {
                XStream xstream = XStreamHelper.createBaseMarshaller(codecType);
                codec = new XStreamCodec(codecType, xstream);
            }
        }
        CodecManager.registerCodec(CodecManager.COMMON_SERVICE_CODECS, codec);
        return codec;
    }
}
