package csi.server.business.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import csi.security.CsiSecurityManager;
import csi.shared.core.Constants;

/**
 * Created by centrifuge on 7/7/2017.
 */
public class FileHelper {
   private static int removalRetryCount = 10;

   public static boolean deleteUserFile(String fileNameIn) {
      boolean mySuccess = true;

      try {
         File myFile = new File(buildUserFilePath(fileNameIn));

         for (int i = 0; i < removalRetryCount; i++) {
            if (myFile.exists()) {
               mySuccess = false;

               if (myFile.delete()) {
                  mySuccess = true;
                  break;
               }
               Thread.sleep(100);
            }
         }
      } catch (Exception myException) {
      }
      return mySuccess;
   }

   public static FileOutputStream getOutputFile(String fileNameIn, boolean appendIn) throws IOException {
      StringBuilder myFolderBuffer = buildUserFolderPath();
      File myDirectory = new File(myFolderBuffer.toString());

      if (!myDirectory.exists()) {
         myDirectory.mkdirs();
      }
      myFolderBuffer.append(File.separator);
      myFolderBuffer.append(fileNameIn);
      return new FileOutputStream(myFolderBuffer.toString(), appendIn);
   }

   public static StringBuilder buildUserFolderPath() {
      return new StringBuilder(Constants.FileConstants.UPLOAD_DEFAULT_TOP_LEVEL_FOLDER)
                       .append(File.separator)
                       .append(CsiSecurityManager.getUserName())
                       .append(File.separator)
                       .append("datafiles");
   }

   public static String buildUserFilePath(String fileNameIn) {
      return buildUserFolderPath().append(File.separator)
                                  .append(fileNameIn)
                                  .toString();
   }
}
