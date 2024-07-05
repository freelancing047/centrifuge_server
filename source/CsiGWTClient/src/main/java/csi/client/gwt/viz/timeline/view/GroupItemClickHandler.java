package csi.client.gwt.viz.timeline.view;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;

import csi.client.gwt.viz.matrix.UpdateEvent;
import csi.client.gwt.viz.timeline.model.GroupingItem;
import csi.client.gwt.viz.timeline.presenters.legend.TimelineGroupLegendPresenter;
import csi.client.gwt.viz.timeline.view.metrics.TimelineMetricsView;

public class GroupItemClickHandler implements ClickHandler {

    private TimelineGroupLegendPresenter presenter;
    private String text;
    private GroupingItem item;
    
    public GroupItemClickHandler(IsWidget widget, TimelineGroupLegendPresenter presenter, String text) {
        this.presenter = presenter;
        this.text = text;
        this.item = (GroupingItem) widget;
    }

    @Override
    public void onClick(ClickEvent event) {
        //We update the checkbox in case they clicked on the link

        boolean isShift = event.getNativeEvent().getShiftKey();
        boolean isControl = event.getNativeEvent().getCtrlKey();
        if (isControl && !isShift ) {
            presenter.getPresenter().setSelectionByGroup(text, true);
        }else if (isControl && isShift ) {
            presenter.getPresenter().setSelectionByGroup(text, false);
        }else {
            presenter.toggleTrack(this.text);
            Scheduler.get().scheduleDeferred(() -> item.updateCheckBox(presenter.isTrackVisibile(text)));
            event.stopPropagation();
            event.preventDefault();
        }

        TimelineMetricsView.EVENT_BUS.fireEvent(new UpdateEvent(presenter.getPresenter().getUuid()));
    }

}
