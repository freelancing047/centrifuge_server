package csi.client.gwt.admin;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;

import csi.client.gwt.widget.buttons.SimpleButton;


public class SearchControl<T> {
    
    private TextBox _searchBox;
    private RadioButton _allButton;
    private Label _allLabel;
    private RadioButton _searchButton;
    private Label _searchLabel;
    private SimpleButton _getButton;
    private boolean _enableSearch;
    private boolean _enableLabels;
    private String _searchString = null;
    
    public SearchControl(TextBox searchBoxIn, RadioButton allButtonIn,
            Label allLabelIn, RadioButton searchButtonIn,
            Label searchLabelIn, SimpleButton getButtonIn) {
        
        _searchBox = searchBoxIn;
        _allButton = allButtonIn;
        _allLabel = allLabelIn;
        _searchButton = searchButtonIn;
        _searchLabel = searchLabelIn;
        _getButton = getButtonIn;
        
        searchEnableDisable();
        
        _searchButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent eventIn) {
                searchEnableDisable();
            }
        });
        
        _allButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent eventIn) {
                searchEnableDisable();
            }
        });
        
        _searchLabel.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent eventIn) {
                if (_enableLabels && _enableSearch) {
                    _searchButton.setValue(true);
                    searchEnableDisable();
                }
            }
        });
        
        _allLabel.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent eventIn) {
                if (_enableLabels && (! _enableSearch)) {
                    _allButton.setValue(true);
                    searchEnableDisable();
                }
            }
        });
        
        _searchBox.addKeyUpHandler(new KeyUpHandler() {
            public void onKeyUp(KeyUpEvent eventIn) {
                getEnableDisable();
            }
        });
        
        _searchBox.addDropHandler(new DropHandler() {
            public void onDrop(DropEvent eventIn) {
                _getButton.setEnabled(true);
            }
        });

        enableControls();
    }
    
    public String getSearchString() {
        
        _searchString = null;
        
        if (_searchBox.isEnabled()) {
            
            _searchString = _searchBox.getText();
        }
        return _searchString;
    }
    
    public String getRefreshString() {

        return _searchString;
    }

    public void disableControls() {
        
        _searchBox.setEnabled(false);
        _allButton.setEnabled(false);
        _searchButton.setEnabled(false);
        _getButton.setEnabled(false);
        _enableLabels = false;
    }
    
    public void enableControls() {
        
        _searchBox.setEnabled(_enableSearch);
        _allButton.setEnabled(true);
        _searchButton.setEnabled(true);
        getEnableDisable();
        _enableLabels = true;
    }
    
    private void searchEnableDisable() {
        
        _enableSearch = _allButton.getValue();
        _searchBox.setEnabled(_enableSearch);
        if (_enableSearch) {
            _searchBox.setFocus(true);
        }
        getEnableDisable();
    }
    
    private void getEnableDisable() {
        boolean myActive = false;
        
        if ((!_enableSearch) || (0 < _searchBox.getValue().length())) {
            myActive = true;
        }
        _getButton.setEnabled(myActive);
    }
}
