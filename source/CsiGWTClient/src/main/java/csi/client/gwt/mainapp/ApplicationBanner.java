package csi.client.gwt.mainapp;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import csi.client.gwt.WebMain;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.server.common.dto.ApplicationLabelConfig;

/**
 * Created by centrifuge on 5/26/2015.
 */
public class ApplicationBanner extends FullSizeLayoutPanel {

    private static final int INFO_BANNER_HEIGHT = 25;

    private static ApplicationLabelConfig configuration = null;

    private static ApplicationBanner banner = null;

    private VerticalPanel leftPanel;
    private VerticalPanel centerPanel;
    private VerticalPanel rightPanel;
    private Label leftLabel;
    private Label centerLabel;
    private Label rightLabel;
    private Label spacerLabel;


    public static ApplicationBanner getBanner() {

        if (null == banner) {

            banner = new ApplicationBanner();
        }
        return banner;
    }

    public static int getHeight() {

        return displayNormal() ? INFO_BANNER_HEIGHT : 0;
    }

    public static int getFullScreenHeight() {

        return displayFullScreen() ? INFO_BANNER_HEIGHT : 0;
    }

    private ApplicationBanner() {

        super();

        getConfiguration();

        String myForeground = (null != configuration) ? configuration.getHeaderForegroundColor() : "white";
        String myBackground = (null != configuration) ? configuration.getHeaderBackgroundColor() : "white";
        String myLeftLabel = (null != configuration) ? configuration.getHeaderLeftLabel() : "";
        String myCenterLabel = (null != configuration) ? configuration.getHeaderCenterLabel() : "";
        String myRightLabel = (null != configuration) ? configuration.getHeaderRightLabel() : "";

        leftPanel = new VerticalPanel();
        centerPanel = new VerticalPanel();
        rightPanel = new VerticalPanel();
        leftLabel = new Label(myLeftLabel);
        centerLabel = new Label(myCenterLabel);
        rightLabel = new Label(myRightLabel);

        leftPanel.setHeight("100%");
        leftPanel.setWidth("100%");
        centerPanel.setHeight("100%");
        centerPanel.setWidth("100%");
        rightPanel.setHeight("100%");
        rightPanel.setWidth("100%");

        leftPanel.getElement().getStyle().setBackgroundColor(myBackground);
        leftLabel.getElement().getStyle().setBackgroundColor(myBackground);
        leftLabel.getElement().getStyle().setColor(myForeground);
        centerPanel.getElement().getStyle().setBackgroundColor(myBackground);
        centerLabel.getElement().getStyle().setBackgroundColor(myBackground);
        centerLabel.getElement().getStyle().setColor(myForeground);
        rightPanel.getElement().getStyle().setBackgroundColor(myBackground);
        rightLabel.getElement().getStyle().setBackgroundColor(myBackground);
        rightLabel.getElement().getStyle().setColor(myForeground);

        leftLabel.getElement().getStyle().setPaddingLeft(10, Style.Unit.PX);
        leftLabel.getElement().getStyle().setPaddingRight(10, Style.Unit.PX);
        centerLabel.getElement().getStyle().setPaddingLeft(10, Style.Unit.PX);
        centerLabel.getElement().getStyle().setPaddingRight(10, Style.Unit.PX);
        rightLabel.getElement().getStyle().setPaddingLeft(10, Style.Unit.PX);
        rightLabel.getElement().getStyle().setPaddingRight(10, Style.Unit.PX);

        this.add(leftPanel);
        this.setWidgetLeftWidth(leftPanel, 0, Style.Unit.PCT, 33, Style.Unit.PCT);
        this.setWidgetTopBottom(leftPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);

        this.add(centerPanel);
        this.setWidgetLeftRight(centerPanel, 33, Style.Unit.PCT, 33, Style.Unit.PCT);
        this.setWidgetTopBottom(centerPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);

        this.add(rightPanel);
        this.setWidgetRightWidth(rightPanel, 0, Style.Unit.PCT, 33, Style.Unit.PCT);
        this.setWidgetTopBottom(rightPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);

        leftPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        leftPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        leftPanel.add(leftLabel);

        centerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        centerPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        centerPanel.add(centerLabel);

        rightPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        rightPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        rightPanel.add(rightLabel);

        addHandlers();
    }

    private static boolean displayNormal() {

        getConfiguration();

        return (null != configuration) ? configuration.getIncludeHeaderLabels() : false;
    }

    private static boolean displayFullScreen() {

        getConfiguration();

        return ((null != configuration) && configuration.getIncludeHeaderLabels())
                ? configuration.getIncludeFullScreenHeaderLabels() : false;
    }

    private static ApplicationLabelConfig getConfiguration() {

        if (null == configuration) {

            ApplicationLabelConfig myConfiguration = WebMain.getClientStartupInfo().getApplicationBannerConfiguration();

            if ((null != myConfiguration) && (myConfiguration.getIncludeHeaderLabels())) {

                configuration = myConfiguration;
            }
        }
        return configuration;
    }

    private void addHandlers() {

        if (null != configuration) {

            final String myLeftLink = configuration.getHeaderLeftLink();
            final String myCenterLink = configuration.getHeaderCenterLink();
            final String myRightLink = configuration.getHeaderRightLink();

            if ((null != myLeftLink) && (0 < myLeftLink.length())) {

                leftLabel.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {

                        Window.open(myLeftLink, configuration.getHeaderLeftLabel(), null);
                    }
                });
                leftLabel.getElement().getStyle().setCursor(Style.Cursor.POINTER);
            }

            if ((null != myCenterLink) && (0 < myCenterLink.length())) {

                centerLabel.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {

                        Window.open(myCenterLink, configuration.getHeaderCenterLabel(), null);
                    }
                });
                centerLabel.getElement().getStyle().setCursor(Style.Cursor.POINTER);
            }

            if ((null != myRightLink) && (0 < myRightLink.length())) {

                rightLabel.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {

                        Window.open(myRightLink, configuration.getHeaderRightLabel(), null);
                    }
                });
                rightLabel.getElement().getStyle().setCursor(Style.Cursor.POINTER);
            }
        }
    }
}
