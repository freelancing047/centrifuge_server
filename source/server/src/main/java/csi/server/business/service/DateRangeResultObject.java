package csi.server.business.service;

public class DateRangeResultObject {
   private long start;
   private long end;
   private int count;

   public long getStart() {
      return start;
   }
   public long getEnd() {
      return end;
   }
   public int getCount() {
      return count;
   }

   public void setStart(final long start) {
      this.start = start;
   }
   public void setEnd(final long end) {
      this.end = end;
   }
   public void setCount(final int count) {
      this.count = count;
   }
}
