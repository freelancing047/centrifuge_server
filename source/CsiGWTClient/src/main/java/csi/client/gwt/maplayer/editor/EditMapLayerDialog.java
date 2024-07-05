package csi.client.gwt.maplayer.editor;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Label;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.SizeProvidingModal;
import csi.client.gwt.widget.combo_boxes.StringComboBox;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.MapLayerType;
import csi.server.common.model.map.Basemap;
import csi.server.common.service.api.ModelActionsServiceProtocol;
import csi.server.common.util.ValuePair;

public class EditMapLayerDialog extends SizeProvidingModal {
	private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
	private MapMapLayerEditorPresenter presenter;
	private String ownerName;
	private String originalName;
	private DialogPrototype dialog;
	private TextBox name;
	private TextBox url;
	private StringComboBox type;
	private Label layernameLabel;
	private TextBox layername;
	private Label helpTextLabel = new Label();
	private csi.client.gwt.widget.buttons.Button deleteButton;
	private csi.client.gwt.widget.buttons.Button saveButton;

	public EditMapLayerDialog(MapMapLayerEditorPresenter presenter) {
		this.presenter = presenter;
		dialog = new DialogPrototype("Delete", "Save", "Cancel");
		dialog.setTitle(CentrifugeConstantsLocator.get().mapLayerEditor_editMapLayer());
		dialog.add(new Label(i18n.mapLayerEditor_mapLayerNameLabel()));
		name = new TextBox();
		name.addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				hideErrorIndicators();
			}
		});
		name.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				dialog.getSaveButton().setEnabled(canSave());
			}
		});
		dialog.add(name);
		helpTextLabel.getElement().getStyle().setColor("red");
		helpTextLabel.setVisible(false);
		dialog.add(helpTextLabel);
		dialog.add(new Label(i18n.mapLayerEditor_mapLayerUrlLabel()));
		url = new TextBox();
		url.setWidth("300px");
		dialog.add(url);
		
		dialog.add(new Label(i18n.mapLayerEditor_mapLayerTypeLabel()));
		type = new StringComboBox();
		dialog.add(type);
		type.getStore().clear();
		type.getStore().add(MapLayerType.ARCGIS_TILED.getLabel());
		type.getStore().add(MapLayerType.OPENSTREETMAP.getLabel());
		type.getStore().add(MapLayerType.WMS.getLabel());
		type.addSelectionHandler(event -> {
			showOrHideExtraFields(event.getSelectedItem());
		});
		
		layernameLabel = new Label(i18n.mapLayerEditor_mapLayerLayerNameLabel()); 
		dialog.add(layernameLabel);
		layername = new TextBox();
		dialog.add(layername);

		deleteButton = dialog.getDeleteButton();
		deleteButton.setType(ButtonType.LINK);
		deleteButton.addClickHandler(event -> {
			presenter.deleteBasemap();
			dialog.hide();
		});

		saveButton = dialog.getSaveButton();
		saveButton.addClickHandler(event -> {
			if (canSave()) {
				String newBasemapName = name.getText().trim();
				presenter.saveModel(ownerName, newBasemapName, url.getText(), type.getText(), layername.getText());
				dialog.hide();
			}
		});

		dialog.hideOnCancel();
	}

	public void display(Basemap basemap) {
		hideErrorIndicators();
		ownerName = basemap.getOwner();
		originalName = basemap.getName();
		name.setText(originalName);
		dialog.getSaveButton().setEnabled(canSave());
		url.setText(basemap.getUrl());
		String typeString = basemap.getType();
		int selectedIndex = -1;
		if (typeString != null && !typeString.isEmpty())
			selectedIndex = type.getStore().indexOf(typeString);
		if (selectedIndex == -1) 
			selectedIndex = type.getStore().indexOf(MapLayerType.ARCGIS_TILED.getLabel());
		type.setSelectedIndex(selectedIndex);
		String selectedText = type.getStore().get(selectedIndex);
		type.setText(selectedText);
		if (basemap.getLayername() == null)
			layername.setText("");
		else
			layername.setText(basemap.getLayername());
		showOrHideExtraFields(selectedText);
		checkBasemapPermission(basemap);
	}

	private void showOrHideExtraFields(String selectedText) {
		boolean isWMS = selectedText == MapLayerType.WMS.getLabel();
		layernameLabel.setVisible(isWMS);
		layername.setVisible(isWMS);
	}

	public void show() {
		dialog.show();
	}

	private void hideErrorIndicators() {
		name.getElement().getStyle().setColor(null);
		helpTextLabel.setVisible(false);
	}

	private boolean canSave() {
		boolean retval;
		String nameText = name.getText();
		if (nameText == null) {
			helpTextLabel.setText(i18n.mapLayerEditor_mapLayerMustHaveName());
			helpTextLabel.setVisible(true);
			retval = false;
		} else {
			String newBasemapName = nameText.trim();
			if (newBasemapName.equals(originalName)) {
				retval = true;
			} else if (newBasemapName.equals("")) {
				helpTextLabel.setText(i18n.mapLayerEditor_mapLayerMustHaveName());
				helpTextLabel.setVisible(true);
				retval = false;
			} else if (presenter.basemapNameExists(ownerName, newBasemapName)) {
				name.getElement().getStyle().setColor("#FF0000");
				helpTextLabel.setText(i18n.mapLayerEditor_mapLayerNameExists());
				helpTextLabel.setVisible(true);
				retval = false;
			} else {
				retval = true;
			}
		}
		return retval;
	}

	private void checkBasemapPermission(Basemap basemap) {
		if (basemap == null) {
			saveButton.setVisible(false);
			deleteButton.setVisible(false);
		} else {
			UserSecurityInfo _userInfo = WebMain.injector.getMainPresenter().getUserInfo();
			String username = _userInfo.getName();
			String ownername = basemap.getOwner();
			if (username != null && ownername != null && username.equals(ownername)) {
				saveButton.setVisible(true);
				deleteButton.setVisible(true);
			} else {
				try {
					VortexFuture<ValuePair<String, Boolean>> vortexFuture = WebMain.injector.getVortex().createFuture();
					vortexFuture.addEventHandler(new VortexEventHandler<ValuePair<String, Boolean>>() {
						@Override
						public void onSuccess(ValuePair<String, Boolean> result) {
							saveButton.setVisible(result.getValue2());
						}

						@Override
						public boolean onError(Throwable t) {
							return false;
						}

						@Override
						public void onUpdate(int taskProgess, String taskMessage) {
						}

						@Override
						public void onCancel() {
						}
					});
					vortexFuture.execute(ModelActionsServiceProtocol.class).isAuthorized(basemap.getUuid(), AclControlType.EDIT);
				} catch (Exception myException) {
					saveButton.setVisible(false);
				}

				try {
					VortexFuture<ValuePair<String, Boolean>> vortexFuture = WebMain.injector.getVortex().createFuture();
					vortexFuture.addEventHandler(new VortexEventHandler<ValuePair<String, Boolean>>() {
						@Override
						public void onSuccess(ValuePair<String, Boolean> result) {
							deleteButton.setVisible(result.getValue2());
						}

						@Override
						public boolean onError(Throwable t) {
							return false;
						}

						@Override
						public void onUpdate(int taskProgess, String taskMessage) {
						}

						@Override
						public void onCancel() {
						}
					});
					vortexFuture.execute(ModelActionsServiceProtocol.class).isOwner(basemap.getUuid());
				} catch (Exception myException) {
					deleteButton.setVisible(false);
				}
			}
		}
	}
}
