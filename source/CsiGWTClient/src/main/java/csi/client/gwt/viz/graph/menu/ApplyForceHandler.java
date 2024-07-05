package csi.client.gwt.viz.graph.menu;

import java.util.List;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.github.gwtbootstrap.client.ui.Controls;
import com.github.gwtbootstrap.client.ui.Fieldset;
import com.github.gwtbootstrap.client.ui.Form;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.FormType;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.CsiMap;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.shared.core.visualization.graph.GraphLayout;

public class ApplyForceHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {

    public ApplyForceHandler(Graph presenter, GraphMenuManager menuManager) {
        super(presenter, menuManager);
    }


    String lastGoodValue = "25";
    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        if (getPresenter().isViewLoaded()) {
    		Graph presenter = getPresenter();
    		String moreIterations = presenter.getModel().getRelGraphViewDef().getSettings().getPropertiesMap().get("moreIterations");

    		if (Strings.isNullOrEmpty(moreIterations)) {
    			final Dialog dialog = new Dialog();
    			final TextBox iterationsTextBox = new TextBox();
    			{

    				dialog.hideOnCancel();
    				dialog.hideOnAction();
    				Form form = new Form();
    				dialog.add(form);
    				form.setType(FormType.HORIZONTAL);

    				Fieldset fieldset = new Fieldset();
    				form.add(fieldset);

    				ControlGroup controlGroup = new ControlGroup();
    				fieldset.add(controlGroup);
    				ControlLabel controlLabel = new ControlLabel(CentrifugeConstantsLocator.get().numberOfLayoutIterations());
    				controlGroup.add(controlLabel);
    				iterationsTextBox.setValue(lastGoodValue);
    				Controls controls = new Controls();
    				controls.add(iterationsTextBox);
    				controlGroup.add(controls);

    				dialog.getActionButton().addClickHandler(new ClickHandler() {
    					@Override
    					public void onClick(ClickEvent event) {
    						try {
    							GraphLayout oldLayout = ((RelGraphViewDef) getPresenter().getVisualizationDef()).getLayout();
    							VortexFuture<List<CsiMap<String, String>>> future = getPresenter().getModel().applyLayout(
    											GraphLayout.applyForce, Integer.parseInt(iterationsTextBox.getValue()));

    							getPresenter().getGraphSurface().getToolTipManager().removeAllToolTips();
    							getPresenter().getGraphSurface().refreshWithNewLayout(future, oldLayout);
    							lastGoodValue = iterationsTextBox.getValue();
    						} catch (Exception ignored) {
    						}
    					}
    				});

    			}
    			dialog.show();
    			iterationsTextBox.setSelectionRange(0, iterationsTextBox.getValue().length());
    		} else {
    			GraphLayout oldLayout = ((RelGraphViewDef) getPresenter().getVisualizationDef()).getLayout();
    			VortexFuture<List<CsiMap<String, String>>> future = getPresenter().getModel().applyLayout(GraphLayout.applyForce);
    			getPresenter().getGraphSurface().getToolTipManager().removeAllToolTips();
    			getPresenter().getGraphSurface().refreshWithNewLayout(future, oldLayout);
    		}
        }
    }
}
