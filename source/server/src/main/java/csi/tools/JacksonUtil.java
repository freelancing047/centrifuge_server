package csi.tools;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class JacksonUtil {
 
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
 
    public static <T> T fromString(String string, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(string, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("The given string value: "
                + string + " cannot be transformed to Json object");
        }
    }
 
    public static String toString(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("The given Json object value: "
                + value + " cannot be transformed to a String");
        }
    }
 
    public static JsonNode toJsonNode(String value) {
        try {
            return OBJECT_MAPPER.readTree(value);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
 
    public static <T> T clone(T value) {
        return fromString(toString(value), (Class<T>) value.getClass());
    }

}
   