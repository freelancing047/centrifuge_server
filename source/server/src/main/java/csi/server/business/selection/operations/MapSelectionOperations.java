package csi.server.business.selection.operations;

import csi.server.business.visualization.map.MapServiceUtil;
import csi.server.common.model.visualization.selection.AbstractMapSelection;

public class MapSelectionOperations implements SelectionOperations<AbstractMapSelection> {
	@Override
	public void add(AbstractMapSelection existingSelection, AbstractMapSelection selectionToAdd) {
		MapServiceUtil.selectionAddNodes(selectionToAdd.getNodes(), existingSelection);
		existingSelection.addLinks(selectionToAdd.getLinks());
	}

	@Override
	public void remove(AbstractMapSelection existingSelection, AbstractMapSelection removalSelection) {
		MapServiceUtil.selectionRemoveNodes(removalSelection.getNodes(), existingSelection);
		existingSelection.removeLinks(removalSelection.getLinks());
	}

	@Override
	public void replace(AbstractMapSelection existingSelection, AbstractMapSelection replacingSelection) {
		existingSelection.clearSelection();
		add(existingSelection, replacingSelection);
	}
}
