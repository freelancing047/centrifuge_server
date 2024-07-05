package csi.client.gwt.widget.boot;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.cell.core.client.ButtonCell;


public class BooleanButtonCell extends ButtonCell<Boolean> {

    private ImageResource _trueIcon;
    private ImageResource _falseIcon;
    private Boolean _current;
    
    public BooleanButtonCell(ImageResource trueIconIn, ImageResource falseIconIn) {
        
        _trueIcon = trueIconIn;
        _falseIcon = falseIconIn;
    }
    
    @Override
    public void render(Context contextIn, Boolean valueIn, SafeHtmlBuilder htmlBuilderIn) {
        
        if ((null == _current) || (valueIn != _current)) {
            
            if (valueIn) {
                setIcon(_trueIcon);
            } else {
                setIcon(_falseIcon);
            }
            _current = valueIn;
        }
        super.render(contextIn, false, htmlBuilderIn);
    }
}
