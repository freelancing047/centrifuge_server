package csi.client.gwt.worksheet.layout.window;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.XDOM;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.AccessStack;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.event.RegisterEvent;
import com.sencha.gxt.widget.core.client.event.RegisterEvent.HasRegisterHandlers;
import com.sencha.gxt.widget.core.client.event.RegisterEvent.RegisterHandler;
import com.sencha.gxt.widget.core.client.event.UnregisterEvent;
import com.sencha.gxt.widget.core.client.event.UnregisterEvent.HasUnregisterHandlers;
import com.sencha.gxt.widget.core.client.event.UnregisterEvent.UnregisterHandler;

/**
 * NOTE: CSI CHANGES -
 * This class has been copied from Sencha code-base (WindowManager) AS-IS with the following modifications:
 * -start modification list-
 * 
 * 
 * -end modification list-
 * 
 * An object that represents a group of {@link Widget} instances and provides
 * z-order management and window activation behavior. By default, WindowManager
 * only provides these features for the {@link WindowBase} class, as {@code Widget}s
 * don't have a concept of activation or moving to the front.
 * 
 * Can be extended and replaced to support non-{@code Window} classes - the main
 * extension points will be the {@link #activateLast()} and {@link #setActiveWin(Widget)}
 * methods, which should be made general enough for the specific use case. Then, 
 * the global {@code WindowManager} can be replaced with the subclass in the project
 * module file:
 * <pre><code>
&lt;replace-with class="com.example.project.client.ReplacementWindowManager"&gt;
    &lt;when-type-is class="com.sencha.gxt.widget.core.client.WindowManager" /&gt;
&lt;/replace-with&gt;
 */
public class WindowBaseManager implements HasRegisterHandlers<Widget>, HasUnregisterHandlers<Widget> {

  private static WindowBaseManager instance;

  /**
   * Returns the singleton instance.
   * 
   * @return the window manager
   */
  public static WindowBaseManager get() {
    if (instance == null) instance = GWT.create(WindowBaseManager.class);
    return instance;
  }

  private AccessStack<Widget> accessList;
  private Widget front;
  private List<Widget> widgets;
  private SimpleEventBus eventBus;

  /**
   * Creates a window manager.
   */
  public WindowBaseManager() {
    accessList = new AccessStack<Widget>();
    widgets = new ArrayList<Widget>();
  }

  /**
   * Activates the next window in the access stack.
   * 
   * @param widget the reference window
   * @return true if the next window exists
   */
  public boolean activateNext(Widget widget) {
    int count = widgets.size();
    if (count > 1) {
      int idx = widgets.indexOf(widget);
      if (idx == count - 1) {
        return false;
      }
      setActiveWin(widgets.get(++idx));
      return true;
    }
    return false;
  }

  /**
   * Activates the previous widget in the access stack.
   * 
   * @param widget the reference window
   * @return true if a previous window exists
   */
  public boolean activatePrevious(Widget widget) {
    int count = widgets.size();
    if (count > 1) {
      int idx = widgets.indexOf(widget);
      if (idx == 0) {
        return false;
      }
      setActiveWin(widgets.get(--idx));
    }
    return false;
  }

  @Override
  public HandlerRegistration addRegisterHandler(RegisterHandler<Widget> handler) {
    return ensureHandlers().addHandler(RegisterEvent.getType(), handler);
  }

  @Override
  public HandlerRegistration addUnregisterHandler(UnregisterHandler<Widget> handler) {
    return ensureHandlers().addHandler(UnregisterEvent.getType(), handler);
  }

  /**
   * Brings the specified widget to the front of any other active widgets.
   * {@link WindowBase} instances will automatically call this method when focused,
   * all other types must call manually.
   * 
   * @param widget the window
   * @return true if the widget was brought to the front, else false if it was
   *         already in front
   */
  public boolean bringToFront(Widget widget) {
    if (widget != front) {
      accessList.add(widget);
      activateLast();
      return true;
    } else {
      focus(widget);
    }
    return false;
  }

  /**
   * Gets the currently-active widget in the group.
   * 
   * @return the active window
   */
  public Widget getActive() {
    return front;
  }

  /**
   * Returns the ordered widgets.
   * 
   * @return the widgets
   */
  public Stack<Widget> getStack() {
    return accessList.getStack();
  }

  /**
   * Returns the visible widgets.
   * 
   * @return the widgets
   */
  public List<Widget> getWindows() {
    return widgets;
  }

  /**
   * Hides all widgets that are registered to this WindowManager.
   */
  public void hideAll() {
    for (int i = accessList.size() - 1; i >= 0; --i) {
      accessList.get(i).setVisible(false);
    }
  }

  /**
   * Registers the window with the WindowManager. {@link WindowBase} instances will
   * automatically register and unregister themselves. All other types must
   * register / unregister manually.
   * 
   * @param widget the window
   */
  public void register(Widget widget) {
    accessList.add(widget);
    widgets.add(widget);
    ensureHandlers().fireEvent(new RegisterEvent<Widget>(widget));
  }

  /**
   * Sends the specified window to the back of other active widgets.
   * 
   * @param widget the widget
   */
  public void sendToBack(Widget widget) {
    accessList.getStack().pop();
    accessList.getStack().insertElementAt(widget, 0);
    activateLast();
  }

  /**
   * Unregisters the window with the WindowManager.
   * 
   * @param widget the widget
   */
  public void unregister(Widget widget) {
    if (front == widget) {
      front = null;
    }
    accessList.remove(widget);
    widgets.remove(widget);
    activateLast();
    ensureHandlers().fireEvent(new UnregisterEvent<Widget>(widget));
  }

  SimpleEventBus ensureHandlers() {
    return eventBus == null ? eventBus = new SimpleEventBus() : eventBus;
  }

  /**
   * Activates the top window in the stack. Protected so it can be overridden to activate
   * more than just GXT Window objects.
   */
  protected void activateLast() {
    for (int i = accessList.size() - 1; i >= 0; --i) {
      Widget last = accessList.get(i);
      if (last instanceof WindowBase) {
        WindowBase w = (WindowBase) last;
        if (w.isVisible()) {
          setActiveWin(w);
          return;
        }
      }
    }
    setActiveWin(null);
  }

  protected void focus(Widget widget) {
    if (widget instanceof Component) {
      ((Component) widget).focus();
    } else {
      widget.getElement().focus();
    }
  }

  /**
   * Indicates that this window should be made active. Protected so it can be overridden
   * to provide specialized behavior when more than GXT Windows are used.
   * @param window
   */
  protected void setActiveWin(Widget window) {
    if (window != front) {
      if (front != null) {
        if (window instanceof WindowBase) {
          ((WindowBase) front).setActive(false);
        }
      }
      front = window;
      if (window != null) {
        if (window instanceof WindowBase) {
          WindowBase w = (WindowBase) window;
          w.setActive(true);
          w.setZIndex(XDOM.getTopZIndex(10));
        } else {
          window.getElement().<XElement> cast().setZIndex(XDOM.getTopZIndex(10));
        }

        focus(window);
      }
    }
  }
}
