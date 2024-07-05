package csi.client.gwt.util.name;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.dataview.DataViewPresenter;
import csi.client.gwt.dataview.DataViewRegistry;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.worksheet.WorksheetPresenter;
import csi.server.common.model.filter.Filter;

public class UniqueNameUtil {

    public static String getDistinctName(Collection<String> currentNames, String name) {
        if (currentNames.contains(name)) {
            int i = findNextNameSequence(currentNames, name);
            if (i > -1) {
                return name + " (" + i + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                return name;
            }

        } else {
            return name;
        }
    }

    public static List<String> getWorksheetNames(DataViewPresenter dataViewPresenter) {
        List<String> names = new ArrayList<String>();
        for (WorksheetPresenter worksheetPresenter : dataViewPresenter.getWorksheetPresenters()) {
            names.add(worksheetPresenter.getName());
        }
        return names;
    }

    public static List<String> getVisualizationNames(AbstractDataViewPresenter dataViewPresenter) {
        List<String> names = new ArrayList<String>();
        for (Visualization visualization : dataViewPresenter.getVisualizations()) {
            names.add(visualization.getName());
        }
        return names;
    }
    
    public static List<String> getFilterNames(String dataViewUuid) {
        List<String> names = new ArrayList<String>();
        
        for (Filter filter : DataViewRegistry.getInstance().getDataViewByUuid(dataViewUuid).getMeta().getFilters()) {
            names.add(filter.getName());
        }
        return names;
    }

    private static int findNextNameSequence(Collection<String> currentNames, String proposedName) {
        // convert to lowercase otherwise we can't handle case
        // where current name is Phone1 and basename is Phone1
        // or phone1
        //FIXME: the above logic should be handled before you get to this method.

        int max = 0;

        for (String name : currentNames) {
            //Does this name end with a ')', we only care about these names
            if (name.endsWith(")")) { //$NON-NLS-1$
                //Does this name have a '(' to pair with the ')'
                int indexOfLastOpenParan = name.lastIndexOf('(');
                if (indexOfLastOpenParan > 0) {
                    //Test if this has a common base with the proposed name.
                    String basename = name.substring(0, indexOfLastOpenParan-1);
                    if (basename.equals(proposedName)) {
                        //Here we get all the characters between the final '(' and the closing ')'
                        String uniqueValue = name.substring(indexOfLastOpenParan + 1, name.length() - 1);
                        try {
                            int uniqueValueInt = Integer.parseInt(uniqueValue);
                            if (max < uniqueValueInt) {
                                max = uniqueValueInt;
                            }
                        } catch (NumberFormatException ignored) {
                            //do nothing, they had something other than our unique value at this location in the name.
                        }
                    }
                }
            }
        }
        return max+1;
    }
}
