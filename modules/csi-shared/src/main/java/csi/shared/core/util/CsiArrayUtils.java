package csi.shared.core.util;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

public class CsiArrayUtils {
    
    /**
     * Dedupes array, if array is already sorted used doSort=false, default to true
     * 
     * @param items
     * @return
     */
    public static  int[] deDupe(int[] items){
        return deDupe(items, true);
    }
    
    /**
     * Dedupes array, if array is already sorted used doSort=false, default to true
     * 
     * @param items
     * @return
     */
    public static int[] deDupe(int[] items, boolean doSort){
        
        if(items == null || items.length == 0){
            return items;
        }
        if(doSort){
            Arrays.sort(items);
        }
        
        int index=0;
        items[index]=items[0];
        for(int i=0;i<items.length;i++)
        {
            if (items[index]!=items[i])
            {
                index++;
                items[index]=items[i];
            }
        }

        int[] result = new int[index+1];
        System.arraycopy(items, 0, result, 0, result.length);
        return result;

    }

    public static int[] merge(int[] ids, int[] ids2){
        int aLen = ids.length;
        int bLen = ids2.length;
        int[] result= new int[aLen+bLen];
        System.arraycopy(ids, 0, result, 0, aLen);
        System.arraycopy(ids2, 0, result, aLen, bLen);

        return result;
    }
    
    
    public static Object[] deDupe(Object[] items){
        
        if(items == null || items.length == 0){
            return items;
        }
        
        //Can't sort
        if(items[0] == null || !(items[0] instanceof Comparable)){
            return items;
        }
        
        Arrays.sort(items);

        int index=0;
        items[index]=items[0];
        for(int i=0;i<items.length;i++)
        {
            if (items[index]!=items[i])
            {
                index++;
                items[index]=items[i];
            }
        }

        Object[] result = new Object[index+1];
        System.arraycopy(items, 0, result, 0, result.length);
        return result;

    }

    public static Object[] merge(Object[] ids, Object[] ids2){
        int aLen = ids.length;
        int bLen = ids2.length;
        Object[] result= new Object[aLen+bLen];
        System.arraycopy(ids, 0, result, 0, aLen);
        System.arraycopy(ids2, 0, result, aLen, bLen);

        return result;
    }
    

    public static List<Object> join(Object[] items, Object[] items2){
        return join(items, items2, true);
    }
    
    
    public static List<Object> join(Object[] items, Object[] items2, boolean dedupe){
        
        if(items == null || items.length == 0){
            return Lists.newArrayList();
        }
        if(items2 == null || items2.length == 0){
            return Lists.newArrayList();
        }
        
        //Many not be necessary but the way we find joins is on dupe, so we gotta dedupe here
        if(dedupe){
            items = CsiArrayUtils.deDupe(items);
            items2 = CsiArrayUtils.deDupe(items2);
        }
        
        Object[] all = merge(items, items2);

        Arrays.sort(all);
        List<Object> commonItems = Lists.newArrayList();
        Object last = null;
        for(int ii=0; ii<all.length-1; ii++){
            
            if((last == null || !last.equals(all[ii])) && all[ii].equals(all[ii+1])){
                commonItems.add(all[ii]);
                last = all[ii];
                ii++;
            }
        }
        
        return commonItems;
    }
    
    public static List<? extends Number> join(int[] items, int[] items2){
        return join(items, items2, true);
    }
    
    public static List<? extends Number> join(int[] items, int[] items2, boolean doDedupe){
        
        if(items == null || items.length == 0){
            return Lists.newArrayList();
        }
        if(items2 == null || items2.length == 0){
            return Lists.newArrayList();
        }
        
        //Many not be necessary but the way we find joins is on dupe, so we gotta dedupe here
        if(doDedupe){
            items = CsiArrayUtils.deDupe(items);
            items2 = CsiArrayUtils.deDupe(items2);
        }
        
        int[] all = merge(items, items2);

        Arrays.sort(all);
        List<Number> commonItems = Lists.newArrayList();
        Number last = null;
        for(int ii=0; ii<all.length-1; ii++){
            
            if((last == null || !last.equals(all[ii])) && all[ii] == (all[ii+1])){
                commonItems.add(all[ii]);
                last = all[ii];
                ii++;
            }
        }
        
        return commonItems;
    }


    private static int[] sort(int arr[])
    {
        int n = arr.length;

        // Build heap (rearrange array)
        for (int i = n / 2 - 1; i >= 0; i--)
            heapify(arr, n, i);

        // One by one extract an element from heap
        for (int i=n-1; i>=0; i--)
        {
            // Move current root to end
            int temp = arr[0];
            arr[0] = arr[i];
            arr[i] = temp;

            // call max heapify on the reduced heap
            heapify(arr, i, 0);
        }
        
        return arr;
    }

    private static void heapify(int arr[], int n, int i)
    {
        int largest = i;  // Initialize largest as root
        int l = 2*i + 1;  // left = 2*i + 1
        int r = 2*i + 2;  // right = 2*i + 2

        // If left child is larger than root
        if (l < n && arr[l] > arr[largest])
            largest = l;

        // If right child is larger than largest so far
        if (r < n && arr[r] > arr[largest])
            largest = r;

        // If largest is not root
        if (largest != i)
        {
            int swap = arr[i];
            arr[i] = arr[largest];
            arr[largest] = swap;

            // Recursively heapify the affected sub-tree
            heapify(arr, n, largest);
        }
    }
}
