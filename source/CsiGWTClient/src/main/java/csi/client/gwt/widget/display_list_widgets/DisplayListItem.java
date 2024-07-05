package csi.client.gwt.widget.display_list_widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by centrifuge on 2/19/2015.
 */
class DisplayListItem<T extends Widget & HasClickHandlers & CanBeSelected, S> {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private Integer _key = null;
    private S _object = null;
    private T _displayObject = null;
    private List<DisplayListItem<T, S>> _components = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Package Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    DisplayListItem(Integer keyIn, S objectIn) {

        super();
        _key = keyIn;
        _object = objectIn;
        _components = new ArrayList<DisplayListItem<T, S>>();
    }

    DisplayListItem(Integer keyIn, T displayObjectIn, S objectIn) {

        super();
        _key = keyIn;
        _object = objectIn;
        _displayObject = displayObjectIn;
    }

    boolean isValid() {

        boolean myValidFlag = (null != _displayObject) ? _displayObject.isValid() : true;

        if (myValidFlag && (null != _components)) {

            for (DisplayListItem<T, S> myComponent : _components) {

                if (!myComponent.isValid()) {

                    myValidFlag = false;
                    break;
                }
            }
        }
        return myValidFlag;
    }

    S getObject() {

        return _object;
    }

    T getDisplayObject() {

        return _displayObject;
    }

    Integer getKey() {

        return _key;
    }

    List<DisplayListItem<T, S>> getChildren() {

        return _components;
    }

    DisplayListItem<T, S> getChild(int indexIn) {

        DisplayListItem<T, S> myChild = null;

        if ((null != _components) && (0 < _components.size())) {

            myChild = _components.get(indexIn);
        }
        return myChild;
    }

    boolean isEnabled() {

        return (null != _displayObject) ? _displayObject.isEnabled() : false;
    }

    boolean isSelected() {

        return (null != _displayObject) ? _displayObject.isSelected() : false;
    }

    void display(DisplayCallback callbackIn, int levelIn) {

        if (null != callbackIn) {

            if (null != _displayObject) {

                callbackIn.addToDisplay(this);

            } else if (null != _components) {

                callbackIn.beginNode(this);

                for (DisplayListItem myItem : _components) {

                    myItem.display(callbackIn, levelIn + 1);
                }

                callbackIn.endNode(this);

            } else {

                callbackIn.addToDisplay(this);
            }
        }
    }

    void addChild(DisplayListItem<T, S> itemIn) {

        getComponentList().add(itemIn);
    }

    void insertChild(DisplayListItem<T, S> itemIn, final Integer slotIn) {

        if ((null != slotIn) && (getChildCount() > slotIn)) {

            List<DisplayListItem<T, S>> myOldList = clearChildren();
            List<DisplayListItem<T, S>> myNewList = getComponentList();

            for (int i = 0; myOldList.size() > i; i++) {

                if (slotIn == i) {

                    myNewList.add(itemIn);
                }
                myNewList.add(myOldList.get(i));
            }

        } else {

            addChild(itemIn);
        }
    }

    int getChildCount() {

        return getComponentList().size();
    }

    List<DisplayListItem<T, S>> clearChildren() {

        List<DisplayListItem<T, S>> myList = _components;

        _components = null;

        return myList;
    }

    int replaceTree(Integer keyIn, DisplayListItem<T, S> itemIn, DisplayListItem<T, S> undoStorageIn) {

        int myUndoListSize = 0;
        Integer mySlot = locateSlot(keyIn);

        if (null != mySlot) {

            if ((null != undoStorageIn) && (null != undoStorageIn.getComponentList())) {

                List<DisplayListItem<T, S>> myUndoList = undoStorageIn.getComponentList();

                myUndoList.add(_components.get(mySlot));
                myUndoListSize = myUndoList.size();
            }

            _components.set(mySlot, itemIn);
        }
        return myUndoListSize;
    }

    Integer undoLastChange(Integer keyIn, Map<Integer, DisplayListItem<T, S>> itemMapIn,
                        Map<Integer, DisplayListItem<T, S>> parentMapIn,
                        DisplayListItem<T, S> undoStorageIn) {

        Integer myKey = null;

        if ((null != undoStorageIn) && (null != undoStorageIn.getComponentList())) {

            List<DisplayListItem<T, S>> myUndoList = undoStorageIn.getComponentList();
            int myUndoSlot = myUndoList.size() - 1;

            if (0 <= myUndoSlot) {

                DisplayListItem<T, S> myUndo = myUndoList.get(myUndoSlot);
                DisplayListItem<T, S> myParent = parentMapIn.get(keyIn);

                if (null != myParent) {

                    Integer mySlot = myParent.removeTree(itemMapIn, parentMapIn, keyIn);

                    if (null != mySlot) {

                        myParent.replaceSlot(mySlot, myUndo);
                        myUndoList.remove(myUndoSlot);
                        myKey = myUndo.getKey();
                    }
                }
            }
        }
        return myKey;
    }

    void replaceSlot(Integer slotIn, DisplayListItem itemIn) {

        _components.set(slotIn, itemIn);
    }

    Integer getSlot(Integer keyIn) {

        Integer mySlot = null;

        if (null != keyIn) {

            for (int i = 0; _components.size() > i; i++) {

                DisplayListItem<T, S> myItem = _components.get(i);

                if (keyIn.equals(myItem.getKey())) {

                    mySlot = new Integer(i);
                }
            }
        }
        return mySlot;
    }

    void removeChildren(Map<Integer, DisplayListItem<T, S>> itemMapIn,
                        Map<Integer, DisplayListItem<T, S>> parentMapIn) {

        if (null != _components) {

            for (DisplayListItem myItem : _components) {

                Integer myKey = myItem.getKey();

                myItem.removeTree(itemMapIn, parentMapIn);

                if ((null != myKey) && (!myKey.equals(_key)) && (parentMapIn.containsKey(myKey))) {

                    parentMapIn.remove(myKey);
                }
            }
            _components.clear();
        }
    }

    Integer removeChild(Map<Integer, DisplayListItem<T, S>> itemMapIn,
                        Map<Integer, DisplayListItem<T, S>> parentMapIn, Integer keyIn) {

        if ((null != _components) && (null != keyIn)) {

            DisplayListItem myTarget = null;

            for (DisplayListItem myItem : _components) {

                Integer myKey = myItem.getKey();

                if (keyIn == myKey) {

                    myTarget = myItem;
                    myTarget.removeTree(itemMapIn, parentMapIn);

                    if (parentMapIn.containsKey(myKey)) {

                        parentMapIn.remove(myKey);
                    }
                    break;
                }
            }
            if (null != myTarget) {

                _components.remove(myTarget);
            }
        }
        return _key;
    }

    Integer removeTree(Map<Integer, DisplayListItem<T, S>> itemMapIn,
                       Map<Integer, DisplayListItem<T, S>> parentMapIn, Integer keyIn) {

        Integer mySlot = locateSlot(keyIn);

        if (null != mySlot) {

            DisplayListItem myItem = _components.get(mySlot);

            myItem.removeTree(itemMapIn, parentMapIn);

            if (parentMapIn.containsKey(keyIn)) {

                parentMapIn.remove(keyIn);
            }
            _components.set(mySlot, null);
        }
        return mySlot;
    }

    void removeTree(Map<Integer, DisplayListItem<T, S>> itemMapIn,
                    Map<Integer, DisplayListItem<T, S>> parentMapIn) {

        if (null != _components) {

            for (DisplayListItem myItem : _components) {

                Integer myKey = myItem.getKey();

                myItem.removeTree(itemMapIn, parentMapIn);

                if ((null != myKey) && (!myKey.equals(_key)) && (parentMapIn.containsKey(myKey))) {

                    parentMapIn.remove(myKey);
                }
            }
        }
        if (null != _key) {

            if (itemMapIn.containsKey(_key)) {

                itemMapIn.remove(_key);
            }
        }

        if (null != _displayObject) {

            _displayObject.removeFromParent();
            _displayObject = null;
        }
    }

    void selectTree() {

        selectItem();

        if (null != _components) {

            for (DisplayListItem myItem : _components) {

                myItem.selectTree();
            }
        }
    }

    void deselectTree() {

        deselectItem();

        if (null != _components) {

            for (DisplayListItem myItem : _components) {

                myItem.deselectTree();
            }
        }
    }

    void disableTree() {

        disableItem();

        if (null != _components) {

            for (DisplayListItem myItem : _components) {

                myItem.disableTree();
            }
        }
    }

    void enableTree() {

        enableItem();

        if (null != _components) {

            for (DisplayListItem myItem : _components) {

                myItem.enableTree();
            }
        }
    }

    void selectItem() {

        if (null != _displayObject) {

            _displayObject.setSelected(true);
        };
    }

    void deselectItem() {

        if (null != _displayObject) {

            _displayObject.setSelected(false);
        };
    }

    void disableItem() {

        if (null != _displayObject) {

            _displayObject.setEnabled(false);
        };
    }

    void enableItem() {

        if (null != _displayObject) {

            _displayObject.setEnabled(true);
        };
    }

    DisplayListItem<T, S> simpleClone() {

        DisplayListItem<T, S> myClone = new DisplayListItem<T, S>(_key, _displayObject, _object);

        for (DisplayListItem<T, S> myChild : _components) {

            myClone.addChild(myChild);
        }
        return myClone;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private Integer locateSlot(Integer keyIn) {

        Integer mySlot = null;

        if (null != _components) {

            for (int i = 0; _components.size() > i; i++) {

                DisplayListItem myItem = _components.get(i);
                Integer myKey = myItem.getKey();

                if ((null != myKey) && (myKey.equals(keyIn))) {
                    mySlot = new Integer(i);
                    break;
                }
            }
        }
        return mySlot;
    }

    private List<DisplayListItem<T, S>> getComponentList() {

        if (null == _components) {

            _components = new ArrayList<DisplayListItem<T, S>>();
        }

        return _components;
    }
}
