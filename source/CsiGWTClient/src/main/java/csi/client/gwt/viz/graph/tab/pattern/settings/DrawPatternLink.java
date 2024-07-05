package csi.client.gwt.viz.graph.tab.pattern.settings;

import com.google.gwt.canvas.dom.client.Context2d;

import csi.client.gwt.util.HasXY;
import csi.client.gwt.widget.drawing.Edge;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternLink;

public class DrawPatternLink extends Edge {
    private PatternLink link;
    private boolean edit;
    private boolean over;

    public DrawPatternLink(HasXY node1, HasXY node2) {
        super(node1, node2);
        setColor("#000000");
        this.width = 12;
    }

    public PatternLink getLink() {
        return link;
    }

    public void setLink(PatternLink link) {
        this.link = link;
    }

    @Override
    public void render(Context2d context2d) {
        if(edit){
            setColor("blue");//NON-NLS
        }
        else if(over){
            setColor("green");//NON-NLS
        }
        else{
            setColor("black");//NON-NLS
        }
        super.render(context2d);
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public boolean isEdit() {
        return edit;
    }

    public void setOver(boolean over) {
        this.over = over;
    }

    public boolean isOver() {
        return over;
    }
}
