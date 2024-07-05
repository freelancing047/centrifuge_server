package csi.client.gwt.icon.ui;

import java.util.*;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Label;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.constants.LabelType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.common.base.Joiner;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Image;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.XDOM;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.client.util.Padding;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import csi.client.gwt.WebMain;
import csi.client.gwt.csi_resource.filters.ResourceFilterDialog;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.icon.IconEditPresenter;
import csi.client.gwt.icon.IconRepeatingCommand;
import csi.client.gwt.icon.IconSelectionEvent;
import csi.client.gwt.icon.IconSelectionHandler;
import csi.client.gwt.icon.IconUploadPresenter;
import csi.client.gwt.util.Display;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.combo_boxes.StringComboBox;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.list_boxes.ResourceFilterListBox;
import csi.server.common.dto.user.preferences.ResourceFilter;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.icons.Icon;
import csi.server.common.model.icons.IconResultDto;
import csi.server.common.service.api.IconActionsServiceProtocol;
import csi.shared.core.icon.IconInfoDto;

public class IconPanel {
    private static final String ICON_TAG_STYLE = "csi-icon-tag"; //$NON-NLS-1$
    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private static int ADD_PANEL_HEIGHT = 30;
    private static final int SCROLL_HANDLER_DELAY = 200;
    private static final String ICON_TAG_SELECTED_STYLE = "csi-icon-tag-selected";
    private int currentWindowHeight = 440;
    private int scrollPanelWidth = 455;
    private int currentWindowWidth = 650;
    private int extraSpace = 40;
    private boolean editAccess = false;
    private ScrollPanel scrollPanel;
    private AbsolutePanel iconPanel;
    private AbsolutePanel parentPanel;
    private FlowPanel editPanel;
    private FlowPanel tagsPanel;
    private ScrollPanel tagScrollPanel;
    private Map<Integer, IconContainer> loadedIcons = new HashMap<>();
    private HandlerManager handlerManager;
    private final int ICON_VERTICAL_SPACING = 20;
    private final int ICON_HORIZONTAL_SPACING = 10;
    private int iconsPerPage = 1;
    private int iconCount = 0;
    private int iconTotalCount = 0;
    private int iconsPerRow = 1;
    private IconRepeatingCommand command = null;
    private com.github.gwtbootstrap.client.ui.Icon spinnerIcon = new com.github.gwtbootstrap.client.ui.Icon(IconType.SPINNER);
    private boolean locked = false;
    private List<String> tags = null;
    private StringComboBox tagComboBox;
    private ResizeableGrid<String> tagsGrid;
    private GridContainer gridContainer;
    private String typedText = null;
    private IconUploadPresenter presenter;
    private FlowPanel addPanel;
    private String queryText = null;

    private VerticalLayoutContainer multiSelectOptionPanel;
    private HashSet<IconContainer> selectedIcons = new HashSet<>();

    private IconContainer currentlySelectedIcon;
    private ScrollHandler scrollHandler = null;
    private boolean keepSelection;
    private ResourceFilterDialog _filterEditDialog;
    private ResourceFilter _filter;
    private TextBox iconFilter;

    private ResourceFilterListBox filterDropDown;
    private AbsolutePanel noResultsIcons;
    private AbsolutePanel tagFilterPanel;

    private StringComboBox multiTagCombo;
    private String multiTag;

    private void createMultiDeletePanel() {
        multiSelectOptionPanel = new VerticalLayoutContainer();
        multiSelectOptionPanel.setWidth(currentWindowWidth - scrollPanelWidth + "px");
        multiSelectOptionPanel.setHeight(currentWindowHeight + "px");

        //title and back button
        HBoxLayoutContainer title = new HBoxLayoutContainer();
        title.setPadding(new Padding(5));
        // create label for heading
        Label label = new Label("Bulk Icon Operations");
        label.setType(LabelType.INFO);
        label.getElement().getStyle().setBackgroundColor("white");
        label.getElement().getStyle().setColor("black");
        label.getElement().getStyle().setFontSize(9, Unit.PT);
        label.getElement().getStyle().setProperty("textShadow", "none");

        // create back button
        Button closeButton = new Button(i18n.iconPanelCloseButton(), IconType.ARROW_LEFT); //$NON-NLS-1$
        closeButton.setType(ButtonType.LINK);


        title.add(label, new BoxLayoutContainer.BoxLayoutData(new Margins(5, 5, 0, 0)));
        title.add(closeButton, new BoxLayoutContainer.BoxLayoutData(new Margins(0)));

        // add to main panel
        multiSelectOptionPanel.add(title);
        Button del = new Button();
        //todo i18n
        del.setText("Bulk Delete");
        del.setType(ButtonType.DANGER);

        del.setWidth(currentWindowWidth - scrollPanelWidth - 10 + "px");

        del.addDomHandler(selectEvent -> {
            setLocked(true);
            List<String> idsToDelete = new ArrayList<>(selectedIcons.size());
            for (IconContainer i : selectedIcons) {
                idsToDelete.add(i.getIconUuid());
            }

            VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();

            future.addEventHandler(new AbstractVortexEventHandler<Void>() {
                @Override
                public void onSuccess(Void result) {
                    initializeScroll(false);
                }

                @Override
                public boolean onError(Throwable t) {
                    spinnerIcon.setVisible(false);
                    Display.error("IconPanel", 1, t);
                    return false;
                }
            });

            try {
                future.execute(IconActionsServiceProtocol.class).deleteIcons(idsToDelete);
            } catch (CentrifugeException e) {
                Display.error("IconPanel", 2, e);
                e.printStackTrace();
            }

            hideDeletePanel();
            setLocked(false);
        }, ClickEvent.getType());


        multiSelectOptionPanel.add(del);


        HBoxLayoutContainer c = new HBoxLayoutContainer();

        c.setPadding(new Padding(10));
        c.setHBoxLayoutAlign(HBoxLayoutContainer.HBoxLayoutAlign.MIDDLE);
        c.setPack(BoxLayoutContainer.BoxLayoutPack.START);


        multiTagCombo = createEmptyStringCombo();

        multiTagCombo.addKeyUpHandler(event -> {
            multiTag = multiTagCombo.getText();
            if (KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
                if (!tagsGrid.getStore().getAll().contains(multiTag))
                    tagsGrid.getStore().add(multiTag);

                multiTagCombo.setText(multiTag);
            }
        });

        multiTagCombo.addBlurHandler(event -> multiTagCombo.setText(multiTag));

        multiTagCombo.addSelectionHandler(event -> multiTag = event.getSelectedItem());

        Button addTagToSelectedIcons = new Button();
        addTagToSelectedIcons.setText("Add Tag");

        BoxLayoutContainer.BoxLayoutData boxLayoutData = new BoxLayoutContainer.BoxLayoutData(new Margins(25, 5, 0, 0));

        c.add(multiTagCombo, boxLayoutData);

        c.add(addTagToSelectedIcons, new BoxLayoutContainer.BoxLayoutData(new Margins(5, 5, 0, 0)));

        multiSelectOptionPanel.add(c);

        closeButton.addClickHandler(event -> {
            deselectAll();
            hideDeletePanel();
            clearAndReload();
            addTagToSelectedIcons.setText("Add Tag");
            addTagToSelectedIcons.setType(ButtonType.DEFAULT);
        });

        multiTagCombo.addTriggerClickHandler(event -> {
            addTagToSelectedIcons.setText("Add Tag");
            addTagToSelectedIcons.setType(ButtonType.DEFAULT);
        });
        multiTagCombo.setWidth(currentWindowWidth - scrollPanelWidth + "px");
        addTagToSelectedIcons.setWidth(currentWindowWidth - scrollPanelWidth - 15 + "px");
        addTagToSelectedIcons.addClickHandler(event -> {
            if (multiTag != null && !multiTag.isEmpty()) {
                if (!tagsGrid.getStore().getAll().contains(multiTag))
                    tagsGrid.getStore().add(multiTag);

                multiTagCombo.setText(multiTag);
                if (!tags.contains(multiTag)) {
                    tags.add(multiTag);
                    Collections.sort(tags);
                }


                List<String> tgs = new ArrayList<>();
                tgs.add(multiTag);

                List<String> iconIds = new ArrayList<>();
                for (IconContainer i : selectedIcons) {
                    iconIds.add(i.getIconUuid());
                }
                VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();

                future.addEventHandler(new AbstractVortexEventHandler<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        addTagToSelectedIcons.setType(ButtonType.SUCCESS);
                        addTagToSelectedIcons.setText("Added " + tgs.size() + " tag to " + iconIds.size() + " icons");
                    }

                    @Override
                    public boolean onError(Throwable t) {
                        spinnerIcon.setVisible(false);
                        Display.error("IconPanel", 3, t);
                        return false;
                    }
                });

                try {
                    future.execute(IconActionsServiceProtocol.class).addTagsToIcons(iconIds, tgs);
                    addTagToSelectedIcons.setLoadingText("Updating");
//                    addTagToSelectedIcons.loading
                } catch (CentrifugeException e) {
                    Display.error("IconPanel", 4, e);
                    e.printStackTrace();
                }


            }
        });

        multiSelectOptionPanel.setVisible(false);

        parentPanel.add(multiSelectOptionPanel);
    }

    private void showMultiDeletePanel() {

        if (multiSelectOptionPanel == null) {
            createMultiDeletePanel();
        }

        tagScrollPanel.setVisible(false);
        tagFilterPanel.setVisible(false);

        if (tags.size() > 0) {
            multiTagCombo.getStore().clear();
            multiTagCombo.getStore().addAll(tags);
            multiTagCombo.setVisible(editAccess);

        }

        multiSelectOptionPanel.setVisible(true);

    }

    // hides the delete panel and shows te filter tag panel
    private void hideDeletePanel() {
        multiSelectOptionPanel.setVisible(false);
        tagFilterPanel.setVisible(true);
        tagScrollPanel.setVisible(true);
    }

    IconPanel(IconUploadPresenter presenter, csi.client.gwt.widget.buttons.Button button) {
        keepSelection = button.isVisible();
        parentPanel = new AbsolutePanel();


        iconPanel = new AbsolutePanel();
        tagsPanel = new FlowPanel();
        tagScrollPanel = new ScrollPanel();
        editPanel = new FlowPanel();
        editPanel.setVisible(false);
        addPanel = new FlowPanel();
        scrollPanel = new ScrollPanel(iconPanel);
        this.presenter = presenter;

        presenter.setLoadHandler(new AbstractVortexEventHandler<String>() {

            @Override
            public void onSuccess(String result) {
                populateEditPane(result);
                initializeScroll(true);
            }

            @Override
            public boolean onError(Throwable t) {
                Display.error("IconPanel", 7, t);
                spinnerIcon.setVisible(false);
                return false;
            }
        });
        createSpinner();

        parentPanel.add(scrollPanel);
        parentPanel.add(editPanel);
        tagScrollPanel.add(tagsPanel);
        parentPanel.add(tagScrollPanel);

        editPanel.setWidth(currentWindowWidth - scrollPanelWidth + "px"); //$NON-NLS-1$
        editPanel.setHeight(currentWindowHeight + "px"); //$NON-NLS-1$
        tagScrollPanel.setWidth(currentWindowWidth - scrollPanelWidth + "px"); //$NON-NLS-1$
        tagsPanel.setWidth("100%"); //$NON-NLS-1$
        tagScrollPanel.setHeight((currentWindowHeight) + "px"); //$NON-NLS-1$
        scrollPanel.setHeight(currentWindowHeight + "px"); //$NON-NLS-1$
        scrollPanel.setWidth(scrollPanelWidth + "px"); //$NON-NLS-1$

        addPanel.setHeight(ADD_PANEL_HEIGHT + "px"); //$NON-NLS-1$
        parentPanel.setHeight(currentWindowHeight + "px"); //$NON-NLS-1$
        parentPanel.setWidth(currentWindowWidth + "px"); //$NON-NLS-1$

        scrollPanel.getElement().getStyle().setLeft(currentWindowWidth - scrollPanelWidth, Unit.PX);
        //make sure is always smaller
        iconPanel.setWidth(scrollPanelWidth - 10 + "px"); //$NON-NLS-1$
        scrollPanel.getElement().getStyle().setOverflowX(Overflow.HIDDEN);
        parentPanel.getElement().getStyle().setOverflowX(Overflow.HIDDEN);
        iconPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
        scrollPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
        editPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);
        tagsPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);

        initializeScroll(false);

        handlerManager = new HandlerManager(iconPanel.asWidget());


        queryForTags(null);

        iconPanel.addDomHandler((ClickEvent event) -> {
            int x = event.getRelativeX(iconPanel.getElement());
            int y = event.getRelativeY(iconPanel.getElement());

            for (Integer index : loadedIcons.keySet()) {
                if (loadedIcons.get(index).hitTest(x, y)) {
                    IconContainer hit = loadedIcons.get(index);
                    if (hit != null) {
                        selectIcon(hit, event.isControlKeyDown());
                        if (selectedIcons.size() > 1) {
                            toggleLeftPanel(false);
                            showMultiDeletePanel();
                            //multidelete
                        } else if (selectedIcons.size() == 1) {
                            hideDeletePanel();
                            populateEditPane(hit.getIconUuid());
                        } else {
                            toggleLeftPanel(true);
                        }

                    }

                    return;
                }
            }

        }, ClickEvent.getType());


        button.addClickHandler(event -> {
            if (selectedIcons.size() > 0) {
                handlerManager.fireEvent(new IconSelectionEvent(selectedIcons.iterator().next()));
            }

        });


        createMultiDeletePanel();
    }

    private void selectIcon(IconContainer iconContainer, boolean control) {

        if (editAccess && control) {
            if (iconContainer == null) {
                return;
            }

            if (selectedIcons.contains(iconContainer)) {
                iconContainer.setSelected(false);
                selectedIcons.remove(iconContainer);
            } else {
                iconContainer.setSelected(true);
                selectedIcons.add(iconContainer);
            }

        } else {
            if (selectedIcons.size() == 0) {
                iconContainer.setSelected(true);
                selectedIcons.add(iconContainer);
            } else {
                deselectAll();
                iconContainer.setSelected(true);
                selectedIcons.add(iconContainer);
            }

        }

    }

    private void deselectAll() {
        for (IconContainer i : selectedIcons) {
            i.setSelected(false);
        }
        selectedIcons.clear();
    }

    private void createSpinner() {
        spinnerIcon.setIconSize(IconSize.FOUR_TIMES);
        spinnerIcon.setSpin(true);
        spinnerIcon.addStyleName("csi-icon-spinner"); //$NON-NLS-1$
        parentPanel.add(spinnerIcon);
        positionSpinner();
        spinnerIcon.getElement().getStyle().setZIndex(XDOM.getTopZIndex());
        spinnerIcon.setVisible(false);
    }

    private void positionSpinner() {
        int height = spinnerIcon.getOffsetHeight();
        int width = spinnerIcon.getOffsetWidth();

        Style style = spinnerIcon.getElement().getStyle();
        style.setMarginLeft(currentWindowWidth - scrollPanelWidth + scrollPanelWidth / 2 - width / 2, Unit.PX);
        style.setMarginTop(currentWindowHeight / 2 - height / 2, Unit.PX);
        style.setPosition(Position.ABSOLUTE);
    }

    private void initializeScroll(final boolean isUpdate) {
        VortexFuture<IconInfoDto> future = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<IconInfoDto>() {

            @Override
            public void onSuccess(final IconInfoDto result) {
                int count = iconCount;
                editAccess = result.hasEditAccess();
                addPanel.setVisible(editAccess);

                try {
                    if (result.getCount() == 0) {
                        // show no icons....
                        showNoResultsIcons();
                        return;
                    } else {
                        showIconPanel();
                    }
                    count = result.getCount();
                } catch (Exception e) {
                    Display.error("IconPanel", 8, e);
//                    return;
                }
                if (count != iconCount) {
                    iconCount = count;
                    if (loadedIcons.isEmpty()) {
                        iconPanel.clear();
                        initialIconPopulation();
                    } else {
                        double maxScrollPosition = (double) scrollPanel.getMaximumVerticalScrollPosition();
                        double scrollRatio = 0;
                        if (maxScrollPosition >= 0) {
                            scrollRatio = ((double) scrollPanel.getVerticalScrollPosition()) / ((double) scrollPanel.getMaximumVerticalScrollPosition());
                        }
                        if (isUpdate) {
                            resizeAbsolutePanel(); // also calls queryIcons
                        } else {
                            iconPanel.clear();
                            loadedIcons.clear();
                            queryIcons(scrollRatio, maxScrollPosition);
                        }
                    }
                }

            }


            void initialIconPopulation() {
                resizeAbsolutePanel();
                if (scrollHandler == null) {
                    scrollHandler = event -> deferIconRepeatingCommand();
                    scrollPanel.addScrollHandler(scrollHandler);
                }
            }


            @Override
            public boolean onError(Throwable t) {
                Display.error("IconPanel", 9, t);
                return false;
            }
        });
        try {
            if (_filter == null) {
                future.execute(IconActionsServiceProtocol.class).getIconInfo(queryText);
            } else {
                future.execute(IconActionsServiceProtocol.class).getIconInfo(_filter, queryText);
            }
        } catch (CentrifugeException e) {
            Display.error("IconPanel", 10, e);
            // error but
        }
    }

    private void showNoResultsIcons() {
        iconPanel.clear();
        scrollPanel.setVisible(false);
    }

    private void showIconPanel() {
        scrollPanel.setVisible(true);
//        if(noResultsIcons!=null){
//            parentPanel.remove(noResultsIcons);
//        }
    }

    private void setTagSelected(String tag) {
        int currItem = 0;
        while (tagsPanel.getWidgetCount() != currItem) {
            Label l = (Label) tagsPanel.getWidget(currItem);
            if (l.getText().equals(tag)) {
                l.setStyleName(ICON_TAG_SELECTED_STYLE);
            } else {
                l.setStyleName(ICON_TAG_STYLE);
            }
            // oops
            l.addStyleName("label");
            currItem++;
        }
    }

    private void deferIconRepeatingCommand() {
        double maxScrollPosition = (double) scrollPanel.getMaximumVerticalScrollPosition();
        double scrollRatio = 0;
        if (maxScrollPosition >= 0) {
            scrollRatio = ((double) scrollPanel.getVerticalScrollPosition()) / ((double) scrollPanel.getMaximumVerticalScrollPosition());
        }

        IconRepeatingCommand repeatingCommand = new IconRepeatingCommand(scrollRatio, maxScrollPosition) {
            @Override
            public boolean execute() {
                //killswitch
                if (cancelled) {
                    return false;
                }
                //We only allow latest command to go up
                //and only one at a time, this is due to serialization limits
                if (this == command && !isLocked()) {
                    // i guess this is if we have all of the icons...
                    if (iconCount == loadedIcons.size()) {
                        initializeScroll(true);
                    } else {
                        queryIcons(getScrollRatio(), getMaxScrollPosition());
                    }
                } else if (this == command) {
                    return true;
                }

                return false;
            }

        };
        Scheduler.get().scheduleFixedDelay(repeatingCommand, SCROLL_HANDLER_DELAY);

        command = repeatingCommand;
    }

    private boolean isLocked() {
        return locked;

    }

    private void setLocked(boolean locked) {
        this.locked = locked;
    }

    private void resizeAbsolutePanel() {
        //Give 15px buffer for scrollbars
        int windowWidth = scrollPanelWidth - 15;
        int windowHeight = currentWindowHeight - ADD_PANEL_HEIGHT - 15;

        iconsPerRow = (int) Math.floor(windowWidth / (IconContainer.ICON_WIDTH + ICON_HORIZONTAL_SPACING * 2));
        if (iconsPerRow == 0) {
            iconsPerRow = 1;
        }
        int rows = (int) Math.ceil((double) iconCount / (double) iconsPerRow);


        int trueHeight = rows * (IconContainer.ICON_HEIGHT + ICON_VERTICAL_SPACING) + ICON_VERTICAL_SPACING;

        iconPanel.setHeight(trueHeight + "px"); //$NON-NLS-1$

        double heightRatio = (double) windowHeight / (double) trueHeight;
        if (heightRatio > 1) {
            heightRatio = 1;
        }
        iconsPerPage = (int) Math.ceil(heightRatio * rows * iconsPerRow);
        double maxScrollPosition = (double) scrollPanel.getMaximumVerticalScrollPosition();
        double scrollRatio = 0;
        if (maxScrollPosition > 0) {
            scrollRatio = ((double) scrollPanel.getVerticalScrollPosition()) / ((double) scrollPanel.getMaximumVerticalScrollPosition());
        }
        queryIcons(scrollRatio, maxScrollPosition);
    }

    private void queryIcons(double scrollRatio, double maxScrollPosition) {

        int start = (int) Math.floor(iconCount * scrollRatio);
        //puts start at beginning of a row
        while (iconsPerRow != 0) {
            if (start % iconsPerRow != 0) {
                start--;
            } else {
                break;
            }
        }

        queryIcons(start);
    }

    private void queryIcons(int start) {
        setLocked(true);
        populateIcons(start, iconsPerPage, iconCount);
    }

    private void populateIcons(int startIndex, int iconPerPage, final int count) {

        //Grab ~3 pages
        int end = startIndex + iconPerPage + iconPerPage / 2;
        startIndex = startIndex - iconPerPage - iconPerPage / 2;

        if (startIndex < 0) {
            startIndex = 0;
        }

        if (end + iconsPerRow >= iconCount) {
            //only time we may not pull full row is at the end
            end = iconCount;
        } else {
            //puts end at beginning of a row
            while (iconsPerRow != 0) {
                if (end % iconsPerRow != 0) {
                    end++;
                } else {
                    break;
                }
            }
        }

        //skip over any icons we already have, until we meet our end page
        //this will not query if end is met.
        while (loadedIcons.keySet().contains(startIndex) && startIndex < end) {
            startIndex++;
        }


        if (startIndex != iconCount) {
            //puts start at beginning of a row
            while (iconsPerRow != 0) {
                if (startIndex % iconsPerRow != 0) {
                    startIndex--;
                } else {
                    break;
                }
            }
        }


        if (iconCount == end) {
            //probably should do something at end????
        }

        //Current bug with going to the middle of the page of a search, sad news
//        if(queryText != null){
//            for(Integer key :loadedIcons.keySet()){
//                if(key < startIndex){
//                    startIndex = key;
//                }
//            }
//        }

        final int start = startIndex;

        if (start == end) {
            setLocked(false);
            return; //No new icons needed
        }

        if (loadedIcons.get(end) != null && loadedIcons.get(start) != null) {
            setLocked(false);
            return; //No new icons needed
        }

        spinnerIcon.setVisible(true);

        VortexFuture<IconResultDto> future = WebMain.injector.getVortex().createFuture();

        future.addEventHandler(new AbstractVortexEventHandler<IconResultDto>() {
            @Override
            public void onSuccess(final IconResultDto result) {
                try {
                    setLocked(false);
                    if (result != null && result.getResults() != null)
                        displayIcons(result.getResults(), start);
                } catch (Exception e) {
                    Display.error("IconPanel", 11, e);
                } finally {
                    spinnerIcon.setVisible(false);
                }
            }

            @Override
            public boolean onError(Throwable t) {
                spinnerIcon.setVisible(false);
                setLocked(false);
                Display.error("IconPanel", 12, t);
                return false;
            }
        });

        if (_filter != null) {
            if (!_filter.getMatchPattern().isEmpty()) {
                _filter.setTestName(true);
            }
        }

        try {
            future.execute(IconActionsServiceProtocol.class).listIcons(start, end, queryText, iconTotalCount, null, _filter);
        } catch (CentrifugeException e) {
            Display.error("IconPanel", 13, e);
        }
    }

    private IconContainer createIconContainer(Icon icon, int pos) {
        IconContainer container = new IconContainer(icon, pos);
        String tooltip = "Name: " + icon.getName() + "\nTags: " + Joiner.on(",").join(icon.getTags());
        container.setTitle(tooltip);

        return container;
    }

    // switch to private
    private int displayIcons(List<Icon> result, int start) {
        int currentIconColumnIndex = 0;
        int topIndex = start / iconsPerRow;

        int newIconsFound = 0;
        for (Icon icon : result) {
            if (!loadedIcons.keySet().contains(start)) {
                IconContainer container = createIconContainer(icon, start);
                if (loadedIcons.containsValue(container)) {
                    continue;
                }
                iconPanel.add(container);
                container.loadImage();
                newIconsFound++;

                int left = ICON_HORIZONTAL_SPACING;
                left += ((ICON_HORIZONTAL_SPACING * 2 + IconContainer.ICON_WIDTH) * currentIconColumnIndex);

                int top = ICON_VERTICAL_SPACING;
                top += ((ICON_VERTICAL_SPACING + IconContainer.ICON_HEIGHT) * topIndex);

                container.setPosition(top, left);

                loadedIcons.put(start, container);
            }
            currentIconColumnIndex++;
            start++;
            if (currentIconColumnIndex >= iconsPerRow) {
                currentIconColumnIndex = 0;
                topIndex++;
            }

        }
        iconPanel.getElement().getStyle().setPosition(Position.ABSOLUTE);

        checkCurrentSpot(start, currentIconColumnIndex);

        return newIconsFound;

    }

    private void checkCurrentSpot(int end, int currentIconColumnIndex) {

        double maxScrollPosition = (double) scrollPanel.getMaximumVerticalScrollPosition();
        double scrollRatio = 0;
        if (maxScrollPosition >= 0) {
            scrollRatio = ((double) scrollPanel.getVerticalScrollPosition()) / ((double) scrollPanel.getMaximumVerticalScrollPosition());
        }

        int start = (int) Math.floor(iconCount * scrollRatio);


        boolean queryAgain = false;

        if (end - iconsPerPage <= start || end + iconsPerPage * 2 <= start + iconsPerPage) {
            queryAgain = true;
        }

        if (end > start) {
            start = end;
        }

        //puts start at beginning of a row
        while (iconsPerRow != 0) {
            if (start % iconsPerRow != 0) {
                start--;
            } else {
                break;
            }
        }

        final int newStart = start;

        IconRepeatingCommand repeatingCommand = new IconRepeatingCommand(scrollRatio, maxScrollPosition) {
            @Override
            public boolean execute() {
                if (cancelled) {
                    return false;
                }
                //We only allow latest command to go up
                //and only one at a time, this is due to serialization limits
                if (this == command && !isLocked()) {
                    if (iconCount == loadedIcons.size()) {
                        initializeScroll(true);
                    } else {
                        queryIcons(newStart);
                    }
                } else if (this == command) {
                    return true;
                }

                return false;
            }

        };

        if (queryAgain) {
            Scheduler.get().scheduleFixedDelay(repeatingCommand, SCROLL_HANDLER_DELAY);

            command = repeatingCommand;
        }
    }

    HandlerRegistration addIconSelectionHandler(IconSelectionHandler selectionHandler) {
        return handlerManager.addHandler(IconSelectionEvent.type, selectionHandler);
    }

    public Widget getWidget() {
        return parentPanel.asWidget();
    }

    public void setHeight(String htmlHeight) {
        scrollPanel.setHeight(htmlHeight);
        parentPanel.setHeight(htmlHeight);
        currentWindowHeight = parentPanel.getOffsetHeight();
        //TODO: adjust after change
    }

    public void setWidth(String htmlWidth) {
        scrollPanel.setWidth(htmlWidth);
        parentPanel.setWidth(htmlWidth);
        scrollPanelWidth = parentPanel.getOffsetWidth();
        //TODO: adjust after change
    }

    //Moves every icon after the one we deleted to the left
    private void fixIconLayout(int index) {
        while (index < loadedIcons.size()) {
            IconContainer container = loadedIcons.get(index);
//
//
//            int left = ICON_HORIZONTAL_SPACING;
//            left += ((ICON_HORIZONTAL_SPACING * 2 + IconContainer.ICON_WIDTH) * currentIconColumnIndex);

            //Move left one spot
            container.setPosition(container.getTop(), container.getLeft() - (ICON_HORIZONTAL_SPACING * 2 + IconContainer.ICON_WIDTH) - ICON_HORIZONTAL_SPACING / 2);

            if (container.getLeft() <= ICON_HORIZONTAL_SPACING) {
                //Move up and to the right-most if at left-most spot
                int left = ICON_HORIZONTAL_SPACING;
                left += ((ICON_HORIZONTAL_SPACING * 2 + IconContainer.ICON_WIDTH) * (iconsPerRow - 1));
                container.setPosition(container.getTop() - (ICON_VERTICAL_SPACING + IconContainer.ICON_HEIGHT), left);
            }
            index++;
        }
    }

    private void populateEditPane(final String iconUuid) {
        editPanel.clear();
        toggleLeftPanel(true);

        final IconEditPresenter iconEditPresenter = new IconEditPresenter();
        iconEditPresenter.setUuid(iconUuid);
        iconEditPresenter.setLoadHandler(new AbstractVortexEventHandler<String>() {

            @Override
            public void onSuccess(String result) {
                populateEditPane(iconUuid);
                IconContainer foundIcon = null;
                for (Integer key : loadedIcons.keySet()) {
                    if (loadedIcons.get(key).getIconUuid().equals(iconUuid)) {
                        foundIcon = loadedIcons.get(key);

                        if (currentlySelectedIcon != null && currentlySelectedIcon.getImagePreview().getUrl().length() == 0) {
                            currentlySelectedIcon.getImagePreview().setUrl(result);
                        }
                        selectIcon(foundIcon, false);


                        break;
                    }
                }
                if (foundIcon == null) {
                    Icon icon = new Icon();
                    icon.setUuid(iconUuid);
                    icon.setImage(result);

                    foundIcon = new IconContainer(icon, -100);

                    selectIcon(foundIcon, false);
                } else {
                    foundIcon.getImagePreview().setUrl(result);
                }
            }

            @Override
            public boolean onError(Throwable t) {
                spinnerIcon.setVisible(false);
                Display.error("IconPanel", 14, t);
                return false;
            }
        });
        final AbsolutePanel imageContainer = iconEditPresenter.getView(editAccess);
        Button closeButton = new Button(i18n.iconPanelCloseButton(), IconType.ARROW_LEFT); //$NON-NLS-1$
        closeButton.setType(ButtonType.LINK);

        closeButton.addClickHandler(event -> {
            toggleLeftPanel(false);
            clearAndReload();
        });

        editPanel.add(closeButton);

        addDeleteButton(iconUuid);

        editPanel.add(imageContainer);
        Label label = new Label(i18n.iconPanelNameTitle()); //$NON-NLS-1$
        label.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        label.setWidth("30px"); //$NON-NLS-1$

        editPanel.add(label);
        final TextBox nameTextBox = addNameTextBox(iconUuid);
        HorizontalPanel horizontalPanel = new HorizontalPanel();

        createTagComboBox(horizontalPanel);

        editPanel.add(horizontalPanel);
        imageContainer.addStyleName("csi-icon-preview"); //$NON-NLS-1$

        imageContainer.addStyleName("csi-icon-hover"); //$NON-NLS-1$
        imageContainer.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        imageContainer.getElement().getStyle().setPosition(Position.RELATIVE);
        imageContainer.setWidth(75 + "px"); //$NON-NLS-1$
        imageContainer.setHeight(75 + "px"); //$NON-NLS-1$


        VortexFuture<Icon> future = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<Icon>() {
            @Override
            public void onSuccess(final Icon result) {
                editPanel.setVisible(true);
                final Image image = new Image();
                image.setUrl(result.getImage());
                IconContainer foundIcon = null;
                for (Integer key : loadedIcons.keySet()) {
                    foundIcon = loadedIcons.get(key);
                    if (result.getUuid().equals(foundIcon.getIconUuid())) {
                        break;
                    } else {
                        foundIcon = null;
                    }
                }

                //Newly uploaded Icon
                if (foundIcon == null) {
                    Icon icon = new Icon();
                    icon.setUuid(iconUuid);
                    icon.setImage(result.getImage());
                    //We create an icon container just to use for selection
                    foundIcon = new IconContainer(icon, -100);
                }

                selectIcon(foundIcon, false);

                image.addStyleName("csi-icon-border"); //$NON-NLS-1$
                image.addStyleName("csi-icon-preview"); //$NON-NLS-1$

                image.getElement().getStyle().setMargin(0, Unit.PX);
                image.getElement().getStyle().setPadding(0, Unit.PX);
                image.getElement().getStyle().setBorderWidth(0, Unit.PX);
                image.setWidth("100%"); //$NON-NLS-1$
                image.setHeight("100%"); //$NON-NLS-1$

                nameTextBox.setText(result.getName());
                iconEditPresenter.attach(image, editAccess);
                if(!WebMain.getClientStartupInfo().getFeatureConfigGWT().isUseNewLogoutPage()) {
                    addIconUploadButton(imageContainer, image);
                }

                populateTags(result.getTags(), result.getUuid());
            }

            void addIconUploadButton(final AbsolutePanel imageContainer, final Image image) {
                if (editAccess) {
                    final Button changeButton = new Button();
                    imageContainer.add(changeButton);
                    imageContainer.addDomHandler(event -> {

                        changeButton.setIcon(IconType.EDIT);
                        changeButton.setType(ButtonType.SUCCESS);
                        changeButton.setSize(ButtonSize.LARGE);
                        Style buttonStyle = changeButton.getElement().getStyle();
                        buttonStyle.setFontSize(25.0D, Unit.PX);
                        buttonStyle.setTextDecoration(TextDecoration.NONE);
                        buttonStyle.setPadding(0, Unit.PX);
                        buttonStyle.setMargin(0, Unit.PX);
                        buttonStyle.setPosition(Position.ABSOLUTE);
                        buttonStyle.setLeft(30, Unit.PX);
                        buttonStyle.setTop(30, Unit.PX);
                        changeButton.setVisible(true);
                        image.getElement().getStyle().setOpacity(.75);
                    }, MouseOverEvent.getType());

                    imageContainer.addDomHandler(event -> {
                        changeButton.setVisible(false);

                        image.getElement().getStyle().setOpacity(1.0);
                    }, MouseOutEvent.getType());
                }
            }

            @Override
            public boolean onError(Throwable t) {
                Display.error("IconPanel", 15, t);
                return false;
            }
        });
        try {
            future.execute(IconActionsServiceProtocol.class).getIcon(iconUuid);
        } catch (CentrifugeException e) {

            Display.error("IconPanel", 16, e);
        }


    }

    /**
     * @param toggle // i guess we neeed a third panel to have a DELETE BUTTON
     */
    private void toggleLeftPanel(boolean toggle) {
        editPanel.setVisible(toggle);

        tagScrollPanel.setVisible(!toggle);

        if (tagFilterPanel != null) {
            tagFilterPanel.setVisible(!toggle);
            toggleExtraSpace(toggle);
        }
    }

    private void toggleExtraSpace(boolean toggle) {
        if (toggle) {
            parentPanel.setHeight((currentWindowHeight + extraSpace) + "px");
            editPanel.setHeight((currentWindowHeight + extraSpace) + "px");
            scrollPanel.setHeight((currentWindowHeight + extraSpace) + "px");
        } else {
            parentPanel.setHeight(currentWindowHeight + "px");
            editPanel.setHeight(currentWindowHeight + "px");
            scrollPanel.setHeight(currentWindowHeight + "px");
        }
    }

    private void createTagComboBox(HorizontalPanel horizontalPanel) {

        tagComboBox = createTagComboBox();
        horizontalPanel.add(tagComboBox);
        tagComboBox.setWidth("175px"); //$NON-NLS-1$

        if (editAccess) {
            Button addButton = new Button();
            addButton.setIcon(IconType.CIRCLE_ARROW_DOWN);
            addButton.setType(ButtonType.LINK);
            addButton.setSize(ButtonSize.LARGE);
            Style buttonStyle = addButton.getElement().getStyle();
            buttonStyle.setFontSize(25.0D, Unit.PX);
            buttonStyle.setTextDecoration(TextDecoration.NONE);
            buttonStyle.setPaddingLeft(0, Unit.PX);
            buttonStyle.setPaddingTop(0, Unit.PX);
            buttonStyle.setMarginBottom(4, Unit.PX);
            buttonStyle.setPosition(Position.ABSOLUTE);
            buttonStyle.setLeft(165, Unit.PX);

            addButton.addClickHandler(event -> {

                if (typedText != null && !typedText.isEmpty()) {
                    if (!tagsGrid.getStore().getAll().contains(typedText))
                        tagsGrid.getStore().add(typedText);

                    tagComboBox.setText(typedText);
                    if (!tags.contains(typedText)) {
                        tags.add(typedText);
                        Collections.sort(tags);
                    }
                }
            });
            horizontalPanel.add(addButton);

            Label tagLabel = new Label(i18n.iconPanelAvailableTitle()); //$NON-NLS-1$
            tagLabel.getElement().getStyle().setDisplay(Style.Display.BLOCK);
            tagLabel.setWidth("75px"); //$NON-NLS-1$
            editPanel.add(tagLabel);
        }
    }

    private TextBox addNameTextBox(String iconUuid) {
        final TextBox nameTextBox = new TextBox();
        nameTextBox.setWidth("150px"); //$NON-NLS-1$
        if (!editAccess) {
            nameTextBox.setEnabled(false);
        } else {
            nameTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {

                private CancelRepeatingCommand command = null;

                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    String text = event.getValue();
                    if (command != null) {
                        command.cancel();
                    }
                    command = new CancelRepeatingCommand(text, iconUuid);

                    Scheduler.get().scheduleFixedDelay(command, 2000);


                }
            });

        }

        editPanel.add(nameTextBox);
        return nameTextBox;
    }

    private void addDeleteButton(final String iconUuid) {
        if (editAccess) {
            Button deleteButton = new Button(i18n.iconPanelDeleteButton(), IconType.TRASH); //$NON-NLS-1$
            deleteButton.setType(ButtonType.LINK);

            deleteButton.addClickHandler(event -> {

                toggleLeftPanel(false);
                spinnerIcon.setVisible(true);
                VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
                future.addEventHandler(new AbstractVortexEventHandler<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        int index;
                        IconContainer foundIcon;
                        for (Integer key : loadedIcons.keySet()) {
                            if (loadedIcons.get(key).getIconUuid().equals(iconUuid)) {
                                foundIcon = loadedIcons.get(key);

                                currentlySelectedIcon = null;
                                loadedIcons.remove(key);
                                foundIcon.removeFromParent();
                                index = key;
                                index++;
                                while (index < loadedIcons.size()) {
                                    foundIcon = loadedIcons.get(index);
                                    foundIcon.setIndex(index - 1);
                                    loadedIcons.put(index - 1, foundIcon);
                                    index++;
                                }
                                iconCount--;
                                iconTotalCount--;
                                fixIconLayout(key);
                                break;
                            }
                        }

                        spinnerIcon.setVisible(false);
                    }


                    @Override
                    public boolean onError(Throwable t) {

                        spinnerIcon.setVisible(false);
                        Display.error("IconPanel", 17, t);
                        return false;
                    }
                });
                try {
                    future.execute(IconActionsServiceProtocol.class).deleteIcon(iconUuid);
                } catch (CentrifugeException e) {
                    //TODO: delete failed, probably should do more than this
                    iconCount++;
                    iconTotalCount++;
                    spinnerIcon.setVisible(false);
                    Display.error("IconPanel", 18, e);
                }
            });


            editPanel.add(deleteButton);
        }
    }

    private StringComboBox createEmptyStringCombo() {
        StringComboBox stringComboBox = new StringComboBox();
        stringComboBox.setForceSelection(false);
        stringComboBox.setValidateOnBlur(false);
        stringComboBox.setClearValueOnParseError(false);
        stringComboBox.setAutoValidate(false);
        stringComboBox.setEditable(true);
        stringComboBox.setAllowTextSelection(true);
        stringComboBox.setReadOnly(false);
        stringComboBox.setVisible(editAccess);
        return stringComboBox;
    }

    private StringComboBox createTagComboBox() {

        StringComboBox stringComboBox = createEmptyStringCombo();

        stringComboBox.addKeyUpHandler(event -> {
            typedText = tagComboBox.getText();
            if (KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
                if (!tagsGrid.getStore().getAll().contains(typedText))
                    tagsGrid.getStore().add(typedText);

                tagComboBox.setText(typedText);
            }
        });

        stringComboBox.addBlurHandler(event -> tagComboBox.setText(typedText));

        stringComboBox.addSelectionHandler(event -> typedText = event.getSelectedItem());


        return stringComboBox;
    }

    private void populateTags(final Set<String> iconTags, final String iconUuid) {
        //clear store
        tagComboBox.getStore().clear();
        //clear text field
        tagComboBox.clear();

        //repopulate
        if (tags == null) {
            queryForTags(iconTags);
        } else {
            ArrayList<String> result = new ArrayList<>(tags);
            result.removeAll(iconTags);
            tagComboBox.getStore().addAll(result);
        }

        List<ColumnConfig<String, ?>> columns = new ArrayList<>();
        ColumnConfig<String, String> nameColumn = new ColumnConfig<>(new ValueProvider<String, String>() {

            @Override
            public String getValue(String object) {
                return object;
            }

            @Override
            public void setValue(String object, String value) {

                tagsGrid.getStore().add(value);
            }

            @Override
            public String getPath() {
                return "toString"; //$NON-NLS-1$
            }
        });
        nameColumn.setHeader(i18n.iconPanelCurrentTitle()); //$NON-NLS-1$
        nameColumn.setWidth(150);
        columns.add(nameColumn);

        final ColumnConfig<String, Void> deleteColumn = new ColumnConfig<>(new ValueProvider<String, Void>() {

            @Override
            public Void getValue(String object) {
                return null;
            }

            @Override
            public String getPath() {
                return ""; //$NON-NLS-1$
            }

            @Override
            public void setValue(String object, Void value) {

            }
        });
        if (editAccess) {
            IconCell deleteCell = new IconCell(IconType.REMOVE);
            deleteCell.setTooltip(i18n.kmlExportDialogdeleteTooltip()); //$NON-NLS-1$
            deleteColumn.setCell(deleteCell);
            deleteColumn.setWidth(20);
            columns.add(deleteColumn);
        }
        tagsGrid = new ResizeableGrid<>(new ListStore<>(item -> item), new ColumnModel<>(columns));

        tagsGrid.getStore().addAll(iconTags);

        tagsGrid.addCellClickHandler(event -> {
            if (!editAccess) {
                return;
            }
            int rowIndex = event.getRowIndex();
            int cellIndex = event.getCellIndex();
            ListStore<String> store = tagsGrid.getStore();
            int delColIndex = tagsGrid.getColumnModel().indexOf(deleteColumn);
            if (cellIndex == delColIndex) {
                //Triggers removeHandler
                String value = store.remove(rowIndex);
            }
        });
        tagsGrid.getStore().addStoreAddHandler(event -> {
            VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
            future.addEventHandler(new AbstractVortexEventHandler<Void>() {
                @Override
                public void onSuccess(Void object) {
                    updateTagsPanel();
                }

                @Override
                public boolean onError(Throwable t) {
                    Display.error("IconPanel", 19, t);
                    return false;
                }
            });
            try {
                future.execute(IconActionsServiceProtocol.class).addTag(iconUuid, event.getItems());
            } catch (CentrifugeException e) {

                Display.error("IconPanel", 20, e);
            }
        });
        tagsGrid.getStore().addStoreRemoveHandler(event -> {
            VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
            future.addEventHandler(new AbstractVortexEventHandler<Void>() {
                @Override
                public void onSuccess(Void object) {
                    updateTagsPanel();
                }

                @Override
                public boolean onError(Throwable t) {
                    Display.error("IconPanel", 21, t);
                    return false;
                }
            });
            try {
                future.execute(IconActionsServiceProtocol.class).removeTag(iconUuid, event.getItem());
            } catch (CentrifugeException e) {

                Display.error("IconPanel", 22, e);
            }
        });
        //tagsGrid.setWidth("170px");
        gridContainer = new GridContainer();
        gridContainer.setGrid(tagsGrid);
        gridContainer.setWidth("188px"); //$NON-NLS-1$
        gridContainer.setHeight("235px"); //$NON-NLS-1$
        editPanel.add(gridContainer);


    }

    private void queryForTags(final Set<String> iconTags) {
        VortexFuture<List<String>> future = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<List<String>>() {
            @Override
            public void onSuccess(final List<String> result) {
                tags = new ArrayList<>(result);
                if (tagComboBox != null) {
                    result.removeAll(iconTags);
                    tagComboBox.getStore().addAll(result);
                }

                Collections.sort(tags);
                updateTagsPanel();
            }

            @Override
            public boolean onError(Throwable t) {
                Display.error("IconPanel", 23, t);
                return false;
            }
        });
        try {
            future.execute(IconActionsServiceProtocol.class).listAvailableTags();
        } catch (CentrifugeException e) {

            Display.error("IconPanel", 24, e);
        }
    }

    private void updateTagsPanel() {
        // 2 is header, and all tag.
        if (tags.size() + 2 == tagsPanel.getWidgetCount()) {

        } else {
            tagsPanel.clear();

            final Label allLabel = new Label(i18n.iconPanelAllTagsTag()); //$NON-NLS-1$
            allLabel.addStyleName(ICON_TAG_STYLE);
            //todo
            allLabel.setTitle(i18n.iconPanelAllTags());
            allLabel.getElement().getStyle().setDisplay(Style.Display.BLOCK);
            tagsPanel.add(allLabel);

            allLabel.addClickHandler(event -> {
                if (command != null) {
                    command.setCancelled(true);
                }
                setTagSelected(Element.as(event.getNativeEvent().getEventTarget()).getInnerText());

                if (queryText != null) {
                    queryText = null;
                }
                clearAndReload();
            });

            Collections.sort(tags);

            for (final String tag : tags) {
                Label tagLabel = new Label(tag);
                tagLabel.setTitle(tag);
                tagLabel.addStyleName(ICON_TAG_STYLE);
                tagLabel.getElement().getStyle().setDisplay(Style.Display.BLOCK);
                tagsPanel.add(tagLabel);

                tagLabel.addClickHandler(event -> {
//                    _filter = null;
                    setLocked(true);
                    if (command != null) {
                        command.setCancelled(true);
                    }
                    setTagSelected(Element.as(event.getNativeEvent().getEventTarget()).getInnerText());
                    if (queryText == null || !queryText.equals(tag)) {
                        queryText = tag;
                    }
                    clearAndReload();
                });
            }

        }
    }

    private void clearFilter() {
        _filter = null;
        if (iconFilter != null) {
            iconFilter.setText("");
        }
    }

    public void clear() {
        iconPanel.clear();
    }

    private void setupFilterPanel() {
        tagFilterPanel = new AbsolutePanel();
        // todo - set as the edit panel as well
        tagFilterPanel.setWidth(currentWindowWidth + "px");
        tagFilterPanel.getElement().getStyle().setOverflow(Overflow.VISIBLE);
    }

    Widget getTagFilter() {

        if (tagFilterPanel == null) {
            setupFilterPanel();
        }

        FlowPanel p = new FlowPanel();
        InlineHTML noResultsLBL = new InlineHTML();
        noResultsLBL.getElement().getStyle().setFontSize(1, Unit.EM);
        noResultsLBL.setText("No Results");
        noResultsLBL.setVisible(false);


        TextBox tagFilter = new TextBox();
        tagFilter.setHeight("20px");
        tagFilter.getElement().getStyle().setMargin(1, Unit.PX);
        tagFilter.setWidth(180 + "px");

        tagFilter.setPlaceholder(i18n.iconPanelTagFilter());


        tagFilter.addChangeHandler(event -> {
            String filterText = tagFilter.getText();
            int hidden = 0;
            int visible = 0;
            int currItem = 0;
            while (tagsPanel.getWidgetCount() != currItem) {
                Label l = (Label) tagsPanel.getWidget(currItem);
                if (!filterText.isEmpty()) {
                    if (!l.getText().toLowerCase().contains(filterText.toLowerCase())) {
                        l.setVisible(false);
                        hidden++;

                    } else {
                        l.setVisible(true);
                        visible++;
                    }
                } else {
                    l.setVisible(true);
                    visible++;
                }
                currItem++;
            }

            if (visible == 0) {
                noResultsLBL.setText("No tags found for " + tagFilter.getText());
                noResultsLBL.setVisible(true);
            } else {
                noResultsLBL.setText("Showing: " + visible + "/" + (visible + hidden));
                noResultsLBL.setVisible(false);
            }
        });

        p.add(tagFilter);

        p.setHeight(40 + "px");
        p.getElement().getStyle().setFloat(Style.Float.LEFT);
        p.getElement().getStyle().setWidth(currentWindowWidth - scrollPanelWidth, Unit.PX);
        p.add(noResultsLBL);

        tagFilterPanel.add(p);
        return tagFilterPanel;
    }

    private Widget addNewIconPnl() {
        addPanel.add(presenter.getView());
        addPanel.getElement().getStyle().setFloat(Style.Float.RIGHT);
        return addPanel;
    }

    Widget getIconFilter() {
        FlowPanel p = new FlowPanel();


        if (tagFilterPanel == null) {
            setupFilterPanel();
        }

        iconFilter = new TextBox();

        iconFilter.setHeight("20px");
        iconFilter.getElement().getStyle().setMarginBottom(5, Unit.PX);
        iconFilter.getElement().getStyle().setMargin(1, Unit.PX);
        iconFilter.setPlaceholder(i18n.iconPanelIconFilter());
        iconFilter.setWidth(150 + "px");

        InputAddOn bg = new InputAddOn();
//        bg.setHeight(30+"px");
        bg.getElement().getStyle().setMarginTop(1, Unit.PX);
        bg.setTitle(i18n.iconPanelWildcard());
        bg.setAppendIcon(IconType.INFO_SIGN);
        bg.add(iconFilter);

        p.getElement().getStyle().setMarginLeft(currentWindowWidth - scrollPanelWidth + 14, Unit.PX);
        p.setHeight(30 + "px");

        p.add(bg);


        Label clearFilter = new Label();
        clearFilter.setText(i18n.iconPanelClearButton());
        clearFilter.getElement().getStyle().setMarginLeft(5, Unit.PX);

        clearFilter.addClickHandler(event -> {
            clearFilter();
            clearAndReload();
        });

        // icon filter change
        iconFilter.addKeyUpHandler(event -> {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                if (_filterEditDialog != null) {
                    _filter = _filterEditDialog.getFilter() == null ? new ResourceFilter() : _filterEditDialog.getFilter();
                } else {
                    _filter = new ResourceFilter();
                }

                if (iconFilter.getText().isEmpty()) {
                    _filter = null;
                } else {
                    String text = iconFilter.getText();
                    // check if we have star or ? and if we do - dont add anything, user is smart
                    if (!iconFilter.getText().contains("*") && !iconFilter.getText().contains("?")) {
                        text = "*" + iconFilter.getText() + "*";
                    }
                    _filter.setMatchDisplayPattern(text);
                    _filter.setTestName(true);
                }

//                initializeScroll(false);
                clearAndReload();
            }
        });

        p.add(addNewIconPnl());
        p.add(createAdvancedTab());
        p.add(clearFilter);
        tagFilterPanel.add(p);

        return tagFilterPanel;
    }

    private ResourceFilterListBox createAdvancedTab() {

        filterDropDown = new ResourceFilterListBox();

        filterDropDown.setWidth(150);
        filterDropDown.getElement().getStyle().setPaddingLeft(5, Unit.PX);

        filterDropDown.addSelectionChangedHandler(selectionChangedEvent -> {
            if (filterDropDown.getSelectedText().equals(filterDropDown.getAdhocFilter())) {
                // this will get called after we are done with the filter creation - take the filter, and reload.
                ClickHandler doneHandle = event -> {
                    _filter = _filterEditDialog.getFilter();
                    clearAndReload();
                };
                // todo feed this the cancel which will remove the filter
                _filterEditDialog = new ResourceFilterDialog(null, doneHandle, null, null, null);
                _filterEditDialog.disableACL();
                _filterEditDialog.show();

            } else {
                _filter = filterDropDown.getSelectedItem();
                clearAndReload();
            }
        });

        return filterDropDown;
    }

    /**
     * clears the icon count, the loaded icons, and the filter drop down list
     */
    private void clearAndReload() {
        iconCount = 0;
        loadedIcons.clear();
        setLocked(true);
        scrollPanel.setVerticalScrollPosition(0);

        initializeScroll(true);
    }


    public class CancelRepeatingCommand implements RepeatingCommand {

        private boolean cancel = false;
        private String text;
        private String iconUuid;

        public CancelRepeatingCommand(String text, String iconUuid) {
            this.text = text;
            this.iconUuid = iconUuid;
        }

        @Override
        public boolean execute() {
            if (cancel) {
                return false;
            }
            VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
            future.execute(IconActionsServiceProtocol.class).editName(text, iconUuid);
            return false;
        }

        public void cancel() {
            cancel = true;
        }
    }
}
