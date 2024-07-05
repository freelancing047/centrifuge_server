package test.data;

import java.util.Comparator;
import java.util.List;

public class Tuple {
    public static final Comparator<? super Tuple> ValueAscending = new Comparator<Tuple>() {

        @Override
        public int compare( Tuple o1, Tuple o2 )
        {
            return Integer.valueOf(o1.value).compareTo(Integer.valueOf(o2.value));
        }
    };
    
    public static final Comparator<? super Tuple> ValueDescending = new Comparator<Tuple>() {

        @Override
        public int compare( Tuple o1, Tuple o2 )
        {
            return - Integer.valueOf(o1.value).compareTo(Integer.valueOf(o2.value));
        }
    };
    public List<Object> categories;
    public int    value;
}