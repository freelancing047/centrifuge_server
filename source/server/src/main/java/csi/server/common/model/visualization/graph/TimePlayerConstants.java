package csi.server.common.model.visualization.graph;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TimePlayerConstants {
   public static final int LONG_DELAY_MS = 10000;
   public static final int MODERATE_DELAY_MS = 2500;
   public static final int SHORT_DELAY_MS = 500;

   public enum TimePlayerSpeed {
      SLOW(LONG_DELAY_MS),
      MODERATE(MODERATE_DELAY_MS),
      FAST(SHORT_DELAY_MS);

      private int delay;

      private TimePlayerSpeed(int delay) {
         this.delay = delay;
      }

      public int getDelay() {
         return delay;
      }

      public static TimePlayerSpeed fromInt(int delay) {
         if (delay > MODERATE_DELAY_MS) {
            return SLOW;
         }
         if (delay > SHORT_DELAY_MS) {
            return MODERATE;
         }
         return FAST;
      }
   }

   public enum TimePlayerPlaybackMode implements IsSerializable {
      CUMULATIVE, DYNAMIC_TIME_SPAN, FIXED_TIME_SPAN
   }

   public enum TimePlayerStepMode implements IsSerializable {
      ABSOLUTE, RELATIVE, PERCENTAGE
   }
}
