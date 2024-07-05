package csi.client.gwt.widget.display_list_widgets;

import com.google.gwt.resources.client.ImageResource;

/**
 * Created by centrifuge on 3/20/2015.
 */
class ComponentTreeItem {

    private String _key;
    private Integer _foreignKey;
    private String _name;
    private ImageResource _icon;

    ComponentTreeItem(Integer keyIn, String nameIn, ImageResource iconIn) {

        _foreignKey = keyIn;
        _name = nameIn;
        _icon = iconIn;

        _key = _foreignKey.toString();
    }

    Integer getForeignKey() {

        return _foreignKey;
    }

    String getKey() {

        return _key;
    }

    String getName() {

        return _name;
    }

    ImageResource getIcon() {

        return _icon;
    }
}
