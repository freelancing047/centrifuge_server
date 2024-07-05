package csi.client.gwt.viz.timeline.view;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.viz.timeline.presenters.legend.TimelineColorLegendPresenter;

public class LegendItemClickHandler implements ClickHandler {

    private TimelineColorLegendPresenter presenter;
    private String text;
    
    public LegendItemClickHandler(TimelineColorLegendPresenter presenter, String text) {
        this.presenter = presenter;
        this.text = text;
    }

    /**
     *  Control + Shift + Click -   remove the clicked category from selection, keeping others selected
     *  Control + Click         -   select the clicked category, keeping others selected
     *  Click                   -   select only the category clicked, removing all other existing selections
     *
     * @param event
     */
    @Override
    public void onClick(ClickEvent event) {

        if(event.isControlKeyDown() && event.isShiftKeyDown()){
            presenter.deselectByText(text);
        }else if(event.isControlKeyDown()){
            presenter.selectByText(text, false);
        }else{
            presenter.selectByText(text, true);
        }

        event.stopPropagation();
        event.preventDefault();
    }

}
