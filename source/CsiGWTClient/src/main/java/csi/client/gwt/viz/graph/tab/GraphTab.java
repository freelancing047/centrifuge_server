package csi.client.gwt.viz.graph.tab;

import com.github.gwtbootstrap.client.ui.Tab;
import com.github.gwtbootstrap.client.ui.TabLink;
import com.github.gwtbootstrap.client.ui.TabPane;

public class GraphTab extends Tab {
	private String tabname;
	
	public GraphTab() {
		// TODO Auto-generated constructor stub
	}

	public String getTabname() {
		return tabname;
	}

	public void setTabname(String tabname) {
		this.tabname = tabname;
		showTabTitle();
	}

	public void hideTabTitle() {
		TabLink tablink = asTabLink();
		tablink.setText("");
		tablink.setTitle(tabname);
	}
	
	public void showTabTitle() {
		TabLink tablink = asTabLink();
		tablink.setText(tabname);
		tablink.setTitle("");
	}
	
	
	public TabPane getTabPane(){
	    return super.getTabPane();
	}
}