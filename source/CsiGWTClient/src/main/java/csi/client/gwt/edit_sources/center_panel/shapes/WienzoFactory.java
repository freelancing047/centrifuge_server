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
package csi.client.gwt.edit_sources.center_panel.shapes;

import java.util.List;

import com.emitrom.lienzo.client.core.shape.Layer;

import csi.client.gwt.edit_sources.center_panel.ConfigurationPresenter;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.gxt.drag_n_drop.ResizeableLienzoPanel;
import csi.server.common.model.DataSetOp;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class WienzoFactory {

    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    public static WienzoComposite create(List<WienzoComposite> listIn, String selectedDsoIn,
                                         DataSetOp sourceIn, ConfigurationPresenter configurationPresenterIn,
                                         Layer layerIn, ResizeableLienzoPanel viewIn) {

        WienzoComposite myComposite = null;

        if (sourceIn.hasChildren()) {

            Layer myNewLayer = createNewLayer(viewIn);
            DataSetOp myFirstSource = sourceIn.getLeftChild();
            DataSetOp mySecondSource = sourceIn.getRightChild();
            WienzoComposite myFirstChild = create(listIn, selectedDsoIn, myFirstSource, configurationPresenterIn, myNewLayer, viewIn);
            WienzoComposite mySecondChild = create(listIn, selectedDsoIn, mySecondSource, configurationPresenterIn, myNewLayer, viewIn);

            switch (sourceIn.getMapType()) {
                case JOIN: {
                    myComposite = new WienzoJoin(sourceIn, myFirstChild, mySecondChild, configurationPresenterIn);
                    break;
                }
                case APPEND: {
                    myComposite = new WienzoUnion(sourceIn, myFirstChild, mySecondChild, configurationPresenterIn);
                    break;
                }
                default:
            }

        } else {

            myComposite = new WienzoTable(sourceIn, configurationPresenterIn);
        }
        if (null != myComposite) {

            if ((null != selectedDsoIn) && selectedDsoIn.equals(sourceIn.getLocalId())) {

                configurationPresenterIn.recordSelection(myComposite);
            }
            listIn.add(myComposite);
            layerIn.add(myComposite);
            if (!sourceIn.hasChildren()) {
                layerIn.draw();
            }
            return myComposite;
        }
        throw new RuntimeException(i18n.wienzoFactoryExceptionMessage() + sourceIn.getMapType()); //$NON-NLS-1$
    }

    private static Layer createNewLayer(ResizeableLienzoPanel viewIn) {

        Layer myLayer = new Layer();

        viewIn.add(myLayer);

        return myLayer;
    }
}
