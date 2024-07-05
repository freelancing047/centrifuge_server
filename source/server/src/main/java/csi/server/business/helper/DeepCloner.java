package csi.server.business.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import com.thoughtworks.xstream.XStream;

import csi.server.common.codec.xstream.XStreamHelper;
import csi.server.common.codec.xstream.converter.CsiUUIDSingleValueConverter;

public class DeepCloner {
   private static final String UTF_8 = "UTF-8";

   public enum CloneType {
      EXACT, NEW_ID
   }

   public static <T> T clone(T orig) {
      return clone(orig, CloneType.EXACT);
   }

   @SuppressWarnings("unchecked")
   public static <T> T clone(T orig, CloneType type) {
      T obj = null;

      CsiUUIDSingleValueConverter.clearNewUuidMap();

      try (ByteArrayOutputStream out = new ByteArrayOutputStream();
           Writer writer = new OutputStreamWriter(out, UTF_8)) {
         XStream xs = XStreamHelper.getCloningCodec(type);

         xs.toXML(orig, writer);

         try (InputStream in = new ByteArrayInputStream(out.toByteArray());
              Reader reader = new InputStreamReader(in, UTF_8)) {
            obj = (T) xs.fromXML(reader);
         }
      } catch (UnsupportedEncodingException e) {
         throw new RuntimeException(e);
      } catch (IOException ioe) {
         throw new RuntimeException(ioe);
      } finally {
         CsiUUIDSingleValueConverter.clearNewUuidMap();
      }
      return obj;
   }
}
