package csi.server.business.service.filemanager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import csi.server.common.dto.FileUploadBlock;
import csi.shared.core.Constants;

/**
 * Created by centrifuge on 8/11/2015.
 */
public class FileUploader implements ServletContextListener {
   private static final BlockingQueue<FileUploadBlock> REQUEST_QUEUE = new LinkedBlockingQueue<FileUploadBlock>();

   private volatile Thread thread;

   public static void add(FileUploadBlock fileUploadBlock) {
      REQUEST_QUEUE.add(fileUploadBlock);
   }

   public static void cancel(String fileIn) {
   }

   @Override
   public void contextInitialized(ServletContextEvent servletContextEvent) {
      thread = new Thread(new Runnable() {
         @Override
         public void run() {
//            Map<String,LocalDateTime> activeFiles = new TreeMap<String,LocalDateTime>();
//            String activeFile = null;
            FileUploadBlock fileUploadBlock;

            while (true) {

               // TODO: Process cancel requests

               try {
                  Thread.sleep(10);

                  while ((fileUploadBlock = REQUEST_QUEUE.poll()) != null) {

                     // TODO: Process cancel requests

                     String newFile = fileUploadBlock.getFileName();

                     if (newFile != null) {
//                        if (!newFile.equals(activeFile)) {
//                           activeFile = newFile;
//                           activeFiles.put(activeFile, LocalDateTime.now());
//                        }
                        try (FileOutputStream fos = new FileOutputStream(getOutputFile(newFile), (fileUploadBlock.getBlockNumber() > 0))) {
                           writeBlock(fos, fileUploadBlock.getBlock(), fileUploadBlock.getBlockSize());
                        } catch (Exception myException) {
                        }
                     }
                  }
               } catch (InterruptedException ie) {
                  return;
               } catch (Exception e) {
               }
            }
         }
      });
      thread.start();
   }

   @Override
   public void contextDestroyed(ServletContextEvent servletContextEvent) {
      thread.interrupt();
   }

   private static void writeBlock(FileOutputStream fileIn, byte[] blockIn, long sizeIn) {
      try {
         if ((sizeIn > 0L) && (blockIn != null) && (sizeIn <= blockIn.length)) {
            fileIn.write(blockIn, 0, (int) sizeIn);
         }
      } catch (Exception exception) {
      }
   }

   private static String getOutputFile(String fileNameIn) {
      StringBuilder myFolderBuffer = buildUploadFolderPath();
      File myDirectory = new File(myFolderBuffer.toString());

      if (!myDirectory.exists()) {
         myDirectory.mkdirs();
      }
      myFolderBuffer.append(File.separator);
      myFolderBuffer.append(fileNameIn);
      return myFolderBuffer.toString();
   }

   private static StringBuilder buildUploadFolderPath() {
      return new StringBuilder(Constants.FileConstants.UPLOAD_DEFAULT_TOP_LEVEL_FOLDER)
                       .append(File.separator)
                       .append("_uploadFiles");
   }
}
