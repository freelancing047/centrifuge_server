package csi.client.gwt.util;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.sencha.gxt.widget.core.client.grid.Grid;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.WatchBox;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.ErrorDialog;

/**
 * Created by centrifuge on 2/28/2017.
 */
public class Display {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private static Integer _counter = 0;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public static void showWatchBox(String messageIn) {

        WatchBox myWatchBox = WatchBox.getInstance();

        myWatchBox.show(messageIn);
    }

    public static void showWatchBox(String titleIn, String messageIn) {

        WatchBox myWatchBox = WatchBox.getInstance();

        myWatchBox.show(titleIn, messageIn);
    }

    public static void cancelWatchBox() {

        WatchBox myWatchBox = WatchBox.getInstance();

        myWatchBox.hide();
    }

    public static void debug(final String messageIn) {

        DeferredCommand.add(new Command() {
            public void execute() {
                Dialog.showInfo("DEBUG " + (++_counter).toString(), messageIn);
            }
        });
    }

    public static void continueDialog(final String titleIn, final String messageIn,
                                      ClickHandler continueIn, ClickHandler cancelIn) {

        DeferredCommand.add(new Command() {
            public void execute() {
                Dialog.showContinueDialog(titleIn, messageIn, continueIn, cancelIn);
            }
        });
    }

    public static void continueDialog(final String titleIn, final String messageIn, ClickHandler continueIn) {

        DeferredCommand.add(new Command() {
            public void execute() {
                Dialog.showContinueDialog(titleIn, messageIn, continueIn, false);
            }
        });
    }

    public static void continueDialog(final String titleIn, final String messageIn,
                                      ClickHandler continueIn, boolean hideCancelIn) {

        DeferredCommand.add(new Command() {
            public void execute() {
                Dialog.showContinueDialog(titleIn, messageIn, continueIn, hideCancelIn);
            }
        });
    }

    public static void continueDialog(final String titleIn, final String messageIn,
                                      ClickHandler continueIn, String actionTextIn, String cancelTextIn) {

        DeferredCommand.add(new Command() {
            public void execute() {
                Dialog.showContinueDialog(titleIn, messageIn, continueIn, actionTextIn, cancelTextIn);
            }
        });
    }

    public static void warning(final String titleIn, final String messageIn) {

        DeferredCommand.add(new Command() {
            public void execute() {
                Dialog.showWarning(titleIn, messageIn);
            }
        });
    }


    public static void warning(final String titleIn, final String messageIn, ClickHandler continueHandlerIn) {

        DeferredCommand.add(new Command() {
            public void execute() { Dialog.showContinueDialog(titleIn, messageIn, continueHandlerIn, true); }
        });
    }

    public static void success(final String messageIn) {

        WatchBox.getInstance().hide();
        Dialog.showInfo(_constants.successfulMessageTitle(), messageIn);
    }

    public static void success(final String titleIn, final String messageIn) {

        WatchBox.getInstance().hide();
        Dialog.showInfo(titleIn, messageIn);
    }

    public static void success(final String titleIn, final String messageIn, ClickHandler handlerIn) {

        WatchBox.getInstance().hide();
        Dialog.showInfo(titleIn, messageIn, handlerIn);
    }

    public static void error(final String messageIn) {

        WatchBox.getInstance().hide();
        DeferredCommand.add(new Command() {
            public void execute() {
                Dialog.showError(messageIn);
            }
        });
    }

    public static void error(final String titleIn, final String errorIn, final boolean stripFirstLineIn) {

        WatchBox.getInstance().hide();
        DeferredCommand.add(new Command() {
            public void execute() {
                (new ErrorDialog(titleIn, errorIn, stripFirstLineIn)).show();
            }
        });
    }

    public static void error(final String titleIn, final String errorIn, final boolean stripFirstLineIn,
                             final boolean isOnlyWarningIn, final boolean expandIn) {

        WatchBox.getInstance().hide();
        DeferredCommand.add(new Command() {
            public void execute() {
                (new ErrorDialog(titleIn, errorIn, stripFirstLineIn, isOnlyWarningIn, expandIn)).show();
            }
        });
    }

    public static void error(final String titleIn, final String errorIn, final CanBeShownParent parentIn) {

        WatchBox.getInstance().hide();
        DeferredCommand.add(new Command() {
            public void execute() {
                (new ErrorDialog(titleIn, errorIn, parentIn)).show();
            }
        });
    }

    public static void error(final String titleIn, final String errorIn, final ClickHandler handlerIn) {

        WatchBox.getInstance().hide();
        DeferredCommand.add(new Command() {
            public void execute() {
                (new ErrorDialog(titleIn, errorIn, handlerIn)).show();
            }
        });
    }

    public static void error(final String titleIn, final String errorIn,
                             final boolean stripFirstLineIn, final ClickHandler handlerIn) {

        WatchBox.getInstance().hide();
        DeferredCommand.add(new Command() {
            public void execute() {
                (new ErrorDialog(titleIn, errorIn, stripFirstLineIn, handlerIn)).show();
            }
        });
    }

    public static void error(final String errorIn, final boolean stripFirstLineIn, final ClickHandler handlerIn) {

        WatchBox.getInstance().hide();
        DeferredCommand.add(new Command() {
            public void execute() {
                (new ErrorDialog(errorIn, stripFirstLineIn, handlerIn)).show();
            }
        });
    }

    public static void error(final String titleIn, final String messageIn) {

        WatchBox.getInstance().hide();
        DeferredCommand.add(new Command() {
            public void execute() {
                Dialog.showError(titleIn, messageIn);
            }
        });
    }

    public static void error(final String titleIn, final Throwable exceptionIn) {

        WatchBox.getInstance().hide();
        DeferredCommand.add(new Command() {
            public void execute() {
                Dialog.showException(titleIn, exceptionIn);
            }
        });
    }

    public static void error(final String titleIn, final Throwable exceptionIn, final ClickHandler handlerIn) {

        WatchBox.getInstance().hide();
        DeferredCommand.add(new Command() {
            public void execute() {
                Dialog.showException(titleIn, exceptionIn, handlerIn);
            }
        });
    }

    public static void error(final String titleIn, int idIn, final Throwable exceptionIn) {

        WatchBox.getInstance().hide();
        DeferredCommand.add(new Command() {
            public void execute() {
                Dialog.showException(titleIn, idIn, exceptionIn);
            }
        });
    }

    public static void error(final Throwable exceptionIn) {

        WatchBox.getInstance().hide();
        DeferredCommand.add(new Command() {
            public void execute() {
                Dialog.showException(exceptionIn);
            }
        });
    }

    public static void grid(final Grid gridIn, final boolean refreshHeaderIn) {

        DeferredCommand.add(new Command() {
            public void execute() {
                gridIn.getView().refresh(refreshHeaderIn);
            }
        });
    }
}
