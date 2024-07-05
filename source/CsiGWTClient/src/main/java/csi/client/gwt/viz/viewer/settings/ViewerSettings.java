package csi.client.gwt.viz.viewer.settings;

import com.google.common.collect.Lists;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import csi.server.common.model.visualization.viewer.LensDef;
import csi.shared.gwt.viz.viewer.settings.editor.LensDefSettings;

import java.util.List;

public class ViewerSettings {

    public List<LensDef> lenses;

    //TODO:I think saving all the viewer defs as JSON might make this feature more extensible. JOG integrity is risk.
    List<String> fieldDefs = Lists.newArrayList();
//    private FieldLensDef fieldLensDef;


    public ViewerSettings(String viewerString) {
        JSONValue jsonValue = JSONParser.parseLenient(viewerString);
        JSONValue fields = jsonValue.isObject().get("fields");
        int size = fields.isArray().size();
        for (int i = 0; i < size; i++) {
            fieldDefs.add(fields.isArray().get(i).isString().stringValue());
        }
/*        lenses.add(new NodeLensDef());
        lenses.add(new LinkLensDef());
        this.fieldLensDef = new FieldLensDef();
        this.fieldLensDef.localIds.clear();
        this.fieldLensDef.localIds.addAll(fieldDefs);
        lenses.add(this.fieldLensDef);*/

    }



//    public List<LensDef> getLenses() {
//        return lenses;
//    }
//
//    List<LensDef> lenses = Lists.newArrayList();

    public ViewerSettings() {
//        lenses.add(new NodeLensDef());
//        lenses.add(new NodeNeighborLabelLensDef());
//        lenses.add(new LinkLensDef());
//        fieldLensDef = new FieldLensDef();
//        lenses.add(fieldLensDef);

    }

    public void add(LensDefSettings lensDefSettings) {

    }


    /*public String saveString() {
        JSONArray jsonValue = new JSONArray();
            int index = 0;
        for (String fieldDef : fieldLensDef.localIds) {

            jsonValue.set(index++, new JSONString(fieldDef));
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fields", jsonValue);
        return jsonObject.toString();
    }*/
}
