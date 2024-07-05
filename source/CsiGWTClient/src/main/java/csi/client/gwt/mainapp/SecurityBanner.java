package csi.client.gwt.mainapp;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import csi.client.gwt.WebMain;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.server.common.dto.ClientStartupInfo;
import csi.server.common.model.Resource;
import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 5/5/2015.
 */

public class SecurityBanner extends FullSizeLayoutPanel {

    private static final String DEFAULT_COLOR_KEY = "DEFAULT";
    private static final int SECURITY_BANNER_HEIGHT = 25;
    private static final int Z_INDEX = 2000;

    private static ClientStartupInfo clientInfo = null;
    private static String defaultBanner = null;
    private static ValuePair<String, String> defaultColor = null;
    private static Map<String, ValuePair<String, String>> bannerControl = null;
    private static SecurityBanner topBanner = null;
    private static SecurityBanner bottomBanner = null;

    private VerticalPanel vPanel;
    private HorizontalPanel hPanel;
    private Label label;
    private static String bannerText;

    public static String getBannerText() {
        return bannerText;
    }

    public static SecurityBanner getTopBanner() {

        if (null == topBanner) {
            topBanner = new SecurityBanner();
        }
        return topBanner;
    }

    public static SecurityBanner getBottomBanner() {

        if (null == bottomBanner) {
            bottomBanner = new SecurityBanner();
        }
        return bottomBanner;
    }

    public static void displayBanner(Resource resourceIn) {

        bannerText = getBanner(resourceIn);
        String myBanner = (null != bannerText) ? bannerText : defaultBanner;
        ValuePair<String, String> myColors = getColors(bannerText, myBanner);

        if (null != topBanner) {

            topBanner.defineDisplay(myBanner, myColors.getValue1(), myColors.getValue2());
        }
        if (null != bottomBanner) {

            bottomBanner.defineDisplay(myBanner, myColors.getValue1(), myColors.getValue2());
        }
    }

    public static int getHeight() {

        initialize();

        return ((null != clientInfo) && clientInfo.isProvideBanners()) ? SECURITY_BANNER_HEIGHT : 0;
    }

    private SecurityBanner() {

        super();

        vPanel = new VerticalPanel();
        hPanel = new HorizontalPanel();
        label = new Label();

        vPanel.setWidth("100%");
        vPanel.setHeight("100%");
        hPanel.setWidth("100%");
        hPanel.setHeight("100%");

        this.getElement().getStyle().setZIndex(Z_INDEX);
        this.add(vPanel);
        this.setWidgetLeftRight(vPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
        this.setWidgetTopBottom(vPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
        vPanel.setWidth("100%");
        vPanel.setHeight("100%");
        vPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        vPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        vPanel.add(hPanel);
        hPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        hPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        hPanel.add(label);
    }

    private void defineDisplay(String textIn, String colorIn, String backgroundIn) {

        this.getElement().getStyle().setBackgroundColor(backgroundIn);
        vPanel.getElement().getStyle().setBackgroundColor(backgroundIn);
        hPanel.getElement().getStyle().setBackgroundColor(backgroundIn);
        label.getElement().getStyle().setBackgroundColor(backgroundIn);
        label.getElement().getStyle().setColor(colorIn);
        label.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
        label.setText(textIn);
    }

    private static void initialize() {

        if (null == defaultBanner) {

            clientInfo = WebMain.getClientStartupInfo();

            if (null != clientInfo) {

                bannerControl = clientInfo.getBannerControl();
                defaultBanner = clientInfo.getDefaultBanner();

                if ((null == bannerControl) || (0 == bannerControl.keySet().size())) {

                    bannerControl = new HashMap<String, ValuePair<String, String>>();
                }

                defaultColor = bannerControl.get(DEFAULT_COLOR_KEY);

                if (null == defaultColor) {

                    defaultColor = new ValuePair<String, String>("black", "yellow");
                    bannerControl.put(DEFAULT_COLOR_KEY, defaultColor);
                }

                if (null == defaultBanner) {

                    defaultBanner = "";
                }
            }
        }
    }



    private static String getBanner(Resource resourceIn) {

        String myBanner = null;

        if (null != resourceIn) {

            initialize();

            if(clientInfo.isProvideCapcoBanners()) {

                if (clientInfo.isProvideTagBanners()) {

                    String myTagBanner = resourceIn.getSecurityBanner(clientInfo.getTagBannerPrefix(),
                                                                        clientInfo.getTagBannerDelimiter(),
                                                                        clientInfo.getTagBannerSubDelimiter(),
                                                                        clientInfo.getTagBannerSuffix(),
                                                                        clientInfo.getTagItemPrefix());
                    if (clientInfo.getUseAbreviations()) {

                        myBanner = resourceIn.getSecurityBannerAbr(defaultBanner, myTagBanner);

                    } else {

                        myBanner = resourceIn.getSecurityBanner(defaultBanner, myTagBanner);
                    }

                } else {

                    if (clientInfo.getUseAbreviations()) {

                        myBanner = resourceIn.getSecurityBannerAbr(defaultBanner);

                    } else {

                        myBanner = resourceIn.getSecurityBanner(defaultBanner);
                    }
                }

            } else if (clientInfo.isProvideTagBanners()) {

                myBanner = resourceIn.getSecurityBanner(clientInfo.getTagBannerPrefix(),
                                                        clientInfo.getTagBannerDelimiter(),
                                                        clientInfo.getTagBannerSubDelimiter(),
                                                        clientInfo.getTagBannerSuffix(),
                                                        clientInfo.getTagItemPrefix());
            }
        }
        return (null != myBanner) ? myBanner : defaultBanner;
    }

    public static ValuePair<String, String> getColors(String textIn, String BannerIn) {

        initialize();

        ValuePair<String, String> myPair = null;

        if (clientInfo.isProvideCapcoBanners()) {

            if ((null != BannerIn) && (0 < BannerIn.length())) {

                String[] myParts = BannerIn.split("//");

                myPair = bannerControl.get(myParts[0]);
            }
        } else if (clientInfo.isProvideTagBanners()) {

            if (null != textIn) {

                myPair = bannerControl.get("TAG");
            }
        }
        if (null == myPair) {

            myPair = defaultColor;
        }
        return myPair;
    }
}
