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
package csi.client.gwt.mapper.cells;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import csi.client.gwt.mapper.data_model.SelectionDataAccess;
import csi.client.gwt.mapper.data_model.SelectionPair;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class SelectionMapperCell<T> extends AbstractImageTextMapperCell<SelectionDataAccess<T>> {

    int _mode;

    public SelectionMapperCell() {

        _mode = 1 + 2 + 0;
    }

    public SelectionMapperCell(boolean includeGroupIn) {

        _mode = 1 + 2 + (includeGroupIn ? 4 : 0);
    }

    public SelectionMapperCell(boolean includeImageIn, boolean includeTextIn, boolean includeGroupIn) {

        _mode = (includeImageIn ? 1 : 0) + (includeTextIn ? 2 : 0) + (includeGroupIn ? 4 : 0);
    }

    @Override
    public void render(Cell.Context contextIn, SelectionDataAccess<T> valueIn, SafeHtmlBuilder builderIn) {

        switch (_mode) {

            case 1:

                builderIn.append(template1.html(valueIn.getCastToTypeImageHtml()));
                break;

            case 2:

                builderIn.append(template2.html(valueIn.getItemDisplayName()));
                break;

            case 3:

                builderIn.append(template3.html(valueIn.getCastToTypeImageHtml(), valueIn.getItemDisplayName()));
                break;

            case 6:

                builderIn.append(template6.html(valueIn.getItemDisplayName(), valueIn.getGroupDisplayName()));
                break;

            case 7:

                builderIn.append(template7.html(valueIn.getCastToTypeImageHtml(), valueIn.getItemDisplayName(), valueIn.getGroupDisplayName()));
                break;

            default:

                builderIn.append(template2.html(" "));
                break;
        }
    }
}
