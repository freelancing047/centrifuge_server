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
package csi.client.gwt.widget.ui.color;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.Anchor;
import com.sencha.gxt.core.client.Style.AnchorAlignment;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.ui.color.AbstractColorPicker.ColorPickerModelSelection;
import csi.shared.core.color.ColorModel;
import csi.shared.core.color.ContinuousColorModel;
import csi.shared.core.color.DiscreteColorModel;
import csi.shared.core.color.SingleColorModel;
import csi.shared.core.util.HasLabel;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ColorPicker {
	

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    public enum ColorType implements HasLabel {
        SINGLE(i18n.colorPickerSingleHeader()), DISCRETE(i18n.colorPickerDiscreteHeader()), CONTINUOUS(i18n.colorPickerContinuousHeader()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        private String label;

        private ColorType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public static class ColorPickerCallback {

        /**
         * Called just before the color picker menu/dialog is shown.
         */
        public void beforeShow() {

        }

        /**
         * @param model The model that was selected.
         */
        public void onSelection(ColorModel model) {

        }
    }

    private List<ColorType> colorTypes = new ArrayList<ColorType>();
    private ColorModel colorModel;
    private ColorPickerCallback colorPickerCallback;

    /**
     * Remove registered color types
     */
    public void clear() {
        colorTypes.clear();
    }

    public void addColorType(ColorType... types) {
        if (types != null) {
            for (ColorType colorType : types) {
                colorTypes.add(colorType);
            }
        }
    }

    public void setColorPickerCallback(ColorPickerCallback colorPickerCallback) {
        this.colorPickerCallback = colorPickerCallback;
    }

    /**
     * Bind this color selector so that it is activated on click of the control.
     * @param widget
     */
    public void bind(final Widget widget) {
        assert (widget instanceof HasClickHandlers);
        ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                colorPickerCallback.beforeShow();
                show(widget.getElement());
            }
        });
    }

    /**
     * @param parent Shows the color picker positioning the menu relative to parent.
     */
    public void show(Element parent) {
        if (colorTypes.size() == 0) {
            show(ColorType.SINGLE);
        } else if (colorTypes.size() == 1) {
            show(colorTypes.get(0));
        } else {
            Menu menu = new Menu();
            for (ColorType colorType : colorTypes) {
                final ColorType c = colorType;
                MenuItem item = new MenuItem(colorType.getLabel());
                menu.add(item);
                item.addSelectionHandler(new SelectionHandler<Item>() {

                    @Override
                    public void onSelection(SelectionEvent<Item> event) {
                        show(c);
                    }
                });
            }
            menu.show(parent, new AnchorAlignment(Anchor.TOP_LEFT, Anchor.BOTTOM_LEFT, true));
        }
    }

    private void show(ColorType colorType) {
        switch (colorType) {
            case SINGLE: {
                SingleColorPicker picker = new SingleColorPicker();
                if (getColorModel() == null || !(getColorModel() instanceof SingleColorModel)) {
                    setColorModel(new SingleColorModel());
                }
                showPicker(picker);
                break;
            }
            case DISCRETE: {
                DiscreteColorPicker picker = new DiscreteColorPicker();
                if (getColorModel() == null || !(getColorModel() instanceof DiscreteColorModel)) {
                    setColorModel(new DiscreteColorModel());
                }
                showPicker(picker);
                break;
            }
            case CONTINUOUS: {
                ContinuousColorPicker picker = new ContinuousColorPicker();
                if (getColorModel() == null || !(getColorModel() instanceof ContinuousColorModel)) {
                    setColorModel(new ContinuousColorModel());
                }
                showPicker(picker);
                break;
            }
        }
    }

    private void showPicker(AbstractColorPicker picker) {
        picker.setColorModel(getColorModel());
        picker.setColorPickerModelSelection(new ColorPickerModelSelection() {

            @Override
            public void onSelection(ColorModel model) {
                handleSelectionEvent(model);
            }
        });
        picker.show();
    }

    protected void handleSelectionEvent(ColorModel model) {
        colorPickerCallback.onSelection(model);
    }

    @SuppressWarnings("unchecked")
    public <T extends ColorModel> T getColorModel() {
        return (T) colorModel;
    }

    public void setColorModel(ColorModel colorModel) {
        this.colorModel = colorModel;
    }

}
