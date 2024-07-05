/*
 *  Copyright 2012 GWT-Bootstrap
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package csi.client.gwt.widget.boot;

import java.util.HashSet;
import java.util.Set;

import com.github.gwtbootstrap.client.ui.Close;
import com.github.gwtbootstrap.client.ui.ModalFooter;
import com.github.gwtbootstrap.client.ui.base.DivWidget;
import com.github.gwtbootstrap.client.ui.base.HasVisibility;
import com.github.gwtbootstrap.client.ui.base.IsAnimated;
import com.github.gwtbootstrap.client.ui.constants.BackdropType;
import com.github.gwtbootstrap.client.ui.constants.Constants;
import com.github.gwtbootstrap.client.ui.constants.DismissType;
import com.github.gwtbootstrap.client.ui.event.HasVisibleHandlers;
import com.github.gwtbootstrap.client.ui.event.HiddenEvent;
import com.github.gwtbootstrap.client.ui.event.HiddenHandler;
import com.github.gwtbootstrap.client.ui.event.HideEvent;
import com.github.gwtbootstrap.client.ui.event.HideHandler;
import com.github.gwtbootstrap.client.ui.event.ShowEvent;
import com.github.gwtbootstrap.client.ui.event.ShowHandler;
import com.github.gwtbootstrap.client.ui.event.ShownEvent;
import com.github.gwtbootstrap.client.ui.event.ShownHandler;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;

/**
 * NOTE: The GWT-Bootstrap code has been duplicated to allow addition to the header. The bootstrap code declares header
 * to be private thus obviating extension.
 * Customizations:
 * 1. addToHeader(Widget) method
 * 2. Replace all FQCN in JSNI references in this class with correct package name.
 */
//@formatter:off

/**
* Popup dialog with optional header and {@link ModalFooter footer.}
* <p>
* By default, all other Modals are closed once a new one is opened. This
* setting can be {@link #setHideOthers(boolean) overridden.}
* <p/>
* <p>
* <h3>UiBinder Usage:</h3>
* <p/>
* <pre>
* {@code
* <b:Modal title="My Modal" backdrop="STATIC">
*     <g:Label>Modal Content!</g:Label>
*     <b:ModalFooter>
*         <b:Button icon="FILE">Save</b:Button>
*     </b:ModalFooter>
* </b:Modal>
* }
* </pre>
* <p/>
* All arguments are optional.
* </p>
*
* @author Carlos Alexandro Becker
* @author Dominik Mayer
* @author Danilo Reinert
*
* @see <a
*      href="http://twitter.github.com/bootstrap/javascript.html#modals">Bootstrap
*      documentation</a>
* @see PopupPanel
*
* @since 2.0.4.0
*/
//@formatter:on
public class Modal extends DivWidget implements HasVisibility, HasVisibleHandlers, IsAnimated {

  private static final String TOGGLE = "toggle";

private static final String HIDE = "hide";

private static final String SHOW = "show";

private static Set<Modal> currentlyShown = new HashSet<Modal>();

  private final DivWidget header = new DivWidget();

  private final DivWidget body = new DivWidget("modal-body"); //$NON-NLS-1$

  private boolean keyboard = true;

  private BackdropType backdropType = BackdropType.NORMAL;

  private boolean show = false;

  private boolean hideOthers = true;

  private boolean configured = false;

  private Close close = new Close(DismissType.MODAL);

  private String title;
  

  protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();


    public static CsiHeading createHeading(String stringIn) {

        return new CsiHeading(3, stringIn);
    }

    public ContextMenuHandler contextMenuCancel = new ContextMenuHandler() {

        public void onContextMenu(ContextMenuEvent eventIn) {

            // stop the browser from opening the context menu
            eventIn.preventDefault();
            eventIn.stopPropagation();
        }
    };


  /**
   * Creates an empty, hidden widget.
   */
  public Modal() {
      super("modal"); //$NON-NLS-1$
      getElement().setAttribute("tabindex", "-1"); //$NON-NLS-1$ //$NON-NLS-2$
      super.add(header);
      super.add(body);
      addDomHandler(contextMenuCancel, ContextMenuEvent.getType());
      setVisible(false);
  }

  /**
   * Creates an empty, hidden widget with specified show behavior.
   *
   * @param animated <code>true</code> if the widget should be animated.
   */
  public Modal(boolean animated) {
      this(animated, false);
  }

  /**
   * Creates an empty, hidden widget with specified show behavior.
   *
   * @param animated    <code>true</code> if the widget should be animated.
   * @param dynamicSafe <code>true</code> removes from RootPanel when hidden
   */
  public Modal(boolean animated,
               boolean dynamicSafe) {
      this();
      setAnimation(animated);
      setDynamicSafe(dynamicSafe);
  }

  /**
   * Setup the modal to prevent memory leaks. When modal is hidden, will
   * remove all event handlers, and them remove the modal DOM from document
   * DOM.
   * <p/>
   * Default is false.
   *
   * @param dynamicSafe
   */
  public void setDynamicSafe(boolean dynamicSafe) {
      if (dynamicSafe) {
          addHiddenHandler(new HiddenHandler() {

              @Override
              public void onHidden(HiddenEvent hiddenEvent) {
                  unsetHandlerFunctions(getElement());
                  Modal.this.removeFromParent();
              }
          });
      }
  }

  /**
   * CSI addition.
   * @param widget Widget add to header.
   */
  public void addToHeader(Widget widget) {
      header.add(widget);
      showHeader(true);
  }
  
  /**
   * Sets the title of the Modal.
   *
   * @param title the title of the Modal
   */
  @Override
  public void setTitle(String title) {
      this.title = title;

      header.clear();
      if (title == null || title.isEmpty()) {
          showHeader(false);
      } else {

          header.add(close);
          header.add(createHeading(title));
          showHeader(true);
      }
  }

  /**
   * Gets the title of the Modal.
   *
   * @return title
   */
  public String getTitle() {
      return title;
  }

  private void showHeader(boolean show) {
      if (show)
          header.setStyleName(Constants.MODAL_HEADER);
      else
          header.removeStyleName(Constants.MODAL_HEADER);
  }

  /**
   * {@inheritDoc}
   */
  public void setAnimation(boolean animated) {
      if (animated)
          addStyleName(Constants.FADE);
      else
          removeStyleName(Constants.FADE);
  }

  /**
   * {@inheritDoc}
   */
  public boolean getAnimation() {
      return getStyleName().contains(Constants.FADE);
  }

  /**
   * Sets whether this Modal appears on top of others or is the only one
   * visible on screen.
   *
   * @param hideOthers <code>true</code> to make sure that this modal is the only one
   *                   shown. All others will be hidden. Default: <code>true</code>
   */
  public void setHideOthers(boolean hideOthers) {
      this.hideOthers = hideOthers;
  }

  /**
   * Sets whether the Modal is closed when the <code>ESC</code> is pressed.
   *
   * @param keyboard <code>true</code> if the Modal is closed by <code>ESC</code>
   *                 key. Default: <code>true</code>
   */
  public void setKeyboard(boolean keyboard) {
      this.keyboard = keyboard;
      reconfigure();
  }

  /**
   * Get Keyboard enable state
   *
   * @return true:enable false:disable
   */
  public boolean isKeyboardEnable() {
      return this.keyboard;
  }

  /**
   * Sets the type of the backdrop.
   *
   * @param type the backdrop type
   */
  public void setBackdrop(BackdropType type) {
      backdropType = type;
      reconfigure();

  }

  /**
   * Get backdrop type.
   *
   * @return backdrop type.
   */
  public BackdropType getBackdropType() {
      return this.backdropType;
  }

  /**
   * Reconfigures the modal with changed settings.
   */
  protected void reconfigure() {
      if (configured) {
          reconfigure(keyboard, backdropType, show);
      }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(Widget w) {
      if (w instanceof ModalFooter) {
          super.add(w);
      } else
          body.add(w);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void insert(Widget w, int beforeIndex) {
      body.insert(w, beforeIndex);
  }

  /**
   * {@inheritDoc}
   */
  public void show() {
      checkAttachedOnShow();
      changeVisibility(SHOW); //$NON-NLS-1$
      centerVertically(getElement());  

  }

  /**
   * {@inheritDoc}
   */
  public void show(boolean autoShow) {
      checkAttachedOnShow();
      changeVisibility(SHOW, autoShow); //$NON-NLS-1$
      centerVertically(getElement()); 
  }

  private void checkAttachedOnShow() {
      if (!this.isAttached()) {
          RootPanel.get().add(this);
      }
  }

  @Override
  protected void onAttach() {
      super.onAttach();
      configure(keyboard, backdropType, show);
      setHandlerFunctions(getElement());
      configured = true;
  }

  /**
   * {@inheritDoc}
   */
  public void hide() {
      changeVisibility(HIDE); //$NON-NLS-1$
  }

  /**
   * {@inheritDoc}
   */
  public void hide(boolean autoHidden) {
      changeVisibility(HIDE, autoHidden); //$NON-NLS-1$
  }

  /**
   * {@inheritDoc}
   */
  public void toggle() {
      changeVisibility(TOGGLE); //$NON-NLS-1$
  }

  /**
   * {@inheritDoc}
   */
  public void toggle(boolean autoToggle) {
      changeVisibility(TOGGLE, autoToggle); //$NON-NLS-1$
  }

  private void changeVisibility(String visibility) {
      changeVisibility(getElement(), visibility);
  }

  private void changeVisibility(String visibility, boolean autoTriggered) {
      changeVisibility(getElement(), visibility, autoTriggered);
  }

  /**
   * This method is called immediately when the widget's {@link #hide()}
   * method is executed.
   */
  protected void onHide(Event e) {
      fireEvent(new HideEvent(e, getAutoTriggered(e)));
  }

  /**
   * This method is called once the widget is completely hidden.
   */
  protected void onHidden(Event e) {
      fireEvent(new HiddenEvent(e, getAutoTriggered(e)));
      currentlyShown.remove(this);
  }

  /**
   * This method is called immediately when the widget's {@link #show()}
   * method is executed.
   */
  protected void onShow(Event e) {
      if (hideOthers)
          hideShownModals();
      fireEvent(new ShowEvent(e, getAutoTriggered(e)));
  }

  private void hideShownModals() {
      for (Modal m : currentlyShown) {
          if (!m.equals(this)) {
              m.hide(true);
          }
      }
  }

  /**
   * This method is called once the widget is completely shown.
   */
  protected void onShown(Event e) {
      fireEvent(new ShownEvent(e, getAutoTriggered(e)));
      currentlyShown.add(this);
  }

  private void reconfigure(boolean keyboard, BackdropType backdropType, boolean show) {
      if (backdropType == BackdropType.NORMAL) {
          reconfigure(getElement(), keyboard, true, show);
      } else if (backdropType == BackdropType.NONE) {
          reconfigure(getElement(), keyboard, false, show);
      } else if (backdropType == BackdropType.STATIC) {
          reconfigure(getElement(), keyboard, BackdropType.STATIC.get(), show);
      }
  }

  private void configure(boolean keyboard, BackdropType backdropType, boolean show) {
      if (backdropType == BackdropType.NORMAL) {
          configure(getElement(), keyboard, true, show);
      } else if (backdropType == BackdropType.NONE) {
          configure(getElement(), keyboard, false, show);
      } else if (backdropType == BackdropType.STATIC) {
          configure(getElement(), keyboard, BackdropType.STATIC.get(), show);
      }
  }

  //@formatter:off

  private native void reconfigure(Element e, boolean k, boolean b, boolean s) /*-{
      // Init vars
      var $e = $wnd.jQuery(e);
      var modal = $e.data('modal');
      var wasShown = null;

      // If element is modal, then unset it
      if (modal) {
          $e.removeData('modal');
          wasShown = modal.isShown;
      }

      // Apply modal again to the element
      $e.modal({
          keyboard: k,
          backdrop: b,
          show: s
      });

      // If previous modal was shown, then reset it to current modal
      if (wasShown) {
          $e.data('modal').isShown = wasShown;
      }
  }-*/;

  private native void reconfigure(Element e, boolean k, String b, boolean s) /*-{
      // Init vars
      var $e = $wnd.jQuery(e);
      var modal = $e.data('modal');
      var wasShown = null;

      // If element is modal, then unset it
      if (modal) {
          $e.removeData('modal');
          wasShown = modal.isShown;
      }

      // Apply modal again to the element
      $e.modal({
          keyboard: k,
          backdrop: b,
          show: s
      });

      // If previous modal was shown, then reset it to current modal
      if (wasShown) {
          $e.data('modal').isShown = wasShown;
      }
  }-*/;


  private native void configure(Element e, boolean k, boolean b, boolean s) /*-{
      $wnd.jQuery(e).modal({
          keyboard: k,
          backdrop: b,
          show: s
      });
  }-*/;

  private native void configure(Element e, boolean k, String b, boolean s) /*-{
      $wnd.jQuery(e).modal({
          keyboard: k,
          backdrop: b,
          show: s
      });
  }-*/;

  private native void changeVisibility(Element e, String visibility) /*-{
      $wnd.jQuery(e).modal(visibility);
  }-*/;

  private native void changeVisibility(Element e, String visibility, boolean autoTriggered) /*-{
      var $e = $wnd.jQuery(e);

      var modal = $e.data('modal');
      if (modal)
          modal.autoTriggered = autoTriggered;

      $e.modal(visibility);
  }-*/;

  private native boolean getAutoTriggered(JavaScriptObject jso) /*-{
      // Prevent null result
      if (jso.autoTriggered) return true;
      return false;
  }-*/;

  /**
   * Links the Java functions that fire the events.
   */
  private native void setHandlerFunctions(Element element) /*-{
      var that = this;
      var $el = $wnd.jQuery(element);
      var autoTriggeredCheck = function (event, removeProperty) {
          var modal = $wnd.jQuery(event.target).data('modal');
          if (modal && modal.autoTriggered) {
              event.autoTriggered = true;
              if (removeProperty)
                  modal.autoTriggered = false;
          }
      };

      $el.on('hide', function (e) {
          if (e.target === this) {
              autoTriggeredCheck(e);
              that.@csi.client.gwt.widget.boot.Modal::onHide(Lcom/google/gwt/user/client/Event;)(e);
              e.stopPropagation();
          }
      });
      $el.on('hidden', function (e) {
          if (e.target === this) {
              autoTriggeredCheck(e, true);
              that.@csi.client.gwt.widget.boot.Modal::onHidden(Lcom/google/gwt/user/client/Event;)(e);
              e.stopPropagation();
          }
      });
      $el.on('show', function (e) {
          if (e.target === this) {
              autoTriggeredCheck(e);
              that.@csi.client.gwt.widget.boot.Modal::onShow(Lcom/google/gwt/user/client/Event;)(e);
              e.stopPropagation();
          }
      });
      $el.on('shown', function (e) {
          if (e.target === this) {
              autoTriggeredCheck(e, true);
              that.@csi.client.gwt.widget.boot.Modal::onShown(Lcom/google/gwt/user/client/Event;)(e);
              e.stopPropagation();
          }
      });
  }-*/;

  /**
   * Unlinks all the Java functions that fire the events.
   */
  private native void unsetHandlerFunctions(Element e) /*-{
      var $e = $wnd.jQuery(e);
      $e.off('hide');
      $e.off('hidden');
      $e.off('show');
      $e.off('shown');
  }-*/;
  //@formatter:on

  /**
   * {@inheritDoc}
   */
  public HandlerRegistration addHideHandler(HideHandler handler) {
      return addHandler(handler, HideEvent.getType());
  }

  /**
   * {@inheritDoc}
   */
  public HandlerRegistration addHiddenHandler(HiddenHandler handler) {
      return addHandler(handler, HiddenEvent.getType());
  }

  /**
   * {@inheritDoc}
   */
  public HandlerRegistration addShowHandler(ShowHandler handler) {
      return addHandler(handler, ShowEvent.getType());
  }

  /**
   * {@inheritDoc}
   */
  public HandlerRegistration addShownHandler(ShownHandler handler) {
      return addHandler(handler, ShownEvent.getType());
  }

  /**
   * Show/Hide close button. The Modal must have a title.
   *
   * @param visible <b>true</b> for show and <b>false</b> to hide. Defaults is
   *                <b>true</b>.
   */
  public void setCloseVisible(boolean visible) {
      close.getElement().getStyle().setVisibility(visible
              ? Style.Visibility.VISIBLE
              : Style.Visibility.HIDDEN);
  }

  /**
   * @deprecated modal do not support setSize method
   */
  @Override
  public void setSize(String width, String height) {
      throw new UnsupportedOperationException(i18n.modalSizeException()); //$NON-NLS-1$
  }

  /**
   * Sets the Modal's width.
   *
   * @param width Modal's new width, in px
   */
  public void setWidth(int width) {
      DOM.setStyleAttribute(this.getElement(), "width", width + "px"); //$NON-NLS-1$ //$NON-NLS-2$
      DOM.setStyleAttribute(this.getElement(), "marginLeft", (-width / 2) + "px"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Sets the Modal's body maxHeight.
   *
   * @param maxHeight the Modal's body new maxHeight, in CSS units (e.g. "10px", "1em")
   */
  public void setMaxHeigth(String maxHeight) {
      DOM.setStyleAttribute(body.getElement(), "maxHeight", maxHeight); //$NON-NLS-1$
  }

  /**
   * Centers fixed positioned element vertically.
   * Backported from upstream commit 0b396d3cb1e61289fb3328d94d02299d26fbfd3b
   * https://github.com/gonzohunter/gwt-bootstrap/commit/0b396d3cb1e61289fb3328d94d02299d26fbfd3b
   * @param e Element to center vertically
   */
  private native void centerVertically(Element e) /*-{
  $wnd.jQuery(e).css("margin-top", (-1 * $wnd.jQuery(e).outerHeight() / 2) + "px");
+    $wnd.jQuery(e).css("top", "50%");
}-*/;

}
