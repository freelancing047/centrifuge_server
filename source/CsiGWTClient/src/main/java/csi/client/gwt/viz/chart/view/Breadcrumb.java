package csi.client.gwt.viz.chart.view;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.NavWidget;
import com.google.gwt.event.dom.client.ClickHandler;

public class Breadcrumb extends NavWidget {

    // how this item will be displayed by the breadcrumber
    public enum RenderState{
        FULL,        // getComposedName();
        SHORT,      // Just the value ( of the category drill)
        MICRO,      // ... ( possibly icon too)
        HIDDEN     // invisible
    }
    private List<RenderState> disabledStates = new ArrayList();
    private String categoryName;
    private String crumbName;


    private RenderState renderState = RenderState.FULL;
    private char[] delim = {'(',')'};
    private String microTextValue = "...";


    /**
     * Sets the title of the breadcrumb to <categoryName><DELIMETER><crumbName><DELIMITER>
     * Builds a breadcrumb from a category name, and crumbName, and the tooltip
     *
     * The resulting crumb will have a name, attached ClickHandler,
     *      a default render value of RenderState.FULL and a tooltip
     *
     * //TODO maybe make the tooltip configurable
     *
     * @param categoryName
     * @param crumbName
     * @param handler
     *
     *
     */
    public Breadcrumb(String categoryName, String crumbName, ClickHandler handler){
        this.crumbName = crumbName;
        this.categoryName = categoryName;

        this.setText(this.buildName());
        this.addClickHandler(handler);

        this.setTooltipText("Drill out to " + getCategoryName());
    }

    /**
     * Creates a breadcrumb with a name and a handler
     * @param text
     * @param handler - ClickHandler which will get executed when clicked.
     */
    public Breadcrumb(String text, ClickHandler handler){
        this.setText(text);
        this.addClickHandler(handler);
    }

    /**
     *
     * @param text
     */
    public Breadcrumb(String text) {
        this.setText(text);
    }

    /**
     * Calls setTitle wiht param, this sets the tooltip
     * @param tooltipText
     */
    public void setTooltipText(String tooltipText){
        this.setTitle(tooltipText);
    }

    /**
     * wrap fof getTitle to get the tooltip
     * @return
     */
    public String getTooltipText(){
        return this.getTitle();
    }

    // this might be moved to the enum
    private String buildName(){
        return this.categoryName + delim[0] + this.crumbName +  delim[1];
    }


    /**
     * THIS NEEDS TO GET CALLED AFTER YOU SET THE RENDER STATE ON THE OBJ.
     */
    private void onRenderStateChange(){
        //sexy toggle for name
        this.setVisible(true);
        switch (this.renderState) {
            case HIDDEN:
                this.setVisible(false);
                break;
            case MICRO:
                this.setText(this.getMicroTextValue());
                break;
            case SHORT:
                this.setText(getCrumbName());
                break;
            case FULL:
                this.setText(buildName());
                break;
        }

    }

    /**
     * Returns true if the argument state is not one of the disabledStates
     *
     * @param renderState
     * @return
     */
    public boolean isEnabled(RenderState renderState){
        return !(this.disabledStates.contains(renderState));
    }


    /**
     * The passed in state will be disabled on the node.
     *
     * Note: the RenderState status is enforced in setRenderState(), which will check if its an enabled state, and set it.
     * You can overwrite the RenderState using the overwriteRenderState() method. The rendering of the cell isn't aware of the banned states.
     *
     * @param disabledRenderState
     */
    public void disableRenderState(RenderState disabledRenderState){
        this.disabledStates.add(disabledRenderState);
    }


    public List<RenderState> getDisabledRenderStates(){
        return this.disabledStates;
    }

    public void setDisabledStates(List<RenderState> disabledRenderStates){
        this.disabledStates = disabledRenderStates;
    }

    //****************//


    public String getMicroTextValue() {
        return microTextValue;
    }

    public void setMicroTextValue(String microTextValue) {
        this.microTextValue = microTextValue;
    }

    public RenderState getRenderState() {
        return renderState;
    }

    /**
     * Sets the render state of the breadcrumb without checking if its disabled.
     *
     * @param renderState
     */
    public void overwriteRenderState(RenderState renderState){
        this.renderState = renderState;
        onRenderStateChange();
    }


    /**
     * Checks if the passed in state is allowed for this crumb, and sets the new state if allowed.
     * @param renderState
     */
    public void setRenderState(RenderState renderState) {
        if (isEnabled(renderState)) {
            this.renderState = renderState;
            onRenderStateChange();
        }else{
            onRenderStateChange();
        }
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCrumbName() {
        return crumbName;
    }

    public void setCrumbName(String crumbName) {
        this.crumbName = crumbName;
    }

    public char[] getDelim() {
        return delim;
    }

    public void setDelim(char[] delim) {
        this.delim = delim;
    }
}


