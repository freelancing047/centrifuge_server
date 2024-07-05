package csi.server.common.model.visualization.selection;

import com.google.gwt.user.client.rpc.IsSerializable;


public class TimelineEventSelection extends IntPrimitiveSelection implements IsSerializable{

    private String vizUuid;
    
    //Have to put this for gwt, don't want people to use it though
    protected TimelineEventSelection(){
        super();
    }
    
    public TimelineEventSelection(String vizUuid){
        super();
        this.setVizUuid(vizUuid);
    }
    
    @Override
    public void setFromSelection(Selection selection) {
        if(!(selection instanceof TimelineEventSelection)){
            clearSelection();
            return;
        }

        TimelineEventSelection rowsSelection = (TimelineEventSelection)selection;
        makeSelectionStateForRows(rowsSelection.getSelectedItems());
    }

    @Override
    public Selection copy() {
         TimelineEventSelection selection = new TimelineEventSelection(getVizUuid());
         return selection;
    }

    public String getVizUuid() {
        return vizUuid;
    }

    public void setVizUuid(String vizUuid) {
        this.vizUuid = vizUuid;
    }




}
