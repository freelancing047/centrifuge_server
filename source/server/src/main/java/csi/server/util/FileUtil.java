package csi.server.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
   private FileUtil() {
   }

   public static String readFile(File file) throws IOException {
      String result = "";

      try (FileReader fileReader = new FileReader(file);
           BufferedReader in = new BufferedReader(fileReader)) {
         StringBuilder sb = new StringBuilder();
         String str;

         while ((str = in.readLine()) != null) {
            sb.append(str).append("\n ");
         }
         result = sb.toString();
      }
      return result;
   }

   public static void copyDirectory(File sourceLocation, File targetLocation, FilenameFilter filter)
         throws IOException {
      if (sourceLocation.exists()) {
         if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
               targetLocation.mkdirs();
            }
            String[] children = (filter == null) ? sourceLocation.list() : sourceLocation.list(filter);

            for (int i = 0; i < children.length; i++) {
               copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]), filter);
            }
         } else {
            copyFile(sourceLocation, targetLocation);
         }
      }
   }

   public static void copyFile(File source, File target) {
      File usingTarget = target.isDirectory() ? new File(target, source.getName()) : target;
      File parentFile = usingTarget.getParentFile();

      if ((parentFile != null) && !parentFile.exists()) {
         parentFile.mkdirs();
      }
      try (InputStream in = new FileInputStream(source);
           OutputStream out = new FileOutputStream(usingTarget)) {
         byte[] buf = new byte[1024];
         int len;

         while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
         }
      } catch (Exception e) {
      }
   }

   public static void deleteDir(File dir) {
      if (dir.isDirectory()) {
         String[] children = dir.list();

         for (int i = 0; i < children.length; i++) {
            deleteDir(new File(dir, children[i]));
         }
      }
      // The directory is now empty so delete it
      dir.delete();
   }
}
