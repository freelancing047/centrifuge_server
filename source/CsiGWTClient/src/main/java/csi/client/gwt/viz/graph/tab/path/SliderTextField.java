package csi.client.gwt.viz.graph.tab.path;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.sencha.gxt.widget.core.client.Slider;
import com.sencha.gxt.widget.core.client.event.BlurEvent;
import com.sencha.gxt.widget.core.client.event.BlurEvent.BlurHandler;
import com.sencha.gxt.widget.core.client.form.TextField;

public class SliderTextField extends TextField {
    
    private Slider slider;
    
    public void addSlider(Slider slider){
        this.slider = slider;
        setValue(slider.getValue()+"");
        addChangeHandler(createChangeHandler());
        slider.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                setText(event.getValue().toString());
            }
        });
        addBlurHandler(new BlurHandler(){

            @Override
            public void onBlur(BlurEvent event) {
                    handleChange();
            }});
        
    }

    private ChangeHandler createChangeHandler() {
        return new ChangeHandler(){

            @Override
            public void onChange(ChangeEvent event) {
                handleChange();
                
            }

        };
    }
    
    private void handleChange() {
        String value = getCurrentValue();
        String originalValue = slider.getValue() + "";
        try{
            int valueInt = Integer.parseInt(value);
            if(valueInt == slider.getValue()){
                //Do nothing if values are the same
            } else if(valueInt >= slider.getMinValue()&&
                    valueInt <= slider.getMaxValue()){
                updateSlider(valueInt);
            } else {
                throw new Exception("Not within range");
            }
        } catch(Exception exception){
            //Not a valid value
            revertValue(originalValue);
        }
    }
    
    private void updateSlider(int value) {
        //TODO: There seems to be a bug with the Sencha Slider
        //      by doing this the value displayed is slightly off
        //      no current solution other than to find a new slider.
        slider.setValue((value), true);
        
    }
    
    private void revertValue(String originalValue) {
        try{
            setValue(originalValue, true);
        }
        catch(Exception exception){
            //this fails sometimes not sure why
            //has to do with the blurhandler
        }
    }
    
}
