package csi.client.gwt.mainapp;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;

/**
 * Created by Ivan on 10/3/2017.
 */
public class LandingIconPanel{

    private HorizontalLayoutContainer container;

    public LandingIconPanel(String imageUri, SafeHtml safeHtml){
        container = new HorizontalLayoutContainer();
        container.add(createLabel(safeHtml.asString()), new HorizontalLayoutContainer.HorizontalLayoutData(0.25,1, new Margins(10)));
    }


//    public LandingIconPanel(SafeHtml safeHtml) {
//        super(safeHtml);
//    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.getElement().getStyle().setProperty("whiteSpace", "nowrap");
//        if (Theme.BLUE.isActive() || Theme.GRAY.isActive()) {
//            label.addStyleName(ThemeStyles.get().style().border());
//        }
//        label.addStyleName("pad-text gray-bg");

        return label;
    }
}
