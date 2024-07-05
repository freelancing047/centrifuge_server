package csi.server.util;

import java.util.concurrent.TimeUnit;

import lombok.Synchronized;

/**
 * Created by centrifuge on 2/27/2019.
 */
public class WorkingDisplay extends Thread {
   private static final long ONE_SECOND_IN_MILLIS = TimeUnit.SECONDS.toMillis(1L);
   private static int LINE_SIZE = 80;

   private static WorkingDisplay workingDisplayThread = null;
   private static boolean shutdownFlag = false;

   public static void begin() {
      if (workingDisplayThread != null) {
         cancel();
      }
      workingDisplayThread = new WorkingDisplay();
      workingDisplayThread.start();
   }

   public static void cancel() {
      try {
         getSetShutdown(Boolean.TRUE);
         workingDisplayThread.interrupt();
         workingDisplayThread.join();

         workingDisplayThread = null;

         System.out.append('\n');
         System.out.flush();

         shutdownFlag = false;
      } catch (Exception e) {
      }
   }

   @Synchronized
   private static boolean getSetShutdown(Boolean shutdown) {
      if (shutdown != null) {
         shutdownFlag = shutdown.booleanValue();
      }
      return shutdownFlag;
   }

   public void run() {
      int counter = 0;

      while (!getSetShutdown(null)) {
         System.out.append('.');
         System.out.append(' ');
         System.out.flush();

         counter += 2;

         if (counter >= LINE_SIZE) {
            counter = 0;

            System.out.append('\n');
            System.out.flush();
         }
         oneSecondSleep();
      }
   }

   private void oneSecondSleep() {
      try {
         sleep(ONE_SECOND_IN_MILLIS);
      } catch (InterruptedException ie) {
      } catch (Exception e) {
      }
   }
}
