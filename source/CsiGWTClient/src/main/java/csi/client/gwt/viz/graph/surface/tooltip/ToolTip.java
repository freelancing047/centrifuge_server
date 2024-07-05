package csi.client.gwt.viz.graph.surface.tooltip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.BasicPlace;
import csi.client.gwt.viz.graph.node.settings.tooltip.AnchorLinkType;
import csi.client.gwt.viz.graph.surface.GraphSurface;
import csi.client.gwt.viz.graph.surface.annotation.AnnotationPresenter;
import csi.config.advanced.graph.TooltipAdvConfig;
import csi.server.common.dto.CsiMap;
import csi.server.common.dto.graph.gwt.AbstractVisualItemTypeBase;
import csi.server.common.dto.graph.gwt.FindItemDTO;
import csi.server.common.dto.graph.gwt.NeighborsDTO;
import csi.server.common.dto.graph.gwt.TooltipPropsDTO;

public class ToolTip {

    private final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private GraphSurface surface;
    private ToolTipModel model;
    private ToolTipView view;
    private int anchorX;
    private int anchorY;
    private boolean stationary = false;
    private boolean moreDetails = true;
    private ClickHandler moreDetailsHandler;

    public ToolTip(final GraphSurface surface, final FindItemDTO result, int anchorX, int anchorY, ClickHandler moreDetailsHandler, boolean moreDetails) {
        if(moreDetailsHandler != null){
            this.setStationary(true);
            this.setMoreDetailsHandler(moreDetailsHandler);
        }
        this.setMoreDetails(moreDetails);
        this.surface = surface;
        EventBus eventBus = new SimpleEventBus();
        PlaceController placeController = new PlaceController(eventBus, new PlaceController.DefaultDelegate());
        ActivityMapper activityMapper = new ToolTipActivityMapper(this);
        new ActivityManager(activityMapper, eventBus);
        placeController.goTo(BasicPlace.DEFAULT_PLACE);
        model = new ToolTipModelImpl(result);
        view = new ToolTipViewImpl(this);

        this.anchorX = anchorX;
        this.anchorY = anchorY;

        if (result.getItemType().equals("node")) {//NON-NLS
            Optional<Double> displayX = model.getDisplayX();
            if (displayX.isPresent()) {
                this.anchorX = displayX.get().intValue();
            }
            Optional<Double> displayY = model.getDisplayY();
            if (displayY.isPresent()) {
                this.anchorY = displayY.get().intValue();

            }
        }
        // for (ToolTipItem item : ToolTipItems.values()) {
        // view.addItem(item);
        // }
        ArrayList<ToolTipItem> itemsToAdd = Lists.newArrayList();

        {//add Labels ToolTipItem if more than one label
            final List<String> labels = result.labels;
            if (labels != null) {
                if (labels.size() > 1) {
                    itemsToAdd.add(new ToolTipItem() {
                        @Override
                        public String getValue(ToolTipModel model) {
                            return Joiner.on("\n").join(labels);
                        }

                        @Override
                        public String getLabel() {
                            return i18n.tooltipLabel_Labels();
                        }
                    });
                }
            }
        }
        CsiMap<String, List<String>> attributeNames = result.getTooltips().getAttributeNames();

        CsiMap<String, CsiMap<String, String>> computed = result.getComputed();
        if (computed != null) {
            for (String s : computed.keySet()) {
                CsiMap<String, String> map = computed.get(s);
                List<String> strings = attributeNames.get(s);
                if (strings == null) {
                    strings = Lists.newArrayList();
                    attributeNames.put(internationalizeKey(s), strings);
                }
                for (String s1 : map.keySet()) {
                    try {
                        if (s1.equals("ALL")) {//NON-NLS
                            strings.add(map.get(s1));
                        }
                        //                        else if (result.getDirectionMap().get(s1).equals("Undirected")) {
                        //                            strings.add(map.get(s1) + " Undirected");
                        //                        }else{
                        //                            strings.add(map.get(s1) + " from " + result.getDirectionMap().get(s1));
                        //                        }

                    } catch (Exception ignored) {
                    }
                }
            }
        }
        TooltipAdvConfig tooltipAdvConfig = WebMain.getClientStartupInfo().getGraphAdvConfig().getTooltips();
        for (String s : attributeNames.keySet()) {
            final String t = internationalizeKey(s);
            final List<String> value = attributeNames.get(s);
            Collections.sort(value);
            final List<String> cleanedValues = Lists.newArrayList();
            for (int i = 0; i < value.size(); i++) {
                String value0 = value.get(i);
                // check if first value is JSON
                if (value0.indexOf('{') == 0) {
                    String url = null;
                    String text = null;
                    String value0Json = value0;
                    String countString = "";
                    int lastIndexOfSpace = 0;
                    if (value0.contains(" (")) {
                        // there is only a count appended if there is more than one.
                        lastIndexOfSpace = value0.lastIndexOf(" (");
                        value0Json = value0.substring(0, lastIndexOfSpace);
                        countString = value0.substring(lastIndexOfSpace);
                    }
                    try {
                        JSONValue value0JSON = JSONParser.parseStrict(value0Json);
                        if (value0JSON instanceof JSONObject) {
                            JSONObject jsonObject = (JSONObject) value0JSON;
                            JSONValue jsonValue = jsonObject.get(AnchorLinkType.URL);
                            if (jsonValue instanceof JSONString) {
                                JSONString jsonString = (JSONString) jsonValue;
                                url = jsonString.stringValue();
                            }
                            if (url != null) {
                                // POLICY CHECK
                                String scheme = UriUtils.extractScheme(url);
                                if (tooltipAdvConfig.isRequireSafeURI()) {
                                    if (!UriUtils.isSafeUri(url)) {
                                        break;
                                    }
                                }
                                // only use whitelist if we have one
                                boolean passedSchemeWhitelistCheck = tooltipAdvConfig.getUriSchemeWhiteList().length == 0;
                                for (int j = 0; j < tooltipAdvConfig.getUriSchemeWhiteList().length; j++) {
                                    if (tooltipAdvConfig.getUriSchemeWhiteList()[j].equals(scheme)) {
                                        passedSchemeWhitelistCheck = true;
                                        break;
                                    }
                                }
                                if (!passedSchemeWhitelistCheck) {
                                    break;
                                }
                                // only use whitelist if we have one
                                boolean passedUriPatternWhitelistCheck = tooltipAdvConfig.getUriPatternWhiteList().length == 0;
                                for (int j = 0; j < tooltipAdvConfig.getUriPatternWhiteList().length; j++) {
                                    try {
                                        RegExp r = RegExp.compile(tooltipAdvConfig.getUriPatternWhiteList()[j]);
                                        if (r.exec(url) != null) {
                                            passedUriPatternWhitelistCheck = true;
                                            break;
                                        }

                                    } catch (Exception e) {
                                        // don't crash, we can continue to evaluate the whitelist because they are independent
                                    }
                                }
                                if (!passedUriPatternWhitelistCheck) {
                                    break;
                                }

                                JSONValue jsonValue2 = jsonObject.get(AnchorLinkType.TEXT);
                                if (jsonValue2 instanceof JSONString) {
                                    JSONString jsonString = (JSONString) jsonValue2;
                                    text = jsonString.stringValue();
                                }
                                if (text == null) {
                                    text = url;
                                }

                                cleanedValues.add(i, "<a href=\"" + url + "\" target=\"_blank\">" + text + countString + "</a>");
                            }
                        }
                    } catch (Exception ignore) {

                        // failed parsing, no need to continue -- leave value unchanged
                        break;
                    }
                }
                if (cleanedValues.size() == value.size()) {
                    // if we successfully cleaned all values, replace the dirty ones.
                    value.clear();
                    value.addAll(cleanedValues);
                }
            }

            itemsToAdd.add(new ToolTipItem() {

                @Override
                public String getValue(ToolTipModel model) {
                    // FIXME: Probably need to work with Casey/Stan on specification
                    return Joiner.on("\n").join(value);
                }

                @Override
                public String getLabel() {
                    return t;
                }
            });
        }

        /* adding direction tooltip*/
        if (result.getDirectionMap() != null) {
            final HashMap<String, String> directionMap = result.getDirectionMap();
            Set<String> keySet = directionMap.keySet();
            for (java.lang.String key : keySet) {
                String value = directionMap.get(key);
                if (value.matches("Undirected \\(\\d*\\)")) {
                    directionMap.remove(key);
                }
            }

            if (!directionMap.isEmpty()) {

                itemsToAdd.add(new ToolTipItem() {
                    @Override
                    public String getValue(ToolTipModel model) {
                        Joiner.on("\n").join(directionMap.values());
                        return Joiner.on("\n").join(directionMap.values());
                    }

                    @Override
                    public String getLabel() {
                        return i18n.tooltipLabel_direction();
                    }
                });
            }
        }

        if ((result.isBundle() != null) && result.isBundle()) {
            //itemsToAdd.add(ToolTipItems.BUNDLE_CONTENTS);
            itemsToAdd.add(new ToolTipItem() {
                @Override
                public String getValue(ToolTipModel model) {
                    return CentrifugeConstantsLocator.get().tooltipValue_typeBundle();
                }

                @Override
                public String getLabel() {
                    return CentrifugeConstantsLocator.get().tooltipLabel_type();
                }
            });
        }

        // TODO: want to sort explicitly by order
        // List<NodeDef> nodeDefs = surface.getGraph().getModel().getRelGraphViewDef().getNodeDefs();

        Collections.sort(itemsToAdd, new Comparator<ToolTipItem>() {

            @Override
            public int compare(ToolTipItem o1, ToolTipItem o2) {
                return o1.getLabel().compareTo(o2.getLabel());
            }
        });
        Optional<Map<String, Integer>> tooltipOrderOptional = model.getTooltipOrder();
        if (tooltipOrderOptional.isPresent()) {
            final Map<String, Integer> tooltipOrder = tooltipOrderOptional.get();

            Collections.sort(itemsToAdd, new Comparator<ToolTipItem>() {

                @Override
                public int compare(ToolTipItem o1, ToolTipItem o2) {
                    if (o1 == null) {
                        if (o2 == null) {
                            return 0;
                        }
                        return 1;
                    }
                    if (o2 == null) {
                        return -1;
                    }
                    Integer integer1 = tooltipOrder.get(o1.getLabel());
                    Integer integer2 = tooltipOrder.get(o2.getLabel());
                    if ((integer1 == null) && (integer2 != null)) {
                        return 1;
                    }
                    if ((integer1 != null) && (integer2 == null)) {
                        return -1;
                    }
                    if ((integer1 == null) && (integer2 == null)) {
                        return 0;
                    }
                    return ComparisonChain.start().compare(integer1, integer2).compare(o1.getLabel(), o2.getLabel())
                            .result();
                }
            });
        }
        for (ToolTipItem toolTipItem : itemsToAdd) {

            if (toolTipItem.getLabel().equals(CentrifugeConstantsLocator.get().tooltipLabel_comments())) {
                List<Button> buttons = new ArrayList<>();

                Button button = new Button();
                button.setTitle(CentrifugeConstantsLocator.get().tooltip_editComment());
                button.setIcon(IconType.PENCIL);
                button.setType(ButtonType.LINK);
                button.getElement().getStyle().setMargin(0, Unit.PX);
                button.getElement().getStyle().setPadding(0, Unit.PX);

                //Bring up the htmleditor for this PIECE
                button.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        new AnnotationPresenter(surface.getGraph(), result).show();

                    }
                });

                buttons.add(button);

                button = new Button();
                button.setTitle(CentrifugeConstantsLocator.get().tooltip_removeComment());
                button.setIcon(IconType.REMOVE);
                button.setType(ButtonType.LINK);
                button.getElement().getStyle().setMargin(0, Unit.PX);
                button.getElement().getStyle().setPadding(0, Unit.PX);
                button.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        new AnnotationPresenter(surface.getGraph(), result).remove();
                    }
                });
                buttons.add(button);

                view.addItemWithButtons(toolTipItem, buttons);
            } else {
                if (toolTipItem.getLabel().equals(CentrifugeConstantsLocator.get().tooltipLabel_type()) && toolTipItem.getValue(null).equals(i18n.tooltipValue_typeBundle())) {
                    final StringBuffer sb = new StringBuffer();
                    boolean first = true;
                    if(result.tooltips.getContents() != null){
                        for (Map<String, String> child : result.tooltips.getContents()) {
                            if (first) {
                                first = false;
                            } else {
                                sb.append("\n");
                            }
                            sb.append(child.get("label"));//NON-NLS
                            for (String key : child.keySet()) {
                                if (key.equals("label")) {//NON-NLS
                                    break;
                                }
                                sb.append(";").append(key).append("=").append(child.get(key));
                            }
                        }
                    }
                    view.addItem(new ToolTipItem() {
                        @Override
                        public String getValue(ToolTipModel model) {
                            return sb.toString();
                        }

                        @Override
                        public String getLabel() {
                            return i18n.tooltipLabel_contains();
                        }
                    });
                }
                view.addItem(toolTipItem);
            }

        }
        view.setHeading(model.getHeading());
        if(isStationary()){
            view.addMoreLink(isMoreDetails());
        }
    }



    private String internationalizeKey(String s) {
        if(s == null){
            return "";
        }else if(s.equals("Label")){
			return CentrifugeConstantsLocator.get().tooltipLabel_label();
		} else if(s.equals("Type")){
			return CentrifugeConstantsLocator.get().type();
		} else if(s.equals("Member Types")){
			return CentrifugeConstantsLocator.get().tooltipLabelMemberTypes();
		}
		return s;
	}

    public boolean isWithin(int x, int y) {
        Element element = getView().asWidget().getElement();
        int left = element.getOffsetLeft();
        int top = element.getOffsetTop();
        if((x >= (left - 5)) && (y >= (top - 5))){
            int height = element.getOffsetHeight()+5;
            int width = element.getOffsetWidth()+5;
            if((x <= (left + width)) && (y <= (top + height))){
                return true;
            }
        }
        return false;
    }

	public GraphSurface getSurface() {
        return surface;
    }

    public String getID() {
        Optional<String> itemKey = model.getItemKey();
        if (itemKey.isPresent()) {
            return itemKey.get();
        }
        return null;
    }

    public Widget getWidget() {
        return view.asWidget();
    }

    public double getItemX() {
        return anchorX;
    }

    public double getItemY() {
        return anchorY;
    }

    public void setVisible(boolean visible) {
        view.asWidget().setVisible(visible);
    }

    public ToolTipModel getModel() {
        return model;
    }

    public void moveAnchor(int deltaX, int deltaY) {
        anchorX += deltaX;
        anchorY += deltaY;
    }

    public ToolTipView getView() {
        return view;
    }

    public boolean isMouseOver() {
        return view.isMouseOver();
    }

    public boolean isStationary() {
        return stationary;
    }


    public void setStationary(boolean stationary) {
        this.stationary = stationary;
    }

    public ClickHandler getMoreDetailsHandler() {
        return moreDetailsHandler;
    }



    public void setMoreDetailsHandler(ClickHandler moreDetailsHandler) {
        this.moreDetailsHandler = moreDetailsHandler;
    }

    public boolean isMoreDetails() {
        return moreDetails;
    }



    public void setMoreDetails(boolean moreDetails) {
        this.moreDetails = moreDetails;
    }

    protected interface ToolTipModel {

        String getHeading();

        Optional<String> getItemKey();

        Optional<Double> getItemX();

        Optional<Double> getItemY();

        Optional<Integer> getID();

        Optional<Double> getDisplayY();

        Optional<Double> getDisplayX();

        Optional<Double> getClickY();

        Optional<Double> getClickX();

        Optional<Boolean> isSelected();

        Optional<String> getItemId();

        Optional<String> getItemType();

        Optional<String> getObjectType();

        Optional<Boolean> isAnchored();

        Optional<Boolean> hideLabels();

        Optional<Double> getSize();

        Optional<Integer> getComponentId();

        Optional<String> getLabel();

        Optional<String> getBundleCount();

        Optional<Boolean> isHidden();

        Optional<Boolean> isBundle();

        Optional<Map<String, Integer>> getNeighborTypeCounts();

        Optional<List<NeighborsDTO>> getNeighbors();

        Optional<TooltipPropsDTO> getTooltipPropsDTO();

        Optional<Boolean> isVisualized();

        Optional<AbstractVisualItemTypeBase> getVisualItemTypeBase();

        Optional<Integer> getCountInDispEdges();

        Optional<Integer> getSubGraphNodeId();

        Optional<HashMap<String, String>> getDirectionMap();

        Optional<CsiMap<String, CsiMap<String, String>>> getComputedFields();

        Optional<Map<String, Integer>> getTooltipOrder();

        Optional<List<Map<String, String>>> getBundleContents();
    }

    protected interface ToolTipView extends IsWidget {

        void setHeading(String heading);

        void addMoreLink(boolean b);

        void addRow(String string, String string2);

        Widget getMoveHandle();

        void addItem(ToolTipItem item);

        void updateBodyHeight();

        void setDragContainer(Widget w);

        boolean isMouseOver();

        void addItemWithButtons(ToolTipItem item, List<Button> button);

    }



}
