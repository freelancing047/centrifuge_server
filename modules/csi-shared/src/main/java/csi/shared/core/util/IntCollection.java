package csi.shared.core.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.primitives.Ints;
import com.google.gwt.user.client.rpc.IsSerializable;
/**
 * Class made to store row ids with a smaller footprint
 * 
 * Does not work with negative numbers, will cause de-duplication of items and sort with certain operations
 * 
 * @author jdanberg
 *
 */
public class IntCollection implements Collection<Integer>, IsSerializable, Serializable{

    private static final int NO_INDEX = -1;
    private boolean sorted = false;
    int[] items;
    
    public IntCollection(){
        items = new int[0];
    }
    
    public IntCollection(int length){
        items = new int[length];
    }

    public IntCollection(int[] c) {
        items = c;
    }

    @Override
    /**
     * Don't use this to add a lot of items, since it creates new arrays, only use if single item
     * 
     * @param e
     * @return
     */
    @Deprecated
    public boolean add(Integer e) {
        if(items != null){
            items = CsiArrayUtils.merge(items, new int[]{e.intValue()});
        } else if(items == null){
            items = new int[]{e.intValue()};
        }else {
            return false;
        }
        sorted = false;
        return true;
    }

    private boolean isValid(){
        return items != null && items.length > 0;
    }

    /**
     * Don't use this to add a lot of items, since it creates new arrays, only use if single item
     * 
     * @param e
     * @return
     */
    public boolean add(int i) {
        if(isValid()){
            items = CsiArrayUtils.merge(items, new int[]{i});
        } else if(items == null || items.length == 0){
            items = new int[]{i};
        }else {
            return false;
        }
        sorted = false;
        return true;
    }

    private int[] convertCollectionToArray(Collection<Integer> c){
        if(c != null && c.size() > 0){
            if(c instanceof IntCollection){
                return ((IntCollection) c).toIntArray();
            }
            try{
                return Ints.toArray(c);
            } catch(Exception e){

            }
        }
        return null;
    }

    public boolean isSorted(){
        return sorted;
    }

    /**
     * Will sort and dedupe array 
     */
    public void deDupe(){
        if(isValid()){
            if(sorted) {
                items = CsiArrayUtils.deDupe(items, false);
            } else {
                sorted = true;
                items = CsiArrayUtils.deDupe(items);
            }
        }
    }

    public void sort(){
        if(isValid()){
            if(!sorted) {
                sorted = true;
                Arrays.sort(items);
            }
        }
    }

    @Override
    public boolean addAll(Collection c) {
        int[] cInts = convertCollectionToArray(c);

        return addAll(cInts);
    }

    @Override
    public void clear() {
        items = new int[0];
    }

    @Override
    public boolean contains(Object o) {
        if(o != null && o.getClass().isPrimitive()){
            int object = (int)o;
            return contains(object);
        } else if(o != null && o instanceof Integer){
            return contains(((Integer) o).intValue());
        }
        return false;
    }


    public boolean contains(int i){
        //return indexOf(i) >= 0;
        if(isEmpty()){
            return false;
        }
        return Ints.contains(items, i);
    }

    public int indexOf(int i){
        if(isEmpty()){
            return NO_INDEX;
        }

        return Ints.indexOf(items, i);

        //        if(!isSorted()){
        //            sorted=true;
        //            Arrays.sort(items);
        //        }
        //        
        //        int index = items.length/2;
        //        int newIndex = NO_INDEX;
        //        int upper = items.length -1;
        //        int lower = 0;
        //        int change = 0;
        //        for(int ii=0; ii<items.length; ii++){
        //            if(items[index] == i){
        //                return index;
        //            }
        //            if(items[index] < i){
        //                change = ((upper - index)/2);
        //                if(change ==  0){
        //                    newIndex = index++;
        //                } else {
        //                    newIndex = index + change;
        //                }
        //                lower = index;
        //            } else {
        //                
        //                change  = ((index - lower)/2);
        //                if(change == 0){
        //                    newIndex = index--;
        //                } else {
        //                    newIndex = index - change;
        //                }
        //                upper = index;
        //            }
        //            
        //            if(newIndex == index){
        //                return NO_INDEX;
        //            }
        //            
        //            index = newIndex;
        //            
        //            if(upper - lower < 25){
        //                break;
        //            }
        //        }
        //        
        //        for(int ii = lower; ii<=upper; ii++){
        //            if(items[ii] == i){
        //                return index;
        //            }
        //        }

        //return NO_INDEX;
    }

    @Override
    /**
     *  This sorts internal array and works by comparing duplicates to incoming collection
     * 
     */
    public boolean containsAll(Collection c) {
        //can't contain if not valid
        if(!isValid()){
            return false;
        }

        int[] otherItems = convertCollectionToArray(c);

        //can't contain if nothing to contain
        if(otherItems == null || otherItems.length ==  0){
            return false;
        }

        otherItems = CsiArrayUtils.deDupe(otherItems);
        int[] tempItems = CsiArrayUtils.deDupe(items);
        sorted = true;
        //Can't contain if smaller after dedupe
        if(tempItems.length < otherItems.length){
            return false;
        }

        List commonItems = CsiArrayUtils.join(items, tempItems);

        //Contains all if these are same size        
        return commonItems.size() == tempItems.length;
    }

    @Override
    public boolean isEmpty() {
        return !isValid();
    }

    @Override
    public Iterator<Integer> iterator() {
//        if(isEmpty()){
//            return null;
//        }
        //return Ints.asList(items).iterator();
        return new ListItr(0);
    }

    @Override
    public boolean remove(Object o) {
        if(isEmpty()){
            return false;
        }

        if(o==null){
            return false;
        }

        int index = NO_INDEX;
        if(o.getClass().isPrimitive()){
            index = indexOf((int) o);
        } else if(o instanceof Integer){
            index = indexOf(((Integer)o).intValue());
        }

        if(index > NO_INDEX){

            int aLen = items.length;

            if(aLen == 1){
                items = new int[0];
            } else {
                int[] result= new int[items.length-1];
                System.arraycopy(items, 0, result, 0, index);
                index++;
                System.arraycopy(items, index, result, index-1, items.length - index);
                items= result;
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean removeAll(Collection c) {
        if(isEmpty()){
            return false;
        }
        if(c == null || c.isEmpty()){
            return false;
        }
        
        int length = items.length;
        int count;
        
        if(c instanceof IntCollection){
            IntCollection iCollection = (IntCollection) c;
            int[] removeArray = iCollection.items;
            Arrays.sort(removeArray);
            deDupe();
            count = items.length;
            for(int ii=0; ii<items.length; ii++){
                
                for(int jj=0; jj<removeArray.length && ii<items.length;){
                    if(items[ii] == removeArray[jj]){
                        items[ii] = -1;
                        count--;
                    } else if(items[ii] > removeArray[jj]){
                        jj++;
                    } else {
                        ii++;
                    }
                }
                break;
            }
            
        } else {
            int[] removeArray = Ints.toArray(c);
            Arrays.sort(removeArray);
            deDupe();
            count = items.length;
            for(int ii=0; ii<items.length;){
                
                for(int jj=0; jj<removeArray.length&& ii<items.length;){
                    if(items[ii] == removeArray[jj]){
                        items[ii] = -1;
                        count--;
                    } else if(items[ii] > removeArray[jj]){
                        jj++;
                    } else {
                        ii++;
                    }
                }
                break;
            }
        }
        
        int[] result;
        if(count < 0) {
            count = 0;
        }

        result = new int[count];
        
        int resultPosition = 0;
        for(int ii=0; ii< items.length; ii++) {
            if(items[ii] != -1) {
                result[resultPosition] = items[ii];
                resultPosition++;
            }
        }
        
        items = result;
                
        return length != items.length;

    }

    /**
     * Will  do a dedupe and CsiArrayUtils.join
     */
    @Override
    public boolean retainAll(Collection c) {
        if(isEmpty()){
            return false;
        }
        int[] cInts = convertCollectionToArray(c);
        if(cInts  != null && cInts.length > 0 
                && isValid()){
            items = Ints.toArray(CsiArrayUtils.join(cInts, items));
            return true;
        }


        return false;
    }

    @Override
    public int size() {
        if(isValid())
            return items.length;

        return 0;
    }

    @Override
    public Object[] toArray() {
        if(isEmpty()){
            return new Object[0];
        }
        Integer[] array = new Integer[items.length];
        for(int ii=0; ii<items.length; ii++){
            array[ii] = items[ii];
        }
        return array;
    }

    @Override
    @Deprecated
    public Object[] toArray(Object[] a) {

        if(isEmpty()){
            return new Object[0];
        }
//        if(true)
        return new Object[(int) a[-1]];
//        else{
//            Object[] array = new Object[items.length];
//            for(int ii=0; ii<items.length; ii++){
//                array[ii] = items[ii];
//            }
//            return array;
//        }
    }

    public Iterator listIterator(int index) {
        if (isEmpty() || (index < 0 || index > items.length))
            throw new IndexOutOfBoundsException("Index: "+index);
        return new ListItr(index);
    }

    public int[] toIntArray() {
        return items;
    }

    public boolean addAll(int[] cInts) {
        if(cInts  != null && cInts.length > 0 
                && isValid()){
            items = CsiArrayUtils.merge(cInts, items);
            sorted = false;
            return true;
        } else if(!isValid() && cInts != null){
            sorted = false;
            items =  cInts;
            return true;
        }

        return false;
    }


    private class Itr implements Iterator {
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = items == null ? 0:items.length;

        public boolean hasNext() {
            return items != null && cursor != items.length;
        }

        @SuppressWarnings("unchecked")
        public Integer next() {
            checkForComodification();
            int i = cursor;
            if (items != null && i >= items.length)
                throw new NoSuchElementException();
            int[] elementData = IntCollection.this.items;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i + 1;
            return elementData[lastRet = i];
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                IntCollection.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
                expectedModCount = items.length;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        final void checkForComodification() {
            if (items.length != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }


//    /**
//     * Modified version of java's ArrayList ListItr
//     * @author jdanberg
//     *
//     */
//    private class ListItr extends Itr implements ListIterator<Integer> {
//        ListItr(int index) {
//            super();
//            cursor = index;
//        }
//
//        public boolean hasPrevious() {
//            return cursor != 0;
//        }
//
//        public int nextIndex() {
//            return cursor;
//        }
//
//        public int previousIndex() {
//            return cursor - 1;
//        }
//
//        @SuppressWarnings("unchecked")
//        public Integer previous() {
//            checkForComodification();
//            int i = cursor - 1;
//            if (i < 0)
//                throw new NoSuchElementException();
//            int[] elementData = IntCollection.this.items;
//            if (i >= elementData.length)
//                throw new ConcurrentModificationException();
//            cursor = i;
//            return new Integer(elementData[lastRet = i]);
//        }
//
//        public void set(Integer e) {
//            if (lastRet < 0)
//                throw new IllegalStateException();
//            checkForComodification();
//
//            try {
//                IntCollection.this.set(lastRet, e);
//            } catch (IndexOutOfBoundsException ex) {
//                throw new ConcurrentModificationException();
//            }
//        }
//
//        public void add(Integer e) {
//            checkForComodification();
//
//            try {
//                int i = cursor;
//                IntCollection.this.add(i, e);
//                cursor = i + 1;
//                lastRet = -1;
//                expectedModCount = items.length;
//            } catch (IndexOutOfBoundsException ex) {
//                throw new ConcurrentModificationException();
//            }
//        }
//    }
//    
//    private class Itr implements Iterator<Integer> {
//        int cursor;       // index of next element to return
//        int lastRet = -1; // index of last element returned; -1 if no such
//        int expectedModCount = items.length;
//
//        public boolean hasNext() {
//            return cursor != items.length;
//        }
//
//        @SuppressWarnings("unchecked")
//        public Integer next() {
//            checkForComodification();
//            int i = cursor;
//            if (i >= items.length)
//                throw new NoSuchElementException();
//            int[] elementData = IntCollection.this.items;
//            if (i >= elementData.length)
//                throw new ConcurrentModificationException();
//            cursor = i + 1;
//            return new Integer(elementData[lastRet = i]);
//        }
//
//        public void remove() {
//            if (lastRet < 0)
//                throw new IllegalStateException();
//            checkForComodification();
//
//            try {
//                IntCollection.this.remove(lastRet);
//                cursor = lastRet;
//                lastRet = -1;
//                expectedModCount = items.length;
//            } catch (IndexOutOfBoundsException ex) {
//                throw new ConcurrentModificationException();
//            }
//        }
//
//        final void checkForComodification() {
//            if (items.length != expectedModCount)
//                throw new ConcurrentModificationException();
//        }
//    }


    /**
     * Modified version of java's ArrayList ListItr
     * @author jdanberg
     *
     */
    private class ListItr extends Itr {
        ListItr(int index) {
            super();
            cursor = index;
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor - 1;
        }

        @SuppressWarnings("unchecked")
        public int previous() {
            checkForComodification();
            
            int i = cursor - 1;
            if (i < 0)
                throw new NoSuchElementException();
            int[] elementData = IntCollection.this.items;
            if (elementData != null && i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i;
            return elementData[lastRet = i];
        }

        public void set(int e) {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                IntCollection.this.set(lastRet, e);
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(int e) {
            checkForComodification();

            try {
                int i = cursor;
                IntCollection.this.add(i, e);
                cursor = i + 1;
                lastRet = -1;
                expectedModCount = items.length;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }


    public void add(int i, int e) {
        if(isValid()){

            int len = items.length;
            int newLen = len + 1;
            int[] result= new int[newLen];
            
            if(i > 0){
                System.arraycopy(items, 0, result, 0, i - 1);
            }

            items[i] = e;
            System.arraycopy(items, i, result, i+1, newLen);
            
        }
    }

    public void set(int lastRet, int e) {
        if(isValid()){
            items[lastRet] = e;
        }
        
    }

    public int get(int i) {
        return items[i];
    }

    public void addAllSorted(List<Integer> list) {
        if(!sorted) {
            deDupe();
        }
        
        Collections.sort(list);
        int listSize = list.size();
        if(listSize == 0) {
            return;
        }
        int[] result = new int[items.length + listSize];
        
        
        int listPosition = 0;
        int lastItem = 0;
        for(int ii=0; ii<items.length;) {
            
            if(items[ii] >= list.get(listPosition)) {
                result[ii+listPosition] = list.get(listPosition);
                listPosition++;
                if(listPosition == listSize) {
                    lastItem = ii;
                    break;
                }
            } else if(items[ii] < list.get(listPosition)) {
                result[ii+listPosition] = items[ii];
                ii++;
                if(ii == items.length) {
                    lastItem = ii;
                }
            }
            
        }
        
        if(listPosition == listSize) {
            for(;lastItem<items.length;lastItem++) {
                result[lastItem+listPosition] = items[lastItem];
            }
        } else {
            for(;listPosition<list.size();listPosition++) {
                result[items.length+listPosition] = list.get(listPosition);
            }
                
        }
        
        
        items = result;
    }

    
    
    
}
