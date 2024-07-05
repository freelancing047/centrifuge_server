package csi.server.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import csi.server.common.model.InPlaceUpdate;

/**
 * Created by centrifuge on 10/17/2014.
 */
public class Update {
   private Update() {
   }

   // Updates existing items. adds new items, and returns a collection of items to be removed
   public static <T extends InPlaceUpdate<T>> List<T> createOrUpdateList(List<T> oldListIn, List<T> newListIn,
                                                                         List<T> discardListOut) {
      Map<String,T> myOldMap = new HashMap<String,T>();
      List<T> myNewList = (null != oldListIn) ? oldListIn : new ArrayList<T>();
      List<T> myDiscardList = (null != discardListOut) ? discardListOut : new ArrayList<T>();

      for (T myOldItem : myNewList) {

         myOldMap.put(myOldItem.getUuid(), myOldItem);
      }

      if (null != newListIn) {

         for (T myNewItem : newListIn) {

            String myKey = myNewItem.getUuid();
            T myOldItem = myOldMap.get(myKey);

            if (null != myOldItem) {

               myNewItem.updateInPlace(myOldItem);
               myOldMap.remove(myKey);

            } else {

               myNewList.add(myNewItem);
            }
         }
      }

      myDiscardList.addAll(myOldMap.values());

      for (T myItem : myDiscardList) {

         myNewList.remove(myItem);
      }

      return myNewList;
   }

   // Updates existing items. adds new items, and returns the updated list
   public static <T extends InPlaceUpdate<T>> List<T> updateListInPlace(List<T> existingListIn, List<T> newListIn) {
      List<T> updatedList = (existingListIn == null) ? new ArrayList<T>() : existingListIn;

      if ((newListIn != null) && !newListIn.isEmpty()) {
         Map<String,T> existingItemMap = new HashMap<String,T>();

         for (T item : updatedList) {
            existingItemMap.put(item.getUuid(), item);
         }
         for (T newItem : newListIn) {
            String uuidStr = newItem.getUuid();
            T existingItem = existingItemMap.get(uuidStr);

            if (existingItem == null) {
               updatedList.add(newItem);
            } else {
               existingItem.updateInPlace(newItem);
               existingItemMap.remove(uuidStr);
            }
         }
         for (T item : existingItemMap.values()) {
            updatedList.remove(item);
         }
      } else {
         updatedList.clear();
      }
      return updatedList;
   }
}
