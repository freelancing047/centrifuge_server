/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.viz.chart.view;

import com.google.gwt.i18n.client.NumberFormat;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ChartGridRowProperty implements PropertyAccess<ChartGridRow> {

    class ChartGridRowModelKeyProvider implements ModelKeyProvider<ChartGridRow> {

        @Override
        public String getKey(ChartGridRow item) {
            return item.getDimension();
        }
    }

    public ModelKeyProvider<ChartGridRow> key() {
        return new ChartGridRowModelKeyProvider();
    }

    public ValueProvider<? super ChartGridRow, String> dimensionStringValue() {
        return new ValueProvider<ChartGridRow, String>() {

            @Override
            public String getPath() {
                return "dimension";
            }

            @Override
            public String getValue(ChartGridRow object) {
                try{
                    return object.getDimension();
                } catch(Exception e){
                    return null;
                }
            }

            @Override
            public void setValue(ChartGridRow object, String value) {
                // Noop
            }
        };
    }

    public ValueProvider<? super ChartGridRow, String> dimensionNumberValue() {
        return new ValueProvider<ChartGridRow, String>() {

            @Override
            public String getPath() {
                return "dimension";
            }

            @Override
            public String getValue(ChartGridRow object) {
                Double value = null;
                try{
                    value = new Double(object.getDimension());
                } catch(Exception e){
                    return null;
                }

                if (value != null) {
                    if (value % 1 == 0) {
                        NumberFormat format = NumberFormat.getFormat("0.0#");
                        return format.format(value);
                    } else {
                        // should we add custom formatters to every field?
                        return value.toString();
                    }
                }else{
                    return "";
                }

            }

            @Override
            public void setValue(ChartGridRow object, String value) {
                // Noop
            }
        };
    }

    public ValueProvider<? super ChartGridRow, Integer> dimensionIntegerValue() {
        return new ValueProvider<ChartGridRow, Integer>() {

            @Override
            public String getPath() {
                return "dimension";
            }

            @Override
            public Integer getValue(ChartGridRow object) {
                try{
                    return new Integer(object.getDimension());
                } catch(Exception e){
                    return null;
                }
            }

            @Override
            public void setValue(ChartGridRow object, Integer value) {
                // Noop
            }
        };
    }

    public ValueProvider<? super ChartGridRow, Number> metricValue(final int i, final String metricPath) {
        return new ValueProvider<ChartGridRow, Number>() {
            @Override
            public String getPath() {
                return metricPath;
            }
            
            public Number getValue(ChartGridRow object) {
                try{
                    return object.getMetricValues().get(i);
                } catch(Exception e){
                    return null;
                }
            };
            
            @Override
            public void setValue(ChartGridRow object, Number value) {
                // Noop
            }
        };
    }
}
