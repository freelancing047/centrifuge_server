package csi.client.gwt.viz.map.presenter;

import com.google.gwt.core.client.Scheduler;

public class NullableRepeatingCommand implements Scheduler.RepeatingCommand {
	private boolean isRepeat = true;
	private MapPresenter mapPresenter;
	private CommandToExecute commandToExecute;

	interface CommandToExecute {
		boolean execute(MapPresenter presenter, boolean isRepeat);
	}

	public NullableRepeatingCommand(MapPresenter map, CommandToExecute commandToExecute) {
		this.mapPresenter = map;
		this.commandToExecute = commandToExecute;
	}

	@Override
	public boolean execute() {
		return commandToExecute.execute(mapPresenter, isRepeat);
	}

	public boolean isRepeat() {
		return isRepeat;
	}

	public void setRepeat(boolean isRepeat) {
		this.isRepeat = isRepeat;
	}
}
