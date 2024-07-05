package csi.shared.gwt.viz.chart;

import java.util.Arrays;
import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 */
public class ChartOverviewColorMapper {

    private static List<String> colors = Arrays.asList(
        //rgb(250),    
        rgb(240),
        //rgb(210),
        rgb(180),
        //rgb(150),
        rgb(120),
        //rgb(90),
        rgb(60),
        //rgb(30),
        rgb(0)
    );

    
    
    public static int DEFAULT_WEIGHT = 3;

    private static String rgb(int value) {
        return "rgb(" + value + "," + value + "," + value + ")";
    }

    private static String rgb(int value, int value1, int value2) {
        return "rgb(" + value + "," + value1 + "," + value2 + ")";
    }

    // Safeguarded the hell out of this method, because if value is too large we get no chart. Now this should always work,
    public static String getColor(int index) {
        String s = colors.get(0);
        try {
            s = colors.get(index);
        } catch (Exception e) {

        }
        return s;
    }

    public static Double calculateMinValue(List<Number> data) {
        Double minimumValue = Double.MAX_VALUE;
        
        for (Number number : data) {
            if(number.doubleValue()!=Integer.MIN_VALUE) {
                minimumValue = Math.min(minimumValue, number.doubleValue());
            }else{
                minimumValue = minimumValue;
            }
        }

        if(minimumValue == Double.MAX_VALUE){
            minimumValue = 0.0;
        }
        return minimumValue;
    }

    public static Double calculateMaxValue(List<Number> data){
        Double maximumValue = Double.MIN_VALUE;
        
        for (Number number : data) {

            maximumValue = Math.max(maximumValue, number.doubleValue());
        }
        if(maximumValue == Double.MIN_VALUE){
            maximumValue = 0.0;
        }

        return maximumValue;
    }
    
    public static int getWeight(Number value, Double minimumValue, Double maximumValue) {
        if (minimumValue.equals(maximumValue)) {
            return DEFAULT_WEIGHT;
        }


        double binSize = (maximumValue - minimumValue) / colors.size();
        int index = (int) Math.floor((value.doubleValue() - minimumValue) / binSize);
        if (index == colors.size()) {
            index--;
        }
        if(index < 0 ){
            index = 0;
        }
        return index;
    }
    
    public static int validate(int value){
        if(value >= colors.size()){
            value = colors.size()-1;
        }
        return value;
    }
}
