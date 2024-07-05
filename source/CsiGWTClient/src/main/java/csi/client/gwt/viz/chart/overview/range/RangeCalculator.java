package csi.client.gwt.viz.chart.overview.range;

import java.util.ArrayList;
import java.util.List;

import csi.client.gwt.viz.chart.overview.OverviewState;

/**
 * @author Centrifuge Systems, Inc.
 */
public class RangeCalculator {

    public static double createBinSize(int width, int numberOfCategories) {
        return (double)width / (double)numberOfCategories;
    }

    /**
     * Gets the range of categories that is selected.
     * @param state The current overview state.
     * @param numberOfCategories Total number of categories shown in the component.
     * @return A range of category indices that are within the overview bounds.
     */

    public static Range calculateRange(OverviewState state, int numberOfCategories){
        if(numberOfCategories == 0){
            return Range.EMPTY_RANGE;
        }

        double binSize = createBinSize(state.getWidth(), numberOfCategories);
        
        
        int startIndex = (int) (state.getStartPosition() / binSize);
        //Note: CEN-4757 was getting bad end index at the end of range sometimes.
        // So now we are rounding up ,but need to not exceed actual range
        int endIndex = (int) Math.ceil((state.getEndPosition()-1) / binSize);
        endIndex = Math.min(numberOfCategories - 1, endIndex);
        
        
        return new Range(startIndex, endIndex);
    }
    
    public static Range calculateRange(OverviewState state, int start, int end, int numberOfCategories) {
        if(numberOfCategories == 0){
            return Range.EMPTY_RANGE;
        }

        double binSize = createBinSize(state.getWidth(), numberOfCategories);
        
        
        int startIndex = (int) (start / binSize);
        int endIndex = (int) (end / binSize);
        
        
        
        return new Range(startIndex, endIndex);
        
    }

    /**
     * Ensures each category is the same width.
     * @param numberOfCategories The number of categories to display
     * @param maxDesiredWidth The maximum width for displaying the control.
     * @return A width of the overview content.
     */
    public static int calculateWidthOfOverviewContent(int numberOfCategories, int maxDesiredWidth) {
        if(numberOfCategories == 0){
            return maxDesiredWidth;
        }
        
        int updatedNumberOfCategories = numberOfCategories;
        int ii = 2;
        
        while(updatedNumberOfCategories > maxDesiredWidth){
            updatedNumberOfCategories = numberOfCategories/ii;
            ii++;
        }
        
    
        int remainder = maxDesiredWidth % updatedNumberOfCategories;
        return maxDesiredWidth - remainder;
    }
    
    public static List<Integer> adjustCategoryDataToSize(List<Integer> categoryData, int width) {
        List<Integer> adjustedCategories = new ArrayList<Integer>(categoryData);
        
        double binSize = RangeCalculator.createBinSize(width, adjustedCategories.size());
        
        if(binSize >= 1){
            return adjustedCategories;
        }
        
        int optimalCategoriesPerBin = (int) (1/binSize + .5);
        
        int index = optimalCategoriesPerBin;
        int summedValue = 0;
        List<Integer> updatedBins = new ArrayList<Integer>();
        
        for(Integer value: adjustedCategories){
            
            summedValue += value;
            index--;
            
            if(index <= 0){
                
                int averageValue = (int) (summedValue / optimalCategoriesPerBin + .5);
                
                updatedBins.add(averageValue);
                
                index = optimalCategoriesPerBin;
                summedValue = 0;
            }
        }
        
        
        return updatedBins;
    }
}
