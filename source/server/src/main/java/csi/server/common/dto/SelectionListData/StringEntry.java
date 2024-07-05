package csi.server.common.dto.SelectionListData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import csi.server.common.enumerations.DisplayMode;
import csi.server.common.util.DisplayableObject;


public class StringEntry extends DisplayableObject implements ExtendedDisplayInfo {

    private String _value;

    public StringEntry() {

        this(null, DisplayMode.NORMAL);
    }

    public StringEntry(String valueIn) {

        this(valueIn, DisplayMode.NORMAL);
    }

    public StringEntry(String valueIn, boolean isDisabledIn) {

        this(valueIn, (isDisabledIn) ? DisplayMode.DISABLED : DisplayMode.NORMAL);
    }

    public StringEntry(String valueIn, DisplayMode modeIn) {

        super(modeIn);
        _value = valueIn;
    }

    public StringEntry(String valueIn, int ordinalIn) {

        this(valueIn, DisplayMode.NORMAL, ordinalIn);
    }

    public StringEntry(String valueIn, boolean isDisabledIn, int ordinalIn) {

        this(valueIn, (isDisabledIn) ? DisplayMode.DISABLED : DisplayMode.NORMAL, ordinalIn);
    }

    public StringEntry(String valueIn, DisplayMode modeIn, int ordinalIn) {

        super(modeIn, ordinalIn);
        _value = valueIn;
    }

    public void setKey(String keyIn) {
        
        _value = keyIn;
    }
    
    public String getKey() {
        
        return _value;
    }
    
    public void setValue(String valueIn) {
        
        _value = valueIn;
    }
    
    public String getValue() {
        
        return _value;
    }

    public String getDisplayString() {

        return _value;
    }

    public String getTitleString() {

        return null;
    }

    public String getDescriptionString() {

        return null;
    }

    public static List<String> toStringList(Collection<StringEntry> listIn) {

        List<String> myListOut = new ArrayList<String>(listIn.size());

        for (StringEntry myEntry : listIn) {

            myListOut.add(myEntry.getValue());
        }
        return myListOut;
    }

    public static Set<String> toStringSet(Collection<StringEntry> listIn) {

        Set<String> mySetOut = new TreeSet<String>();

        for (StringEntry myEntry : listIn) {

            mySetOut.add(myEntry.getValue());
        }
        return mySetOut;
    }

    public static String[] toStringArray(Collection<StringEntry> listIn) {

        String[] myArrayOut = new String[listIn.size()];
        int i = 0;

        for (StringEntry myEntry : listIn) {

            myArrayOut[i++] = myEntry.getValue();
        }
        return myArrayOut;
    }

    @Override
    public String toString() {

        return _value;
    }

    @Override
    public String getParentString() {
        // TODO Auto-generated method stub
        return null;
    }
}
