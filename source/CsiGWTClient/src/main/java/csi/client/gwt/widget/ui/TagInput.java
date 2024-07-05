/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.widget.ui;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TagInput extends Widget {

    private List<String> tags = new ArrayList<String>();
    private Element ul;

    public TagInput() {
        super();
        ul = DOM.createElement("ul"); //$NON-NLS-1$
        ul.setId(DOM.createUniqueId());
        setElement(ul);
    }

    //Needed because the user can remove tags via Jquery,
    //and the tags variable will have no idea.
    private void refreshTags(){
        tags.clear();
        JsArrayString arr = _getTags(ul.getId());
        for (int i = 0; i < arr.length(); i++) {
            tags.add(arr.get(i));
        }
    }

    public List<String> getTags() {
        if (isAttached()) {
            refreshTags();
        }
        return tags;
    }

    public void addTag(String tag) {
        refreshTags();
        if(!tags.contains(tag)) {
            tags.add(tag);
        }
        updateTags();
    }

    public void setId(String id) {
        ul.setId(id);
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
        if (isAttached()) {
            updateTags();
        }
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        _init(ul.getId());
        updateTags();
    }

    private void updateTags() {
        JsArrayString jsStrings = (JsArrayString) JsArrayString.createArray();
        for (String tag : tags) {
            jsStrings.push(tag);
        }
        _updateTags(ul.getId(), jsStrings);
    }

    private native void _init(String id) /*-{
		var eid = "#" + id;
		var obj = $wnd.jQuery(eid);
		obj.tagit({
			// prevent auto-complete at all costs!!
			autocomplete : {
				delay : 100000000,
				minLength : 200000
			}
		});
    }-*/;

    private native void _updateTags(String id, JsArrayString tags) /*-{
		var eid = "#" + id;
		var obj = $wnd.jQuery(eid);
		obj.tagit("removeAll");
		tags.forEach(function(tag) {
			obj.tagit("createTag", tag);
		});
    }-*/;

    private native JsArrayString _getTags(String id) /*-{
		var eid = "#" + id;
		return $wnd.jQuery(eid).tagit("assignedTags");
    }-*/;

}
