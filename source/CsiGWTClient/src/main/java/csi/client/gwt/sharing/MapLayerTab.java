package csi.client.gwt.sharing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.events.TemplateNameChangeEvent;
import csi.client.gwt.events.TemplateNameChangeEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.maplayer.editor.MapLayerEditorPresenter;
import csi.client.gwt.util.Display;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.SharingDisplay;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.map.Basemap;
import csi.server.common.service.api.MapActionsServiceProtocol;

/**
 * Created by centrifuge on 7/8/2015.
 */
public class MapLayerTab extends ResourceTab {
	private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

	public MapLayerTab(ResourceSharingView parentIn) {
		super(parentIn, "sharing.MapLayerTab");
		wireInHandlers();

		renameButton.setVisible(false);
	}

    protected String getResourceTypeString() {
        return "Basemap";
    }

    protected String getResourceTypePluralString() {
        return "Basemaps";
    }

	protected AclResourceType getResourceType() {
		return AclResourceType.MAP_BASEMAP;
	}

	protected ClickHandler getExportClickHandler() {
		return null;
	}

	protected ClickHandler getRenameClickHandler() {
		return null;
	}

    protected ClickHandler getDeleteClickHandler() {

        return buildDeleteDialog(handleDeleteConfirmed);
    }

    protected ClickHandler getClassificationHandler() {

        return handleEditClassificationRequest;
    }

	protected ClickHandler handleDeleteConfirmed = new ClickHandler() {
		public void onClick(ClickEvent eventIn) {
			final List<String> myItemList = new ArrayList<String>();
            List<SharingDisplay> mySelectedGridList = _grid.getSelectionModel().getSelection();

			_selectionMap = new HashMap<String, SharingDisplay>();

			for (int i = 0; mySelectedGridList.size() > i; i++) {
				SharingDisplay myItem = mySelectedGridList.get(i);

				_selectionMap.put(myItem.getUuid(), myItem);
				myItemList.add(myItem.getUuid());
			}

			VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
			try {
				watchBox.show(_constants.resourceTab_deleteResources());
				future.execute(MapActionsServiceProtocol.class).deleteBasemaps(myItemList);

				future.addEventHandler(new AbstractVortexEventHandler<Void>() {
					@Override
					public boolean onError(Throwable myException) {
						watchBox.hide();
						Display.error("BasemapTab", 1, myException);
						return true;
					}

					@Override
					public void onSuccess(Void result) {
						for (String myItem : myItemList) {
							SharingDisplay myInfo = _selectionMap.get(myItem);

							if (null != myInfo) {
								mySelectedGridList.remove(myInfo);
								_grid.getStore().remove(myInfo);
							}
						}

						watchBox.hide();
					}
				});
			} catch (CentrifugeException e) {
				watchBox.hide();
				Display.error("BasemapTab", 2, e);
			}
		}
	};

	protected ClickHandler getEditClickHandler() {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
                List<SharingDisplay> mySelectedGridList = _grid.getSelectionModel().getSelection();
				SharingDisplay shareInfo = mySelectedGridList.get(0);
				MapLayerEditorPresenter presenter = new MapLayerEditorPresenter();
				presenter.editBasemap(shareInfo.getUuid(), null);
			}
		};
	}
	
	private MapLayerEditorPresenter presenter;

	protected ClickHandler getCreateClickHandler() {
		return new ClickHandler() {
	        public void onClick(ClickEvent eventIn) {
	        	presenter = new MapLayerEditorPresenter(true);
	        }
	    };
	}

	protected ClickHandler getLaunchClickHandler() {
		return null;
	}

	protected String getEditButtonLabel() {
		return Dialog.txtEditButton;
	}

	protected String getCreateButtonLabel() {
		return _constants.manageResources_LayerTab_newLayerButton();
	}

	@Override
	protected void wireInHandlers() {
		super.wireInHandlers();

		WebMain.injector.getEventBus().addHandler(TemplateNameChangeEvent.type, new TemplateNameChangeEventHandler() {
			@Override
			public void onTemplateNameChange(TemplateNameChangeEvent eventIn) {
				if (null != eventIn) {
					renameLocalResourceEntry(eventIn.getUuid(), eventIn.getName(), eventIn.getRemarks());
				}
			}
		});
	}

	@Override
	public IconType getIconType() {
		return IconType.GLOBE;
	}
}
