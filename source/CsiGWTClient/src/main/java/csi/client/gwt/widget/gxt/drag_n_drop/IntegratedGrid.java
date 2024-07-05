package csi.client.gwt.widget.gxt.drag_n_drop;

import java.util.List;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.dnd.core.client.DND;
import com.sencha.gxt.dnd.core.client.GridDragSource;
import com.sencha.gxt.dnd.core.client.GridDropTarget;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.GridView;

import csi.client.gwt.events.CsiDropEvent;
import csi.client.gwt.events.CsiDropEventHandler;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.server.common.model.CsiUUID;

/**
 * Created by centrifuge on 3/23/2016.
 */
public class IntegratedGrid<T> extends ResizeableGrid<T> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private String _id = CsiUUID.randomUUID();
    private GridDropTarget<T> _target;
    private GridDragSource<T> _source;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public IntegratedGrid(ListStore<T> store, ColumnModel<T> cm, GridView<T> view) {
        super(store, cm, view);
        init();
    }

    public IntegratedGrid(ListStore<T> store, ColumnModel<T> cm) {
        super(store, cm);
        init();
    }

    public IntegratedGrid(ListStore<T> store, ColumnModel<T> cm, boolean showBorderIn) {
        super(store, cm, showBorderIn);
        init();
    }

    public String getGridId() {

        return _id;
    }

    public HandlerRegistration addCsiDropEventHandler(CsiDropEventHandler handlerIn) {

        return addHandler(handlerIn, CsiDropEvent.type);
    }

    public void integrate(String[] friendsIn, String[] nonSpecificFriendsIn, DragLabelProvider labelProviderIn) {

        autoResizeColumns(true);

        ((InterchangeGridTarget<T>)_target).identifyFriends(friendsIn);
        ((InterchangeGridTarget<T>)_target).identifyNonSpecificFriends(nonSpecificFriendsIn);
        _target.setOperation(DND.Operation.COPY);
        _target.setFeedback(DND.Feedback.BOTH);

        ((InterchangeGridSource<T>)_source).setLabelProvider(labelProviderIn);
    }

    public void integrate(String[] friendsIn, String[] nonSpecificFriendsIn, String defaultIn) {

        autoResizeColumns(true);

        ((InterchangeGridTarget<T>)_target).identifyFriends(friendsIn);
        ((InterchangeGridTarget<T>)_target).identifyNonSpecificFriends(nonSpecificFriendsIn);
        _target.setOperation(DND.Operation.COPY);
        _target.setFeedback(DND.Feedback.BOTH);

        ((InterchangeGridSource<T>)_source).setDefault(defaultIn);
    }

    public void integrate(List<String> friendsIn, List<String> nonSpecificFriendsIn, String defaultIn) {

        autoResizeColumns(true);

        ((InterchangeGridTarget<T>)_target).identifyFriends(friendsIn);
        ((InterchangeGridTarget<T>)_target).identifyNonSpecificFriends(nonSpecificFriendsIn);
        _target.setOperation(DND.Operation.COPY);
        _target.setFeedback(DND.Feedback.BOTH);

        ((InterchangeGridSource<T>)_source).setDefault(defaultIn);
    }

    public void integrate(String[] friendsIn, String[] nonSpecificFriendsIn, SafeHtml defaultIn) {

        autoResizeColumns(true);

        ((InterchangeGridTarget<T>)_target).identifyFriends(friendsIn);
        ((InterchangeGridTarget<T>)_target).identifyNonSpecificFriends(nonSpecificFriendsIn);
        _target.setOperation(DND.Operation.COPY);
        _target.setFeedback(DND.Feedback.BOTH);

        ((InterchangeGridSource<T>)_source).setDefault(defaultIn);
    }

    public void integrate(List<String> friendsIn, List<String> nonSpecificFriendsIn, SafeHtml defaultIn) {

        autoResizeColumns(true);

        ((InterchangeGridTarget<T>)_target).identifyFriends(friendsIn);
        ((InterchangeGridTarget<T>)_target).identifyNonSpecificFriends(nonSpecificFriendsIn);
        _target.setOperation(DND.Operation.COPY);
        _target.setFeedback(DND.Feedback.BOTH);

        ((InterchangeGridSource<T>)_source).setDefault(defaultIn);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void setGridId(String idIn) {

        _id = idIn;
        ((InterchangeGridSource<T>)_source).setId(_id);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void init() {

        autoResizeColumns(true);

        _target = new InterchangeGridTarget<T>(this);
        _target.setOperation(DND.Operation.COPY);
        _target.setFeedback(DND.Feedback.BOTH);

        _source = new InterchangeGridSource<T>(this, _id);
    }
}
