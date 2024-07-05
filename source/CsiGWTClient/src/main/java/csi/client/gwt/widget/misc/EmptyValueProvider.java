package csi.client.gwt.widget.misc;

import com.sencha.gxt.core.client.ValueProvider;

public class EmptyValueProvider<T> implements ValueProvider<T, Void>{

        @Override
        public Void getValue(T object) {
            
            return null;
        }

        @Override
        public String getPath() {
            return "";
        }

        @Override
        public void setValue(T object, Void value) {
            
        }
    }