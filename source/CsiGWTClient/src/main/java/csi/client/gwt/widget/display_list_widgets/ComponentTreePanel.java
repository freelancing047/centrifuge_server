package csi.client.gwt.widget.display_list_widgets;

import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.shared.IconProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.tree.Tree;

import csi.server.common.exception.CentrifugeException;

/**
 * Created by centrifuge on 3/20/2015.
 */
public class ComponentTreePanel<T> extends LayoutPanel {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Embedded Classes                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    class ComponentTree extends Tree<ComponentTreeItem, String> {

        private ClickHandler _selectionHandler;
        private ComponentTreeItem _selection = null;

        public ComponentTree(TreeStore<ComponentTreeItem> storeIn,
                             ValueProvider<ComponentTreeItem, String> valueProviderIn,
                             ClickHandler selectionHandlerIn) {

            super(storeIn, valueProviderIn);

            _selectionHandler = selectionHandlerIn;
        }

        public ComponentTreeItem getSelection() {

            return _selection;
        }

        @Override
        public void scrollIntoView(ComponentTreeItem modelIn) {

            TreeNode<ComponentTreeItem> myNode = findNode(modelIn);

            if (myNode != null) {

                XElement myContainer = (XElement) myNode.getElementContainer();

                if (myContainer != null) {

                    myContainer.scrollIntoView(getElement(), false);
                    focusEl.setXY(myContainer.getXY());
                    focus();
                }
            }
        }

        @Override
        public boolean isLeaf(ComponentTreeItem modelIn) {

            return super.isLeaf(modelIn);
        }

        @Override
        protected void onClick(Event eventIn) {

            TreeNode<ComponentTreeItem> mySelection = findNode(eventIn.getEventTarget().<Element>cast());

            _selection = (null != mySelection) ? mySelection.getModel() : null;

            super.onClick(eventIn);
            _selectionHandler.onClick(null);
            selectItem(_selection.getForeignKey());
        }

        @Override
        protected void onDoubleClick(Event eventIn) {

            TreeNode<ComponentTreeItem> mySelection = findNode(eventIn.getEventTarget().<Element>cast());

            _selection = (null != mySelection) ? mySelection.getModel() : null;

            super.onDoubleClick(eventIn);
            _selectionHandler.onClick(null);
        }

        @Override
        protected void onExpand(ComponentTreeItem modelIn, TreeNode<ComponentTreeItem> nodeIn, boolean deepIn) {

            super.onExpand(modelIn, nodeIn, deepIn);
        }

        public void deleteChildren(ComponentTreeItem modelIn) {

            store.removeChildren(modelIn);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ComponentTree tree;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private TreeStore<ComponentTreeItem> _store = null;
    private ComponentTreeItem _top;
    private ComponentTreeItem _activeSelection = null;
    private Map<Integer, ComponentTreeItem> _itemMap = null;
    private Stack<Integer> _keyStack = new Stack<Integer>();
    private Integer _activeParent = null;
    private ComponentTreeHelper<T> _helper = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                       Callbacks                                        //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private IconProvider<ComponentTreeItem> _iconProvider
            = new IconProvider<ComponentTreeItem>() {

        @Override
        public ImageResource getIcon(ComponentTreeItem itemIn) {

            return itemIn.getIcon();
        }
    };

    private ValueProvider<ComponentTreeItem, String> _valueProvider
            = new ValueProvider<ComponentTreeItem, String>() {

        @Override
        public String getValue(ComponentTreeItem itemIn) {

            return itemIn.getName();
        }

        @Override
        public void setValue(ComponentTreeItem itemIn, String valueIn) {
            //do nothing?
        }

        @Override
        public String getPath() {
            return "_name";
        }
    };

    private ModelKeyProvider<ComponentTreeItem> _keyProvider
            = new ModelKeyProvider<ComponentTreeItem>() {

        @Override
        public String getKey(ComponentTreeItem item) {
            return item.getKey();
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ClickHandler _externalHandler = null;

    private ClickHandler handleTreeClickEvent
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent nullIn) {

            _activeSelection = tree.getSelection();

            if (null != _externalHandler) {

                _externalHandler.onClick(null);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // currently used by the data source editor
    //
    public ComponentTreePanel() throws CentrifugeException {

        _store = new TreeStore<ComponentTreeItem>(_keyProvider);
        tree = new ComponentTree(_store, _valueProvider, handleTreeClickEvent);

        tree.setIconProvider(_iconProvider);
        tree.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);

        add(tree);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Package Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    void linkSegmentedString(ComponentTreeHelper<T> helperIn, ClickHandler handlerIn) {

        _helper = helperIn;
        _externalHandler = handlerIn;
        initialize();
    }

    void initialize() {

        _store.clear();
        _activeSelection = null;
        _activeParent = null;
        _keyStack = new Stack<Integer>();
        _itemMap = new TreeMap<Integer, ComponentTreeItem>();
    }

    void beginNode(Integer keyIn, T itemIn) {

        if (null != itemIn) {

            String myText = _helper.getLabel(itemIn);
            ImageResource myIcon = _helper.getIcon(itemIn);

            addItem(_activeParent, new ComponentTreeItem(keyIn, myText, myIcon));
            _keyStack.push(_activeParent);
            _activeParent = keyIn;
        }
    }

    void addValue(Integer keyIn, T itemIn, ComponentLabel labelIn) {

        if (null != itemIn) {

            String myText = labelIn.getText();
            ImageResource myIcon = _helper.getIcon(itemIn);

            addItem(_activeParent, new ComponentTreeItem(keyIn, myText, myIcon));
        }
    }

    void endNode(Integer keyIn) {

        if (!_keyStack.isEmpty()) {

            _activeParent = _keyStack.pop();
        }
    }

    void refresh() {

        tree.refresh(_top);
        tree.expandAll();
    }

    ComponentTreeItem getSelection() {

        return _activeSelection;
    }

    void selectItem(Integer keyIn) {

        if (null != keyIn) {

            ComponentTreeItem myItem = _itemMap.get(keyIn);

            if (null != myItem) {

                tree.getSelectionModel().select(myItem, false);
                _activeSelection = myItem;
            }

        } else {

            deselectAll();
        }
    }

    void deselectAll() {

        tree.getSelectionModel().deselectAll();
        _activeSelection = null;
    }

    void addItem(Integer parentKeyIn, ComponentTreeItem itemIn) {

        ComponentTreeItem myParent = _itemMap.get(parentKeyIn);

        if (null != myParent) {

            _store.add(myParent, itemIn);

        } else {

            _store.add(itemIn);
            _top = itemIn;
        }
        _itemMap.put(itemIn.getForeignKey(), itemIn);
    }

    void insertItemBefore(Integer parentKeyIn, Integer siblingKeyIn, ComponentTreeItem itemIn) {

        ComponentTreeItem myParent = _itemMap.get(parentKeyIn);
        ComponentTreeItem mySibling = _itemMap.get(siblingKeyIn);

        if ((null != myParent) && (null != mySibling)) {

            int myIndex = _store.indexOf(mySibling) + 1;
            _store.insert(myParent, myIndex, itemIn);
        }
        _itemMap.put(itemIn.getForeignKey(), itemIn);
    }

    void insertItemAfter(Integer parentKeyIn, Integer siblingKeyIn, ComponentTreeItem itemIn) {

        ComponentTreeItem myParent = _itemMap.get(parentKeyIn);
        ComponentTreeItem mySibling = _itemMap.get(siblingKeyIn);

        if ((null != myParent) && (null != mySibling)) {

            int myIndex = _store.indexOf(mySibling);
            _store.insert(myParent, myIndex, itemIn);
        }
    }

    void removeItem(Integer parentKeyIn, ComponentTreeItem itemIn) {

        ComponentTreeItem myParent = _itemMap.get(parentKeyIn);

        if (null != myParent) {

            _store.remove(itemIn);
        }
        _itemMap.remove(itemIn.getForeignKey());
    }

    void replaceItem(Integer parentKeyIn, Integer itemKeyIn, ComponentTreeItem itemIn) {

        ComponentTreeItem myParent = _itemMap.get(parentKeyIn);

        if (null != myParent) {

            int myIndex = _store.indexOf(itemIn);
            _store.remove(itemIn);
            _store.insert(myParent, myIndex, itemIn);
        }
        _itemMap.remove(itemKeyIn);
        _itemMap.put(itemIn.getForeignKey(), itemIn);
    }
}
