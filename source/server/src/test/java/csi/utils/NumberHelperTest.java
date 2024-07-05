package csi.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.*;
import csi.server.common.model.SortOrder;
import csi.shared.core.visualization.matrix.AxisLabel;
import csi.shared.core.visualization.matrix.Cell;
import org.junit.Test;

import csi.server.util.NumberHelper;

import java.util.*;

/**
 * Created by Ivan on 5/4/2017.
 */
public class NumberHelperTest {
    @Test
    public final void testDateStrings() {
        String[] wrongNumbers = {"8/20/2007", "4/1/1998 3:00", "33q.2123", "485asdf", "4/4/2004", "abdsdf",  "9/17/2000"};

        System.out.println(Character.isDigit(' '));


        for (String date : wrongNumbers) {
            System.out.println("Testing " + date + " isNumeric: " + NumberHelper.isNumeric(date));
            assertFalse(NumberHelper.isNumeric(date));
        }

    }

    @Test
    public final void testCorrectStrings() {
        String[] rightNumbers = {"1", "-0.123123", "+0.123123123", "1", "234234.324234", "30.2458333","33.425"};

        for (String s : rightNumbers) {
            System.out.println("Testing " + s + " isNumeric: " + NumberHelper.isNumeric(s));
            assertTrue(NumberHelper.isNumeric(s));
        }

    }

    @Test
    public final void testTreeMap(){
        SortOrder orderX = SortOrder.ASC;

        Comparator rowComp = (Comparator<String>) (o1, o2) -> {
            int result = ComparisonChain.start().compare(o1, o2).result();
            return orderX.compared(result);
        };

        TreeBasedTable<String, String, Double> testMap = TreeBasedTable.create(rowComp, rowComp);


        testMap.put("z","j", 3.0 );
        testMap.put("h","g", 2.0 );
        testMap.put("n","m", 1.0 );
        testMap.put("j","n", 4.0 );
        testMap.put("f","v", 5.0 );
        testMap.put("p","a", 6.0 );
        testMap.put("b","y", 7.0 );
        testMap.put("m","t", 8.0 );
        testMap.put("q","u", 9.0 );
        testMap.put("a","v", 20.0 );




        SortedSet<String> strings = testMap.rowKeySet();
        for(String s : strings){
            System.out.println(s);
        }


        for (Map.Entry<String, Map<String, Double>> a : testMap.rowMap().entrySet()) {
            for (Map.Entry<String, Double> b : a.getValue().entrySet()) {
//                System.out.println(a.getKey()+ " | " + b.getKey() + " | " + b.getValue());
            }

        }


        Set<Table.Cell<String, String, Double>> cells = testMap.cellSet();
        List<Table.Cell<String,String,Double>> list = Lists.newArrayList();

        list.addAll(cells);

        Collections.sort(list, stringComparator);

        list.forEach(a -> System.out.println(a.getRowKey()+ " | " + a.getColumnKey() + " | " + a.getValue()));


    }
    public static Ordering<Table.Cell<String, String, Double>> stringComparator =
            new Ordering<Table.Cell<String, String, Double>>() {

                @Override
                public int compare(
                        Table.Cell<String, String, Double> cell1,
                        Table.Cell<String, String, Double> cell2) {
                    String cell1Val = cell1.getRowKey();
                    String cell2Val = cell2.getRowKey();

                    return ComparisonChain.start().compare(cell1.getColumnKey(), cell2.getColumnKey()).result();
                }
            };
}
