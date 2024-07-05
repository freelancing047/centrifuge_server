package csi.client.gwt.viz.table;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiConstructor;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.gestures.TapGestureRecognizer;
import com.sencha.gxt.core.client.gestures.TouchData;
import com.sencha.gxt.core.client.gestures.TouchEventToGestureAdapter;
import com.sencha.gxt.core.client.resources.CommonStyles;
import com.sencha.gxt.core.client.util.Util;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.messages.client.DefaultMessages;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.LabelToolItem;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar.PagingToolBarAppearance;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar.PagingToolBarMessages;
import com.sencha.gxt.widget.core.client.toolbar.SeparatorToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class ScrollingToolBar extends ToolBar{
   
    protected static class DefaultPagingToolBarMessages implements PagingToolBarMessages {

        @Override
        public String afterPageText(int page) {
          return DefaultMessages.getMessages().pagingToolBar_afterPageText(page);
        }

        @Override
        public String beforePageText() {
          return DefaultMessages.getMessages().pagingToolBar_beforePageText();
        }

        @Override
        public String displayMessage(int start, int end, int total) {
          return DefaultMessages.getMessages().pagingToolBar_displayMsg(start, end, total);
        }

        @Override
        public String emptyMessage() {
          return DefaultMessages.getMessages().pagingToolBar_emptyMsg();
        }

        @Override
        public String firstText() {
          return DefaultMessages.getMessages().pagingToolBar_firstText();
        }

        @Override
        public String lastText() {
          return DefaultMessages.getMessages().pagingToolBar_lastText();
        }

        @Override
        public String nextText() {
          return DefaultMessages.getMessages().pagingToolBar_nextText();
        }

        @Override
        public String prevText() {
          return DefaultMessages.getMessages().pagingToolBar_prevText();
        }

        @Override
        public String refreshText() {
          return DefaultMessages.getMessages().pagingToolBar_refreshText();
        }

      }

      protected int activePage = -1, pages;
      protected LabelToolItem beforePage, afterText, displayText;
      protected PagingLoadConfig config;
      protected TextButton first, prev, next, last;
      protected PagingLoader<PagingLoadConfig, ?> loader;
      protected TextBox pageText;
      protected XElement scroller;

      protected boolean showToolTips = true;
      protected int start, pageSize, totalLength;
      private final PagingToolBarAppearance appearance;
      private boolean loading;
      private boolean buttonsEnabled;
      // flag to track if refresh was clicked since setIcon will steal focus. If it was focused, we must refocus after icon change
      private boolean activeRefresh = false;

      
      private HandlerRegistration handlerRegistration;
      private PagingToolBarMessages messages;
      private boolean reuseConfig = true;
    private PageEventHandler pageEventHandler;

      /**
       * Creates a new paging tool bar.
       * 
       * @param pageSize the page size
       */
      @UiConstructor
      public ScrollingToolBar(int pageSize) {
        this(GWT.<ToolBarAppearance> create(ToolBarAppearance.class),
            GWT.<PagingToolBarAppearance> create(PagingToolBarAppearance.class), pageSize);
      }

      /**
       * Creates a new tool bar.
       * 
       * @param toolBarAppearance the tool bar appearance
       * @param appearance the paging tool bar appearance
       * @param pageSize the page size
       */
      public ScrollingToolBar(ToolBarAppearance toolBarAppearance, PagingToolBarAppearance appearance, int pageSize) {
        super(toolBarAppearance);
        this.appearance = appearance;
        this.pageSize = pageSize;

        addStyleName("x-paging-toolbar-mark");

        first = new TextButton();
        first.setIcon(appearance.first());
        first.addSelectHandler(new SelectHandler() {

          @Override
          public void onSelect(SelectEvent event) {
            first();
          }
        });

        prev = new TextButton();
        prev.setIcon(appearance.prev());
        prev.addSelectHandler(new SelectHandler() {

          @Override
          public void onSelect(SelectEvent event) {
            previous();
          }
        });

        next = new TextButton();
        next.setIcon(appearance.next());
        next.addSelectHandler(new SelectHandler() {

          @Override
          public void onSelect(SelectEvent event) {
            next();
          }
        });

        last = new TextButton();
        last.setIcon(appearance.last());
        last.addSelectHandler(new SelectHandler() {

          @Override
          public void onSelect(SelectEvent event) {
            last();
          }
        });

//        refresh = new TextButton();
//        refresh.setIcon(appearance.refresh());
//        refresh.addSelectHandler(new SelectHandler() {
//
//          @Override
//          public void onSelect(SelectEvent event) {
//            refresh();
//          }
//        });

        beforePage = new LabelToolItem();
        beforePage.setLabel(getMessages().beforePageText());

        afterText = new LabelToolItem();

        pageText = new TextBox();
        pageText.setWidth("30px");
        pageText.addKeyDownHandler(new KeyDownHandler() {
          public void onKeyDown(KeyDownEvent event) {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
              onPageChange();
            }
          }
        });
        new TouchEventToGestureAdapter(pageText, new TapGestureRecognizer() {
          @Override
          protected void onTap(TouchData touchData) {
            super.onTap(touchData);
            pageText.setFocus(true);
          }
        });

        displayText = new LabelToolItem();
        clear();
        displayText.addStyleName(CommonStyles.get().nowrap());

        addToolTips();

        add(first);
        add(prev);
        add(new SeparatorToolItem());
        add(beforePage);
        add(pageText);
        add(afterText);
        add(new SeparatorToolItem());
        add(next);
        add(last);
        add(new SeparatorToolItem());
        add(new FillToolItem());
        add(displayText);
      }

      /**
       * Binds the toolbar to the loader.
       * 
       * @param loader the loader
       */
      @SuppressWarnings({"unchecked", "rawtypes"})
      public void bind(PagingLoader<? extends PagingLoadConfig, ?> loader) {
        if (this.loader != null) {
          handlerRegistration.removeHandler();
        }
        this.loader = (PagingLoader) loader;
        if (loader != null) {
          loader.setLimit(pageSize);
        }
      }

      /**
       * Clears the current toolbar text.
       */
      public void clear() {
        pageText.setText("");
        afterText.setLabel("");
        displayText.setLabel("");
      }

      /**
       * Called when a load request is initialed and called after completion of the load request. Subclasses may override as
       * needed.
       * 
       * @param enabled the enabled state
       */
      protected void doEnableButtons(boolean enabled) {
        buttonsEnabled = enabled;
        first.setEnabled(enabled);
        prev.setEnabled(enabled);
        beforePage.setEnabled(enabled);
        pageText.setEnabled(enabled);
        afterText.setEnabled(enabled);
        next.setEnabled(enabled);
        last.setEnabled(enabled);
        displayText.setEnabled(enabled);
      }

      /**
       * Moves to the first page.
       */
      public void first() {
        if (!loading) {
            PageEvent event = new PageEvent();
            event.setActivePage(1);
            pageEventHandler.onPage(event);
        }
      }

      /**
       * Returns the active page.
       * 
       * @return the active page
       */
      public int getActivePage() {
        return activePage;
      }

      /**
       * Returns the toolbar appearance.
       * 
       * @return the appearance
       */
      public PagingToolBarAppearance getPagingToolbarAppearance() {
        return appearance;
      }

      /**
       * Returns the toolbar messages.
       * 
       * @return the messages
       */
      public PagingToolBarMessages getMessages() {
        if (messages == null) {
          messages = new DefaultPagingToolBarMessages();
        }
        return messages;
      }

      /**
       * Returns the current page size.
       * 
       * @return the page size
       */
      public int getPageSize() {
        return pageSize;
      }

      /**
       * Returns the total number of pages.
       * 
       * @return the total pages
       */
      public int getTotalPages() {
        return pages;
      }

      /**
       * Returns true if the paging toolbar buttons are enabled.
       * 
       * @return the buttons enabled.
       */
      public boolean isButtonsEnabled() {
        return buttonsEnabled;
      }

      /**
       * Returns true if the previous load config is reused.
       * 
       * @return the reuse config state
       */
      public boolean isReuseConfig() {
        return reuseConfig;
      }

      /**
       * Returns true if tooltip are enabled.
       * 
       * @return the show tooltip state
       */
      public boolean isShowToolTips() {
        return showToolTips;
      }

      /**
       * Moves to the last page.
       */
      public void last() {
        if (!loading) {
          if (pages > 1) {
              PageEvent event = new PageEvent();
              event.setActivePage(pages);
              pageEventHandler.onPage(event);
          }
        }
      }

      /**
       * Moves to the last page.
       */
      public void next() {
        if (!loading) {
          PageEvent event = new PageEvent();
          event.setActivePage(activePage+1);
          pageEventHandler.onPage(event);
        }
      }

      /**
       * Moves the the previous page.
       */
      public void previous() {
        if (!loading) {
            PageEvent event = new PageEvent();
            event.setActivePage(Math.max(1, activePage - 1));
            pageEventHandler.onPage(event);
        }
      }

      /**
       * Refreshes the data using the current configuration.
       */
      public void refresh() {
        if (!loading) {
          activeRefresh = true;
          doLoadRequest(start, pageSize);
        }
      }

      /**
       * Sets the active page (1 to page count inclusive).
       * 
       * @param page the page
       */
      public void setActivePage(int page) {
        if (page > pages) {
          last();
          return;
        }
        if (page != activePage && page > 0 && page <= pages) {
          doLoadRequest(--page * pageSize, pageSize);
        } else {
          pageText.setText(String.valueOf((int) activePage));
        }
      }

      /**
       * Sets the toolbar messages.
       * 
       * @param messages the messages
       */
      public void setMessages(PagingToolBarMessages messages) {
        this.messages = messages;
      }

      /**
       * Sets the current page size. This method does not effect the data currently being displayed. The new page size will
       * not be used until the next load request.
       * 
       * @param pageSize the new page size
       */
      public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
      }

      /**
       * True to reuse the previous load config (defaults to true).
       * 
       * @param reuseConfig true to reuse the load config
       */
      public void setReuseConfig(boolean reuseConfig) {
        this.reuseConfig = reuseConfig;
      }

      /**
       * Sets if the button tool tips should be displayed (defaults to true, pre-render).
       * 
       * @param showToolTips true to show tool tips
       */
      public void setShowToolTips(boolean showToolTips) {
        this.showToolTips = showToolTips;
        if (showToolTips) {
          addToolTips();
        } else {
          removeToolTips();
        }
      }

      protected void doLoadRequest(int offset, int limit) {
        if (reuseConfig && config != null) {
          config.setOffset(offset);
          config.setLimit(pageSize);
          loader.load(config);
        } else {
          loader.load();
        }
      }
      
      /**
       * 
       * Configures the paging toolbar to show the correct info
       * 
       * @param currentPosition
       * @param fullHeight
       * @param pageHeight
       * @param totalLength
       * @param rowHeight
       * @param isEnd
       */
      public void updateInfo(double currentPosition, double fullHeight, double pageHeight, int totalLength, double rowHeight, boolean isEnd){
          this.pages = (int)(Math.ceil(fullHeight/pageHeight));
          if(isEnd){
              activePage = this.pages;
          } else {
              
              activePage = (int) (currentPosition/fullHeight * pages) + 1;
          }

          pageText.setText(String.valueOf((int) activePage));

          String after = null, display = null;
          after = getMessages().afterPageText(pages);
          afterText.setLabel(after);

          first.setEnabled(activePage != 1);
          prev.setEnabled(activePage != 1);
          next.setEnabled(activePage != pages);
          last.setEnabled(activePage != pages);

          start = (int) (currentPosition/rowHeight);
          pageSize = (int) (pageHeight/rowHeight);
          
          if(start >= 0) {

              int temp = activePage == pages ? totalLength : start + pageSize;

              display = getMessages().displayMessage(start + 1, (int) temp, totalLength);

              String msg = display;
              if (totalLength == 0) {
                msg = "";
              }
              displayText.setLabel(msg);
          } else {
              displayText.setLabel("");
          }
          

          forceLayout();
      }

      protected void onLoad(LoadEvent<PagingLoadConfig, PagingLoadResult<?>> event) {
//        loading = false;
//        config = event.getLoadConfig();
//        PagingLoadResult<?> result = event.getLoadResult();
//        start = result.getOffset();
//        totalLength = result.getTotalLength();
//        activePage = (int) Math.ceil((double) (start + pageSize) / pageSize);
//
//        pages = totalLength < pageSize ? 1 : (int) Math.ceil((double) totalLength / pageSize);
//
//        if (activePage > pages && totalLength > 0) {
//          last();
//          return;
//        } else if (activePage > pages) {
//          start = 0;
//          activePage = 1;
//        }
//
//        pageText.setText(String.valueOf((int) activePage));
//
//        String after = null, display = null;
//        after = getMessages().afterPageText(pages);
//        afterText.setLabel(after);
//
//        first.setEnabled(activePage != 1);
//        prev.setEnabled(activePage != 1);
//        next.setEnabled(activePage != pages);
//        last.setEnabled(activePage != pages);
//
//        int temp = activePage == pages ? totalLength : start + pageSize;
//
//        display = getMessages().displayMessage(start + 1, (int) temp, totalLength);
//
//        String msg = display;
//        if (totalLength == 0) {
//          msg = getMessages().emptyMessage();
//        }
//        displayText.setLabel(msg);
//
//        forceLayout();
      }

      protected void onPageChange() {
        String value = pageText.getText();
        if (value.equals("") || !Util.isInteger(value)) {
          pageText.setText(String.valueOf((int) activePage));
          return;
        }
        int p = Integer.parseInt(value);
        if(p < 0){
            p = 1;
        }
        if(p > pages){
            p = pages;
        }
        
        pageText.setText(String.valueOf(p));
        
        PageEvent event = new PageEvent();
        event.setActivePage(p);
        pageEventHandler.onPage(event);
      }

      /**
       * Helper method to apply the tool tip messages to built-in toolbar buttons. Additional tooltips can be set by
       * overriding {@link #setShowToolTips(boolean)}.
       */
      private void addToolTips() {
        PagingToolBarMessages m = getMessages();
        first.setToolTip(m.firstText());
        prev.setToolTip(m.prevText());
        next.setToolTip(m.nextText());
        last.setToolTip(m.lastText());
      }

      /**
       * Helper method to remove the tool tip messages from built-in toolbar buttons. Additional tooltips can be set by
       * overriding {@link #setShowToolTips(boolean)}.
       */
      private void removeToolTips() {
        first.removeToolTip();
        prev.removeToolTip();
        next.removeToolTip();
        last.removeToolTip();
      }

    public void setScroller(XElement scroller) {
        this.scroller = scroller;
    }

    public void addPageHandler(PageEventHandler pageEventHandler, Type<PageEventHandler> type) {
        this.pageEventHandler = pageEventHandler;
    }
    
}
