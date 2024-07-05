package csi.client.gwt.widget.cells;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.sencha.gxt.core.client.XTemplates;

import csi.server.common.enumerations.DisplayMode;


public class ExtendedDisplays {
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    interface singleArg extends XTemplates {
        
        SafeHtml displayEntry(String listStringIn);
    }
    
    interface doubleArg extends XTemplates {
        
        SafeHtml displayEntry(String listStringIn, String hoverStringIn);
    }
    
    interface tripleArg extends XTemplates {
        
        SafeHtml displayEntry(String listStringIn, String hoverStringIn, String descriptionI);
    }
    
    interface normalSingle extends singleArg {
    
        @XTemplate("<div title=\"{listStringIn}\">{listStringIn}</div>")
        SafeHtml displayEntry(String listStringIn);
    }
    
    interface disabledSingle extends singleArg {
    
        @XTemplate("<div style=\"color:gray;\" title=\"{listStringIn}\">{listStringIn}</div>")
        SafeHtml displayEntry(String listStringIn);
    }
    
    interface errorSingle extends singleArg {
    
        @XTemplate("<div style=\"color:red;\" title=\"{listStringIn}\">{listStringIn}</div>")
        SafeHtml displayEntry(String listStringIn);
    }

    interface specialSingle extends singleArg {

        @XTemplate("<div style=\"color:blue;\" title=\"{listStringIn}\">{listStringIn}</div>")
        SafeHtml displayEntry(String listStringIn);
    }

    interface componentSingle extends singleArg {

        @XTemplate("<div title=\"{listStringIn}\">{listStringIn}</div>")
        SafeHtml displayEntry(String listStringIn);
    }

    interface normalDouble extends doubleArg {
    
        @XTemplate("<div title=\"{hoverStringIn}\">{listStringIn}</div>")
        SafeHtml displayEntry(String listStringIn, String hoverStringIn);
    }
    
    interface disabledDouble extends doubleArg {
    
        @XTemplate("<div style=\"color:gray;\" title=\"{hoverStringIn}\">{listStringIn}</div>")
        SafeHtml displayEntry(String listStringIn, String hoverStringIn);
    }
    
    interface errorDouble extends doubleArg {
    
        @XTemplate("<div style=\"color:red;\" title=\"{hoverStringIn}\">{listStringIn}</div>")
        SafeHtml displayEntry(String listStringIn, String hoverStringIn);
    }
    
    interface specialDouble extends doubleArg {
    
        @XTemplate("<div style=\"color:blue;\" title=\"{hoverStringIn}\">{listStringIn}</div>")
        SafeHtml displayEntry(String listStringIn, String hoverStringIn);
    }

    interface componentDouble extends doubleArg {

        @XTemplate("<div>{listStringIn}</div>")
        SafeHtml displayEntry(String listStringIn, String hoverStringIn);
    }

    interface normalTriple extends tripleArg {
    
        @XTemplate("<div qtip=\"{descriptionIn}\" qtitle=\"{hoverStringIn}\">{listStringIn}</div>")
        SafeHtml displayEntry(String listStringIn, String hoverStringIn, String descriptionIn);
    }
    
    interface disabledTriple extends tripleArg {
    
        @XTemplate("<div style=\"color:gray;\" qtip=\"{descriptionIn}\" qtitle=\"{hoverStringIn}\">{listStringIn}</div>")
        SafeHtml displayEntry(String listStringIn, String hoverStringIn, String descriptionIn);
    }
    
    interface errorTriple extends tripleArg {
    
        @XTemplate("<div style=\"color:red;\" qtip=\"{descriptionIn}\" qtitle=\"{hoverStringIn}\">{listStringIn}</div>")
        SafeHtml displayEntry(String listStringIn, String hoverStringIn, String descriptionIn);
    }
    
    interface specialTriple extends tripleArg {
    
        @XTemplate("<div style=\"color:blue;\" qtip=\"{descriptionIn}\" qtitle=\"{hoverStringIn}\">{listStringIn}</div>")
        SafeHtml displayEntry(String listStringIn, String hoverStringIn, String descriptionIn);
    }

    interface componentTriple extends tripleArg {

        @XTemplate("<div>{listStringIn}</div>")
        SafeHtml displayEntry(String listStringIn, String hoverStringIn, String descriptionIn);
    }

    interface noArg extends XTemplates {
        
        @XTemplate("<div> </div>")
        SafeHtml displayEntry();
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static singleArg _singleArg[] = {

        (singleArg)GWT.create(normalSingle.class),
        (singleArg)GWT.create(specialSingle.class),
        (singleArg)GWT.create(disabledSingle.class),
        (singleArg)GWT.create(errorSingle.class),
        (singleArg)GWT.create(componentSingle.class)
    };
    
    private static doubleArg _doubleArg[] = {

        (doubleArg)GWT.create(normalDouble.class),
        (doubleArg)GWT.create(specialDouble.class),
        (doubleArg)GWT.create(disabledDouble.class),
        (doubleArg)GWT.create(errorDouble.class),
        (doubleArg)GWT.create(componentDouble.class)
     };
    
    private static tripleArg _tripleArg[] = {

        (tripleArg)GWT.create(normalTriple.class),
        (tripleArg)GWT.create(specialTriple.class),
        (tripleArg)GWT.create(disabledTriple.class),
        (tripleArg)GWT.create(errorTriple.class),
        (tripleArg)GWT.create(componentTriple.class)
     };
    
    private static noArg _noArg = GWT.create(noArg.class);

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public static SafeHtml displayEntry() {
        
        return _noArg.displayEntry();
    }
    
    public static SafeHtml displayEntry(DisplayMode modeIn, String listStringIn) {
        
        return _singleArg[modeIn.ordinal()].displayEntry(listStringIn);
    }
    
    public static SafeHtml displayEntry(DisplayMode modeIn, String listStringIn, String hoverStringIn) {
        
        return _doubleArg[modeIn.ordinal()].displayEntry(listStringIn, hoverStringIn);
    }
    
    public static SafeHtml displayEntry(DisplayMode modeIn, String listStringIn, String hoverStringIn, String descriptionIn) {
        
        return _tripleArg[modeIn.ordinal()].displayEntry(listStringIn, hoverStringIn, descriptionIn);
    }
}
