package csi.client.gwt.widget.display_list_widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Widget;

import csi.server.common.interfaces.DisplayListBuilderHelper;
import csi.server.common.model.dataview.DataViewDef;

/**
 * Created by centrifuge on 2/20/2015.
 */
public class DisplayList<T extends Widget & HasClickHandlers & CanBeSelected, S> {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private DataViewDef _metaData;
    private DisplayListItem<T, S> _listTop = null;
    private DisplayListItem<T, S> _undoTop = null;
    private Map<Integer, DisplayListItem<T, S>> _itemMap = null;
    private Map<Integer, DisplayListItem<T, S>> _parentMap = null;

    private Integer _selectionKey = null;
    private Integer _nextKey = 1;

    private boolean _replaceMode = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                       Callbacks                                        //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private DisplayCallback _displayCallback = null;
    private SelectionCallback _selectionChangeCallback = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public DisplayList(DataViewDef metaDataIn, DisplayCallback callbackIn) {

        _metaData = metaDataIn;
        _displayCallback = callbackIn;

        initialize();
    }

    public DisplayList(DataViewDef metaDataIn, DisplayCallback callbackIn, SelectionCallback selectionChangeCallbackIn) {

        _metaData = metaDataIn;
        _displayCallback = callbackIn;
        _selectionChangeCallback = selectionChangeCallbackIn;

        initialize();
    }

    public DisplayList(DataViewDef metaDataIn, DisplayListBuilder<T, ?, S> displayListBuilderIn,
                       DisplayCallback callbackIn, DisplayListBuilderHelper helperIn) {

        _metaData = metaDataIn;
        _displayCallback = callbackIn;

        initialize();

        displayListBuilderIn.build(helperIn, this, _displayCallback);
        _displayCallback.refreshDisplay();
    }

    public DisplayList(DataViewDef metaDataIn, DisplayListBuilder<T, ?, S> displayListBuilderIn, DisplayCallback callbackIn,
                       SelectionCallback selectionChangeCallbackIn, DisplayListBuilderHelper helperIn) {

        _metaData = metaDataIn;
        _displayCallback = callbackIn;
        _selectionChangeCallback = selectionChangeCallbackIn;

        initialize();

        displayListBuilderIn.build(helperIn, this, _displayCallback);
        _displayCallback.refreshDisplay();
    }

    public boolean isValid() {

        boolean myValidFlag = false;
        DisplayListItem<T, S> myTree = _listTop.getChild(0);

        if (null != myTree) {

            myValidFlag = myTree.isValid();
        }

        return myValidFlag;
    }

    public void loadItems(DisplayListBuilder<T, ?, S> displayListBuilderIn, DisplayListBuilderHelper helperIn) {

        initialize();

        displayListBuilderIn.build(helperIn, this, _displayCallback);
        _displayCallback.refreshDisplay();
    }

    public void selectChild(int slotIn)  {

        DisplayListItem<T, S> myItem = _itemMap.get(_selectionKey);

        if (null != myItem) {

            DisplayListItem<T, S> myChild = myItem.getChild(slotIn);

            if (null != myChild) {

                resetSelection(myChild.getKey());
            }
        }
    }

    public void replaceSelection(DisplayListBuilder<T, ?, S> displayListBuilderIn,
                                 DisplayListBuilderHelper helperIn, S objectIn) {

        _replaceMode = true;
        displayListBuilderIn.replaceSelection(helperIn, this, _displayCallback, objectIn);
        _displayCallback.refreshDisplay();
        recordSelection(_selectionKey);
    }

    public void appendToSelection(DisplayListBuilder<T, ?, S> displayListBuilderIn,
                                 DisplayListBuilderHelper helperIn, S objectIn, T displayObjectIn) {

        _replaceMode = true;
        augmentParent(displayObjectIn, 1, true);
        displayListBuilderIn.replaceSelection(helperIn, this, _displayCallback, objectIn);
        _displayCallback.refreshDisplay();
        resetSelection(_selectionKey);
    }

    public void prependToSelection(DisplayListBuilder<T, ?, S> displayListBuilderIn,
                                   DisplayListBuilderHelper helperIn, S objectIn, T displayObjectIn) {

        _replaceMode = true;
        augmentParent(displayObjectIn, 0, true);
        displayListBuilderIn.replaceSelection(helperIn, this, _displayCallback, objectIn);
        _displayCallback.refreshDisplay();
        resetSelection(_selectionKey);
    }

    public void addToSelectedParent(DisplayListBuilder<T, ?, S> displayListBuilderIn,
                                   DisplayListBuilderHelper helperIn, S objectIn, T displayObjectIn, Integer deltaIn) {

        _replaceMode = true;
        augmentParent(displayObjectIn, deltaIn, false);
        displayListBuilderIn.replaceSelection(helperIn, this, _displayCallback, objectIn);
        _displayCallback.refreshDisplay();
        resetSelection(_selectionKey);
    }

    public void display() {

        _listTop.getChild(0).display(_displayCallback, 0);
    }

    public void deselectAll() {

        _undoTop.removeChildren(_itemMap, _parentMap);
        _listTop.deselectTree();
        _selectionKey = null;
    }

    public S undo() {

        if (null != _undoTop) {

            recordSelection(_undoTop.undoLastChange(_selectionKey, _itemMap, _parentMap, _undoTop));
        }
        _displayCallback.refreshDisplay();

        return getDataObject(_selectionKey);
    }

    public void deleteSelected(T displayObjectIn) {

        recordSelection(replaceItem(_selectionKey, displayObjectIn, null));
        _displayCallback.refreshDisplay();
    }

    public void clearUndo() {

        _undoTop.removeChildren(_itemMap, _parentMap);
    }

    public boolean canUndo() {

        return (null != _undoTop) ? (0 < _undoTop.getChildCount()) : false;
    }

    public boolean canDelete() {

        return (null != getDataObject(_selectionKey));
    }

    public Integer getSelection() {

        return _selectionKey;
    }

    public S getSelectedObject() {

        return getDataObject(_selectionKey);
    }

    public S getSelectedParentObject() {

        DisplayListItem<T, S> myParent = (null != _selectionKey) ? _parentMap.get(_selectionKey) : null;
        Integer myKey = (null != myParent) ? myParent.getKey() : null;

        return getDataObject(myKey);
    }

    public List<S> getSelectedChildObjects() {

        List<S> myList = null;
        DisplayListItem<T, S> mySelection = (null != _selectionKey) ? _itemMap.get(_selectionKey) : null;
        List<DisplayListItem<T, S>> myChildren = (null != mySelection) ? mySelection.getChildren() : null;

        if (null != myChildren) {

            myList = new ArrayList<S>();

            for (DisplayListItem<T, S> myChild : myChildren) {

                Integer myKey = myChild.getKey();
                S myObject = (_selectionKey != myKey) ? getDataObject(myKey) : null;

                if (null != myObject) {

                    myList.add(myObject);
                }
            }
        }

        return myList;
    }

    public List<S> getAllSelectedChildObjects() {

        List<S> myList = null;
        DisplayListItem<T, S> mySelection = (null != _selectionKey) ? _itemMap.get(_selectionKey) : null;
        List<DisplayListItem<T, S>> myChildren = (null != mySelection) ? mySelection.getChildren() : null;

        if (null != myChildren) {

            myList = new ArrayList<S>();

            for (DisplayListItem<T, S> myChild : myChildren) {

                Integer myKey = myChild.getKey();
                S myObject = (_selectionKey != myKey) ? getDataObject(myKey) : null;

                myList.add(myObject);
            }
        }

        return myList;
    }

    public S getFirstSelectedChildObject() {

        S myObject = null;
        DisplayListItem<T, S> mySelection = (null != _selectionKey) ? _itemMap.get(_selectionKey) : null;
        List<DisplayListItem<T, S>> myChildren = (null != mySelection) ? mySelection.getChildren() : null;

        if (null != myChildren) {

            for (DisplayListItem<T, S> myChild : myChildren) {

                Integer myKey = myChild.getKey();
                myObject = (_selectionKey != myKey) ? getDataObject(myKey) : null;

                if (null != myObject) {

                    break;
                }
            }
        }

        return myObject;
    }

    public List<S> getSelectedSiblingObjects() {

        List<S> myList = null;
        S mySelection = getDataObject(_selectionKey);
        DisplayListItem<T, S> myParent = (null != _selectionKey) ? _parentMap.get(_selectionKey) : null;
        List<DisplayListItem<T, S>> myChildren = (null != myParent) ? myParent.getChildren() : null;

        if (null != myChildren) {

            Integer myParentKey = myParent.getKey();

            myList = new ArrayList<S>();

            for (DisplayListItem<T, S> myChild : myChildren) {

                Integer myKey = myChild.getKey();
                S myObject = ((myParentKey != myKey) && (_selectionKey != myKey)) ? getDataObject(myKey) : null;

                if (null != myObject) {

                    myList.add(myObject);
                }
            }
        }

        return myList;
    }

    public List<S> getAllSelectedSiblingObjects() {

        List<S> myList = null;
        DisplayListItem<T, S> myParent = (null != _selectionKey) ? _parentMap.get(_selectionKey) : null;
        List<DisplayListItem<T, S>> myChildren = (null != myParent) ? myParent.getChildren() : null;

        if (null != myChildren) {

            Integer myParentKey = myParent.getKey();

            myList = new ArrayList<S>();

            for (DisplayListItem<T, S> myChild : myChildren) {

                Integer myKey = myChild.getKey();
                S myObject = (myParentKey != myKey) ? getDataObject(myKey) : null;

                myList.add(myObject);
            }
        }

        return myList;
    }

    public S getFirstSelectedSiblingObject() {

        S myObject = null;
        S mySelection = getDataObject(_selectionKey);
        DisplayListItem<T, S> myParent = (null != _selectionKey) ? _parentMap.get(_selectionKey) : null;
        List<DisplayListItem<T, S>> myChildren = (null != myParent) ? myParent.getChildren() : null;

        if (null != myChildren) {

            Integer myParentKey = myParent.getKey();

            for (DisplayListItem<T, S> myChild : myChildren) {

                Integer myKey = myChild.getKey();
                myObject = ((myParentKey != myKey) && (_selectionKey != myKey)) ? getDataObject(myKey) : null;

                if (null != myObject) {

                    break;
                }
            }
        }

        return myObject;
    }

    public void addComponentToSelection(T displayObjectIn) {

        resetSelection(addComponent(_selectionKey, displayObjectIn));
    }

    public void addPromptToSelection(T displayObjectIn, Integer ordinalIn) {

        resetSelection(addPrompt(_selectionKey, displayObjectIn, ordinalIn));
    }

    public void replaceTop(T displayObjectIn, S dataItemIn) {

        DisplayListItem<T, S> myItem = _listTop.getChild(0);

        if (null != myItem) {

            recordSelection(replaceItem(myItem.getKey(), displayObjectIn, dataItemIn));

        } else if (null != dataItemIn) {

            recordSelection(addCap(0, displayObjectIn, dataItemIn));

        } else {

            recordSelection(addPrompt(0, displayObjectIn, 0));
        }
    }

    // Limb with branches
    public Integer replaceTop(S dataItemIn) {

        DisplayListItem<T, S> myItem = _listTop.getChild(0);

        if (null != myItem) {

            recordSelection(replaceItem(myItem.getKey(), dataItemIn));

        } else {

            recordSelection(addBranch(0, dataItemIn));
        }
        return _selectionKey;
    }

    public boolean hasSelection() {

        return (null != _selectionKey);
    }

    public S getDataObject(Integer keyIn) {

        S myDataObject = null;

        if (null != keyIn) {

            DisplayListItem<T, S> myItem = _itemMap.get(keyIn);

            if (null != myItem) {

                myDataObject = myItem.getObject();
            }
        }
        return myDataObject;
    }

    public <R> R getResults(ObjectBuilder<T, S, R> builderIn) {

        return builderIn.buildObject(_metaData, _listTop.getChild(0));
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Package Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    // Terminal object -- separate of parent
    Integer addCap(Integer parentKeyIn, T displayObjectIn, S dataItemIn) {

        if (_replaceMode) {

            return replaceItem(_selectionKey, displayObjectIn, dataItemIn);

        } else {

            Integer myParent = (null != parentKeyIn) ? parentKeyIn : 0;

            return addCap(_itemMap.get(myParent), displayObjectIn, dataItemIn);
        }
    }

    // Terminal object -- separate of parent
    Integer addPrompt(Integer parentKeyIn, T displayObjectIn, Integer ordinalIn) {

        Integer myParent = (null != parentKeyIn) ? parentKeyIn : 0;

        return addPrompt(_itemMap.get(myParent), displayObjectIn, ordinalIn);
    }

    // Terminal object -- part of parent
    Integer addComponent(final Integer parentKeyIn, T displayObjectIn) {

        DisplayListItem<T, S> myParent = _itemMap.get(parentKeyIn);
        DisplayListItem<T, S> myItem = new DisplayListItem<T, S>(parentKeyIn, displayObjectIn, null);

        displayObjectIn.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent eventIn) {

                DisplayListItem<T, S> myGrandParent = _parentMap.get(parentKeyIn);
                Integer myGrandParentKey = (null != myGrandParent) ? myGrandParent.getKey() : null;
                Integer mySlot = (null != myGrandParent) ? myGrandParent.getSlot(parentKeyIn) : null;

                resetSelection(parentKeyIn);
                _selectionChangeCallback.segmentSelected(myGrandParentKey, parentKeyIn, mySlot, true);
            }
        });
        myParent.addChild(myItem);

        return myItem.getKey();
    }

    // Limb with branches
    Integer addBranch(Integer parentKeyIn, S dataItemIn) {

        if (_replaceMode) {

            return replaceItem(_selectionKey, dataItemIn);

        } else {

            Integer myParent = (null != parentKeyIn) ? parentKeyIn : 0;

            return addBranch(_itemMap.get(myParent), dataItemIn);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    // Terminal object -- separate of parent
    private Integer addCap(DisplayListItem<T, S> parentIn, T displayObjectIn, S dataItemIn) {

        final Integer myKey = _nextKey++;

        parentIn.addChild(createCap(myKey, parentIn, displayObjectIn, dataItemIn));

        return myKey;
    }

    // Terminal object -- separate of parent
    private Integer addPrompt(DisplayListItem<T, S> parentIn, T displayObjectIn, Integer ordinalIn) {

        final Integer myKey = _nextKey++;

        parentIn.addChild(createPrompt(myKey, parentIn, displayObjectIn, ordinalIn));

        return myKey;
    }

    // Limb with branches
    private Integer addBranch(DisplayListItem<T, S> parentIn, S dataItemIn) {

        final Integer myKey = _nextKey++;

        parentIn.addChild(createBranch(myKey, parentIn, dataItemIn));

        return myKey;
    }

    private DisplayListItem<T, S> createCap(final Integer keyIn, DisplayListItem<T, S> parentIn, T displayObjectIn, S dataItemIn) {

        DisplayListItem<T, S> myItem = null;

        if ((null != parentIn) && (null != displayObjectIn)) {

            myItem = new DisplayListItem<T, S>(keyIn, displayObjectIn, dataItemIn);

            _itemMap.put(keyIn, myItem);
            _parentMap.put(keyIn, parentIn);

            displayObjectIn.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent eventIn) {

                    DisplayListItem<T, S> myParent = _parentMap.get(keyIn);
                    Integer myParentKey = (null != myParent) ? myParent.getKey() : null;
                    Integer mySlot = (null != myParent) ? myParent.getSlot(keyIn) : null;

                    resetSelection(keyIn);
                    _selectionChangeCallback.valueSelected(myParentKey, keyIn, mySlot, true);
                }
            });
        }
        return myItem;
    }

    private DisplayListItem<T, S> createPrompt(final Integer keyIn, final DisplayListItem<T, S> parentIn, T displayObjectIn, final Integer ordinalIn) {

        DisplayListItem<T, S> myItem = null;

        if ((null != parentIn) && (null != displayObjectIn)) {

            myItem = new DisplayListItem<T, S>(keyIn, displayObjectIn, null);

            _itemMap.put(keyIn, myItem);
            _parentMap.put(keyIn, parentIn);

            displayObjectIn.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent eventIn) {

                    DisplayListItem<T, S> myParent = _parentMap.get(keyIn);
                    Integer myParentKey = (null != myParent) ? myParent.getKey() : null;
                    Integer mySlot = (null != myParent) ? myParent.getSlot(keyIn) : null;

                    resetSelection(keyIn);
                    _selectionChangeCallback.emptyValueSelected(myParentKey, keyIn, mySlot, true);
                }
            });
        }
        return myItem;
    }

    public void selectFromTree(final Integer keyIn) {

        DisplayListItem<T, S> myParent = _parentMap.get(keyIn);
        Integer myParentKey = (null != myParent) ? myParent.getKey() : null;
        Integer mySlot = (null != myParent) ? myParent.getSlot(keyIn) : null;

        resetSelection(keyIn);
        _selectionChangeCallback.emptyValueSelected(myParentKey, keyIn, mySlot, true);
    }
/*
    public void selectFromTree(final Integer keyIn) {

        DisplayListItem<T, S> myParent = _parentMap.get(keyIn);
        Integer myParentKey = (null != myParent) ? myParent.getKey() : null;
        DisplayListItem<T, S> myGrandParent = _parentMap.get(myParentKey);
        Integer myGrandParentKey = (null != myGrandParent) ? myGrandParent.getKey() : null;
        Integer mySlot = (null != myGrandParent) ? myGrandParent.getSlot(myParentKey) : null;

        resetSelection(keyIn);
        _selectionChangeCallback.segmentSelected(myGrandParentKey, myParentKey, mySlot, false);
    }
*/
    private DisplayListItem<T, S> createBranch(final Integer keyIn, DisplayListItem<T, S> parentIn, S dataItemIn) {

        DisplayListItem<T, S> myItem = null;

        if (null != parentIn) {

            myItem = new DisplayListItem<T, S>(keyIn, dataItemIn);

            _itemMap.put(keyIn, myItem);
            _parentMap.put(keyIn, parentIn);
        }
        return myItem;
    }

    // Terminal object -- separate of parent
    private Integer replaceItem(Integer keyIn, T displayItemIn, S dataItemIn) {

        if (null != displayItemIn) {

            return replaceItem(keyIn, createCap(_nextKey++, _parentMap.get(keyIn), displayItemIn, dataItemIn));

        } else {

            return deleteItem(keyIn);
        }
    }

    // Limb with branches
    private Integer replaceItem(Integer keyIn, S dataItemIn) {

        return replaceItem(keyIn, createBranch(_nextKey++, _parentMap.get(keyIn), dataItemIn));
    }

    private Integer replaceItem(Integer keyIn, DisplayListItem<T, S> itemIn) {

        Integer myNewKey = itemIn.getKey();
        DisplayListItem<T, S> myParent = _parentMap.get(keyIn);

        myParent.replaceTree(keyIn, itemIn, _undoTop);
        _selectionKey = myNewKey;

        return _selectionKey;
    }

    private Integer deleteItem(Integer keyIn) {

        DisplayListItem<T, S> myParent = _parentMap.get(keyIn);

        return myParent.removeChild(_itemMap, _parentMap, keyIn);
    }

    private void resetSelection(Integer keyIn) {

        Integer myKey = (null != keyIn) ? new Integer(keyIn) : null;

        deselectAll();
        recordSelection(keyIn);
    }

    private void recordSelection(Integer keyIn) {

        _selectionKey = keyIn;
        DisplayListItem<T, S> mySelectionItem = _itemMap.get(_selectionKey);

        if (null != mySelectionItem) {

            mySelectionItem.selectTree();
        }
    }

    private void initialize() {

        _replaceMode = false;

        _itemMap = new TreeMap<Integer, DisplayListItem<T, S>>();
        _parentMap = new TreeMap<Integer, DisplayListItem<T, S>>();

        _listTop = new DisplayListItem<T, S>(0, null);
        _undoTop = new DisplayListItem<T, S>(-1, null);

        _itemMap.put(_listTop.getKey(), _listTop);
    }

    private void augmentParent(T displayObjectIn, Integer deltaIn, boolean fromSelectedIn) {

        if (null != _selectionKey) {

            DisplayListItem<T, S> myParent = _parentMap.get(_selectionKey);

            if (null != myParent) {

                int myMaxSlot = myParent.getChildCount();
                int mySelectedSlot = myParent.getSlot(_selectionKey);
                Integer myKey = _nextKey++;
                int mySlot = (null != deltaIn) ? ((fromSelectedIn) ? mySelectedSlot + deltaIn : myMaxSlot + deltaIn) : myMaxSlot;

//                mySlot = (0 > mySlot) ? 0 : (myMaxSlot < mySlot) ? myMaxSlot : mySlot;
                mySlot = (0 > mySlot) ? 0 : mySlot;

                myParent.insertChild(createPrompt(myKey, myParent, displayObjectIn, mySlot), mySlot);
                _selectionKey = myKey;
            }
        }
    }
}
