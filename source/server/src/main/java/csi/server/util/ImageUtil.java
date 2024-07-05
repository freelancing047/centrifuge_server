package csi.server.util;

import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import com.google.common.base.Throwables;
import com.objectplanet.image.PngEncoder;

public class ImageUtil {
   public static final String BASE_64_IMAGE_PREFIX = "data:image/png;base64,";

   public static void writePNG(BufferedImage img, OutputStream outs) throws IOException {
      if (img != null) {
         if (img.getType() != Transparency.OPAQUE) {
            new PngEncoder(PngEncoder.COLOR_TRUECOLOR_ALPHA).encode(img, outs);
         } else {
            new PngEncoder().encode(img, outs);
         }
      }
   }

   public static String toBase64String(BufferedImage img) {
      String result = null;

      try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
         ImageUtil.writePNG(img, bos);

         result = new StringBuilder(BASE_64_IMAGE_PREFIX)
                            .append(new String(Base64.getEncoder().encode(bos.toByteArray())))
                            .toString();
      } catch (IOException e) {
         e.printStackTrace();
      }
      return result;
   }

   public static BufferedImage fromBase64String(String base64Data) {
      BufferedImage result = null;
      String base64 = base64Data.substring(BASE_64_IMAGE_PREFIX.length());
      byte[] decoded = Base64.getDecoder().decode(base64.getBytes());

      try (ByteArrayInputStream bis = new ByteArrayInputStream(decoded)) {
         result = ImageIO.read(bis);
      } catch (IOException e) {
         throw Throwables.propagate(e);
      }
      return result;
   }
}
