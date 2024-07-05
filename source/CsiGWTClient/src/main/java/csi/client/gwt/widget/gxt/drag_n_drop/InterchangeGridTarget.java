package csi.client.gwt.widget.gxt.drag_n_drop;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.sencha.gxt.dnd.core.client.DndDragMoveEvent;
import com.sencha.gxt.dnd.core.client.DndDropEvent;
import com.sencha.gxt.dnd.core.client.DragSource;
import com.sencha.gxt.dnd.core.client.GridDropTarget;

import csi.client.gwt.events.CsiDropEvent;

/**
 * Created by centrifuge on 8/2/2016.
 */
public class InterchangeGridTarget<T> extends GridDropTarget<T> {

    private String[] _friends = null;
    private String[] _nonSpecifics = null;

    public InterchangeGridTarget(IntegratedGrid<T> gridIn) {

        super(gridIn);
    }

    public InterchangeGridTarget(IntegratedGrid<T> gridIn, String[] friendsIn, String[] nonSpecificFriendsIn) {

        super(gridIn);
        identifyFriends(friendsIn);
        identifyNonSpecificFriends(nonSpecificFriendsIn);
    }

    public InterchangeGridTarget(IntegratedGrid<T> gridIn, List<String> friendsIn, List<String> nonSpecificFriendsIn) {

        super(gridIn);
        identifyFriends(friendsIn);
        identifyNonSpecificFriends(nonSpecificFriendsIn);
    }

    public void identifyFriends(String[] friendsIn) {

        if ((null != friendsIn) && (0 < friendsIn.length)) {

            List<String> myFriends = new ArrayList<String>(friendsIn.length);

            for (int i = 0; friendsIn.length > i; i++) {

                String myFriend = friendsIn[i];

                if ((null != myFriend) && (0 < myFriend.length())) {

                    myFriends.add(myFriend);
                }
            }
            _friends = myFriends.toArray(new String[0]);
        }
    }

    public void identifyFriends(List<String> friendsIn) {

        if ((null != friendsIn) && (0 < friendsIn.size())) {

            List<String> myFriends = new ArrayList<String>(friendsIn.size());

            for (String myFriend : friendsIn) {

                if ((null != myFriend) && (0 < myFriend.length())) {

                    myFriends.add(myFriend);
                }
            }
            _friends = myFriends.toArray(new String[0]);
        }
    }

    public void identifyNonSpecificFriends(String[] friendsIn) {

        if ((null != friendsIn) && (0 < friendsIn.length)) {

            List<String> myNonSpecifics = new ArrayList<String>(friendsIn.length);

            for (int i = 0; friendsIn.length > i; i++) {

                String myNonSpecific = friendsIn[i];

                if ((null != myNonSpecific) && (0 < myNonSpecific.length())) {

                    myNonSpecifics.add(myNonSpecific);
                }
            }
            _nonSpecifics = myNonSpecifics.toArray(new String[0]);
        }
    }

    public void identifyNonSpecificFriends(List<String> friendsIn) {

        if ((null != friendsIn) && (0 < friendsIn.size())) {

            List<String> myNonSpecifics = new ArrayList<String>(friendsIn.size());

            for (String myNonSpecific : friendsIn) {

                if ((null != myNonSpecific) && (0 < myNonSpecific.length())) {

                    myNonSpecifics.add(myNonSpecific);
                }
            }
            _nonSpecifics = myNonSpecifics.toArray(new String[0]);
        }
    }

    @Override
    protected void onDragDrop(final DndDropEvent eventIn) {

        final Object myDataDrop = eventIn.getData();
        final T myDataTarget = grid.getSelectionModel().getSelectedItem();

        DeferredCommand.add(new Command() {

            public void execute() {

                grid.fireEvent(new CsiDropEvent(myDataDrop, myDataTarget));
            }
        });
    }

    @Override
    protected void showFeedback(DndDragMoveEvent eventIn) {

        activeItem = null;
        int idx = -1;

        if (isNonSpecific(eventIn)) {

            grid.getSelectionModel().deselectAll();
            eventIn.getStatusProxy().setStatus(true);

        } else {

            if (isValidSource(eventIn)) {

                EventTarget target = eventIn.getDragMoveEvent().getNativeEvent().getEventTarget();
                Element row = grid.getView().findRow(Element.as(target)).cast();

                idx = (row != null) ? grid.getView().findRowIndex(row) : -1;
                activeItem = ((0 <= idx) && (grid.getStore().size() > idx)) ? grid.getStore().get(idx) : null;
            }
            if (null != activeItem) {
                grid.getSelectionModel().select(idx, false);
                eventIn.getStatusProxy().setStatus(true);
            } else {
                grid.getSelectionModel().deselectAll();
                eventIn.getStatusProxy().setStatus(false);
//            eventIn.getStatusProxy().update("Reject item from unrecognized source.");
            }
        }
    }

    private boolean isNonSpecific(DndDragMoveEvent eventIn) {

        boolean myNonSpecific = false;

        if ((null != _nonSpecifics) && (0 < _nonSpecifics.length)) {

            DragSource mySource = eventIn.getDragSource();

            if (mySource instanceof InterchangeGridSource<?>) {

                String mySourceId = ((InterchangeGridSource) mySource).getId();

                if (null != mySourceId) {

                    for (int i = 0; _nonSpecifics.length > i; i++) {

                        if (mySourceId.equals(_nonSpecifics[i])) {

                            myNonSpecific = true;
                            break;
                        }
                    }
                }
            }
        }
        return myNonSpecific;
    }

    private boolean isValidSource(DndDragMoveEvent eventIn) {

        boolean myValidFlag = false;

        if ((null != _friends) && (0 < _friends.length)) {

            DragSource mySource = eventIn.getDragSource();

            if (mySource instanceof InterchangeGridSource<?>) {

                String mySourceId = ((InterchangeGridSource) mySource).getId();

                if (null != mySourceId) {

                    for (int i = 0; _friends.length > i; i++) {

                        if (mySourceId.equals(_friends[i])) {

                            myValidFlag = true;
                            break;
                        }
                    }
                }
            }
        }
        return myValidFlag;
    }
}
