package csi.client.gwt.widget.display_list_widgets;

import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

import csi.client.gwt.util.Display;
import csi.client.gwt.widget.misc.ScrollingString;
import csi.server.common.interfaces.DisplayListBuilderHelper;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.util.Format;

/**
 * Created by centrifuge on 2/20/2015.
 */
public class SegmentedStringWidget<T> extends ScrollingString {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private DataViewDef _metaData;
    private Map<T, String> _lookUpMap = null;
    private DisplayList<ComponentLabel, T> _displayList;
    ComponentTreePanel<T> _treePanel = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                       Callbacks                                        //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    SelectionCallback _selectionChangeCallback = null;

    private SelectionCallback handleSelectionChange = new SelectionCallback() {

        @Override
        public void segmentSelected(Integer parentKeyIn, Integer keyIn, Integer ordinalIn, boolean forwardIn) {

            if (forwardIn && (null != _treePanel)) {

                _treePanel.selectItem(keyIn);
            }

            if (null != _selectionChangeCallback) {

                _selectionChangeCallback.segmentSelected(parentKeyIn, keyIn, ordinalIn, forwardIn);
            }
        }

        @Override
        public void valueSelected(Integer parentKeyIn, Integer keyIn, Integer ordinalIn, boolean forwardIn) {

            if (forwardIn && (null != _treePanel)) {

                _treePanel.selectItem(keyIn);
            }

            if (null != _selectionChangeCallback) {

                _selectionChangeCallback.valueSelected(parentKeyIn, keyIn, ordinalIn, forwardIn);
            }
        }

        @Override
        public void emptyValueSelected(Integer parentKeyIn, Integer keyIn, Integer ordinalIn, boolean forwardIn) {

            if (forwardIn && (null != _treePanel)) {

                _treePanel.deselectAll();
            }

            if (null != _selectionChangeCallback) {

                _selectionChangeCallback.emptyValueSelected(parentKeyIn, keyIn, ordinalIn, forwardIn);
            }
        }
    };

    private DisplayCallback<ComponentLabel, T> displayListCallback
            = new DisplayCallback<ComponentLabel, T>() {
        @Override
        public void refreshDisplay() {

            refresh();
        }

        @Override
        public void beginNode(DisplayListItem<ComponentLabel, T> itemIn) {

            if (null != _treePanel) {

                _treePanel.beginNode(itemIn.getKey(), itemIn.getObject());
            }
        }

        @Override
        public void addToDisplay(DisplayListItem<ComponentLabel, T> itemIn) {

            ComponentLabel myLabel = itemIn.getDisplayObject();

            if (null != myLabel) {

                stringPanel.add(myLabel);
            }

            if (null != _treePanel) {

                _treePanel.addValue(itemIn.getKey(), itemIn.getObject(), itemIn.getDisplayObject());
            }
        }

        @Override
        public void endNode(DisplayListItem<ComponentLabel, T> itemIn) {

            if (null != _treePanel) {

                _treePanel.endNode(itemIn.getKey());
            }
        }
    };

    private DisplayObjectBuilder<ComponentLabel, String> displayObjectBuilder
            = new DisplayObjectBuilder<ComponentLabel, String>() {

        @Override
        public ComponentLabel createObject(String itemIn) {

            return new ObjectComponentLabel(itemIn);
        }

        @Override
        public ComponentLabel createObject() {

            return new PromptComponentLabel();
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ClickHandler handleComponentTreeClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            if (null != _treePanel) {

                ComponentTreeItem mySelection = _treePanel.getSelection();

                if (null != mySelection) {

                    _displayList.selectFromTree(mySelection.getForeignKey());
                }
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public SegmentedStringWidget(DataViewDef metaDataIn, Map<T, String> lookUpMapIn, SelectionCallback selectionChangeCallbackIn) {

        initialize();
        _metaData = metaDataIn;
        _lookUpMap = lookUpMapIn;
        _selectionChangeCallback = selectionChangeCallbackIn;
        _displayList = new DisplayList<ComponentLabel, T>(_metaData, displayListCallback, handleSelectionChange);
    }

    public DisplayList<ComponentLabel, T> initializeDisplay(DisplayListBuilderHelper<T, String> helperIn) {

        if (null != helperIn) {

            _displayList.loadItems(new DisplayListBuilder<ComponentLabel, String, T>(_metaData,
                    displayObjectBuilder, _lookUpMap),helperIn);

        } else {

            _displayList.replaceTop(new PromptComponentLabel(true), null);
        }

        return _displayList;
    }

    public DisplayList<ComponentLabel, T> getDisplayList() {

        return _displayList;
    }

    public void setLookUpMap(Map<T, String> lookUpMapIn) {

        _lookUpMap = lookUpMapIn;
    }

    public void selectChild(int slotIn)  {

        _displayList.selectChild(slotIn);
    }

    public T getDataObject(Integer keyIn) {

        return _displayList.getDataObject(keyIn);
    }

    public void clearDisplay() {

        stringPanel.clear();
    }

    public void refresh() {

        clearDisplay();

        if (null != _treePanel) {

            _treePanel.initialize();
        }
        _displayList.display();

        if (null != _treePanel) {

            _treePanel.selectItem(_displayList.getSelection());
            _treePanel.refresh();
        }
    }

    public boolean hasSelection() {

        return _displayList.hasSelection();
    }

    public void replaceSelection(DisplayListBuilderHelper<T, String> helperIn, T dataItemIn) {

        _displayList.replaceSelection(new DisplayListBuilder<ComponentLabel, String, T>(_metaData,
                        displayObjectBuilder, _lookUpMap), helperIn, dataItemIn);
    }

    public void appendToSelection(DisplayListBuilderHelper<T, String> helperIn, T dataItemIn) {

        _displayList.appendToSelection(new DisplayListBuilder<ComponentLabel, String, T>(_metaData,
                displayObjectBuilder, _lookUpMap), helperIn, dataItemIn, new PromptComponentLabel());
    }

    public void prependToSelection(DisplayListBuilderHelper<T, String> helperIn, T dataItemIn) {

        _displayList.prependToSelection(new DisplayListBuilder<ComponentLabel, String, T>(_metaData,
                displayObjectBuilder, _lookUpMap), helperIn, dataItemIn, new PromptComponentLabel());
    }

    public void addToSelectedParent(DisplayListBuilderHelper<T, String> helperIn, T dataItemIn) {

        _displayList.addToSelectedParent(new DisplayListBuilder<ComponentLabel, String, T>(_metaData,
                displayObjectBuilder, _lookUpMap), helperIn, dataItemIn, new PromptComponentLabel(), -1);
    }

    private void initialize() {

        stringPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
    }

    public void undo() {

        _displayList.undo();
    }

    public void deleteSelected(boolean isAddOnIn) {

        _displayList.deleteSelected(isAddOnIn ? null : displayObjectBuilder.createObject());
    }

    public void clearUndo() {

        _displayList.clearUndo();
    }

    public boolean canUndo() {

        return _displayList.canUndo();
    }

    public boolean canDelete() {

        return _displayList.canDelete();
    }

    public T getSelectedObject() {

        return _displayList.getSelectedObject();
    }

    public void connect(ComponentTreePanel<T> treePanelIn, ComponentTreeHelper<T> helperIn) {

        _treePanel = treePanelIn;
        _treePanel.linkSegmentedString(helperIn, handleComponentTreeClick);
    }

    public <R> R getResults(ObjectBuilder<ComponentLabel, T, R> builderIn) {

        return _displayList.getResults(builderIn);
    }
}
