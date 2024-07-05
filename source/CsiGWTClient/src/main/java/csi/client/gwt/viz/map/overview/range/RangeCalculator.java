package csi.client.gwt.viz.map.overview.range;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 */
public class RangeCalculator {

    public static double createBinSize(int width, int numberOfCategories) {
        return (double)width / (double)numberOfCategories;
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
