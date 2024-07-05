package csi.client.gwt;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import com.github.gwtbootstrap.client.ui.resources.JavaScriptInjector;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.widget.core.client.container.Viewport;

import csi.client.gwt.error.ModalErrorHandler;
import csi.client.gwt.etc.ApplicationInjector;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.i18n.ConstantFunctionImpl;
import csi.client.gwt.i18n.EnumMaps;
import csi.client.gwt.util.Display;
import csi.client.gwt.viz.map.settings.MapConfigProxy;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.vortex.VortexServiceProvider;
import csi.server.common.dto.ClientStartupInfo;
import csi.server.common.dto.system.ReleaseInfo;
import csi.server.common.service.api.InternationalizationServiceProtocol;
import csi.server.common.service.api.SysinfoActionsServiceProtocol;
import csi.shared.core.Constants;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class WebMain implements EntryPoint {

    public static final ApplicationInjector injector = GWT.create(ApplicationInjector.class);
    private static ClientStartupInfo clientStartupInfo;
    private static Frame downloadFrame;

    private Viewport viewport;

    public static Frame getDownloadFrame() {
        return downloadFrame;
    }

    public static ClientStartupInfo getClientStartupInfo() {
        return clientStartupInfo;
    }

    @Override
    public void onModuleLoad() {
        // Refer to http://stackoverflow.com/questions/3028521/ to see why uncaught exception handler is set and
        // module is loaded in a deferred call.
        GWT.setUncaughtExceptionHandler(new ModalErrorHandler());
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                main();
            }
        });
    }

    private void main() {
        initialize();
    }

    private void fetchClientConfigurationPriorToStartupKickoff() {
        VortexFuture<ClientStartupInfo> sysInfoVF = WebMain.injector.getVortex().createFuture();
        sysInfoVF.addEventHandler(new AbstractVortexEventHandler<ClientStartupInfo>() {

            @Override
            public void onSuccess(ClientStartupInfo result) {
                checkNotNull(result);
                clientStartupInfo = result;
                ReleaseInfo.initialize(clientStartupInfo.getReleaseVersion(), clientStartupInfo.getBuildNumber());
                RootPanel.get().add(viewport);
                WebMain.injector.getEventBus().fireEvent(new ApplicationStartEvent(viewport));
            }
        });
        sysInfoVF.execute(SysinfoActionsServiceProtocol.class).startupInfo();
    }
    
    private void fetchMapConfigurationPriorToStartupKickoff() {
    	MapConfigProxy.initialize();
    }

    private void addDownloadFrame() {
        downloadFrame = buildDownloadFrame();
        RootPanel.get().add(downloadFrame);
    }

    private Frame buildDownloadFrame() {
        Frame frame = new Frame();
        frame.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        frame.getElement().getStyle().setLeft(-1000, Style.Unit.PX);
        frame.getElement().getStyle().setTop(-1000, Style.Unit.PX);
        return frame;
    }

    public static native String getLanguage() /*-{
    	var nav = window.navigator,
    	browserLanguagePropertyKeys = ['language', 'browserLanguage', 'systemLanguage', 'userLanguage'],
    	i,
    	language;

    	// support for HTML 5.1 "navigator.languages"
    	if (Array.isArray(nav.languages)) {
      		for (i = 0; i < nav.languages.length; i++) {
        		language = nav.languages[i];
        		if (language) {
        			language = language.trim();
        			if (language.length > 0) {
          				return language;
        			}
        		}
      		}
    	}

    	// support for other well known properties in browsers
    	for (i = 0; i < browserLanguagePropertyKeys.length; i++) {
      		language = nav[browserLanguagePropertyKeys[i]];
      		if (language) {
	      		language = language.trim();
	      		if (language.length > 0) {
	        		return language;
	      		}
      		}
    	}

    	return null;
    }-*/;

    /**
     * High level initializations (infrastructure setup).
     */
    private void initialize() {
        injector.getResources().style().ensureInjected();
        // Refer to javadoc of bootstrapModalFix!
        JavaScriptInjector.inject(injector.getResources().bootstrapModalFix().getText());
        // Inject highcharts. Note: Highcharts will not work if the javascript is included before jQuery. jQuery is
        // loaded by the bootstrap resource injector (see Bootstrap.java) as part of the load of the bootstrap module
        // which will happen before this is called. So we load highcharts here once jQuery is already loaded.
        JavaScriptInjector.inject(injector.getResources().highchartsCore().getText());
        JavaScriptInjector.inject(injector.getResources().highchartsMore().getText());
        JavaScriptInjector.inject(injector.getResources().highchartsExport().getText());

        JavaScriptInjector.inject(injector.getResources().jqueryUi().getText());
        JavaScriptInjector.inject(injector.getResources().tagIt().getText());

        // Use GWT.getModuleBaseURL() if you want to use the module name in the RPC url.
        ((VortexServiceProvider) injector.getVortex())
                .setRPCServiceEntryPointURL(Constants.UIConstants.URL_RPC_ENDPOINT);

        VortexFuture<Map<String, String>> vortexFuture = WebMain.injector.getVortex().createFuture();
		vortexFuture.execute(InternationalizationServiceProtocol.class).getProperties(getLanguage());
		vortexFuture.addEventHandler(new AbstractVortexEventHandler<Map<String, String>>() {
			@Override
			public void onSuccess(Map<String, String> properties) {
				CentrifugeConstants centrifugeConstants = GWT.create(CentrifugeConstants.class);
				centrifugeConstants.initialize(properties);
				CentrifugeConstantsLocator.set(centrifugeConstants);
				injector.getMainPresenter().initialize();
		        addDownloadFrame();
		        viewport = new Viewport();
		        fetchClientConfigurationPriorToStartupKickoff();
		        fetchMapConfigurationPriorToStartupKickoff();

                if (ConstantFunctionImpl.hasErrors()) {

                    Display.error(centrifugeConstants.i18nErrorTitle(),
                            centrifugeConstants.i18nErrorMessage() + ConstantFunctionImpl.getFailures(), true);
                }
                EnumMaps.buildAllMaps();
			}
		});
    }

}
