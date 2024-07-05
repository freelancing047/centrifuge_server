package csi.server.business.visualization.map.storage;

import java.util.HashMap;

class OutOfBandResourcesLRUCache {
   private int capacity;
   private HashMap<String,OutOfBandResourcesNode> map = new HashMap<String,OutOfBandResourcesNode>();
   private OutOfBandResourcesNode head = null;
   private OutOfBandResourcesNode end = null;

   public OutOfBandResourcesLRUCache(int capacity) {
      this.capacity = capacity;
   }

   public OutOfBandResources get(String key) {
      if ((capacity > 0) && map.containsKey(key)) {
         OutOfBandResourcesNode n = map.get(key);
         remove(n);
         setHead(n);
         return n.value;
      }

      if (AbstractMapStorageService.instance().hasVisualizationData(key)) {
         OutOfBandResources value = AbstractMapStorageService.instance().get(key);
         if (value != null) {
            set(key, value);
            return value;
         }
      }

      return null;
   }

   public void remove(String key) {
      if ((capacity > 0) && map.containsKey(key)) {
         OutOfBandResourcesNode n = map.get(key);
         map.remove(key);
         remove(n);
      }
   }

   private void remove(OutOfBandResourcesNode n) {
      if (n.pre != null) {
         n.pre.next = n.next;
      } else {
         head = n.next;
      }

      if (n.next != null) {
         n.next.pre = n.pre;
      } else {
         end = n.pre;
      }

   }

   private void setHead(OutOfBandResourcesNode n) {
      n.next = head;
      n.pre = null;

      if (head != null) {
         head.pre = n;
      }

      head = n;

      if (end == null) {
         end = head;
      }
   }

   public void set(String key, OutOfBandResources value) {
      if (capacity > 0) {
         if (map.containsKey(key)) {
            OutOfBandResourcesNode old = map.get(key);
            old.value = value;
            remove(old);
            setHead(old);
         } else {
            OutOfBandResourcesNode created = new OutOfBandResourcesNode(key, value);
            if (map.size() >= capacity) {
               map.remove(end.key);
               remove(end);
               setHead(created);
            } else {
               setHead(created);
            }

            map.put(key, created);
         }
      }
   }
}
