package csi.client.gwt.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface FieldDefResource extends ClientBundle {

    FieldDefResource IMPL = (FieldDefResource) GWT.create(FieldDefResource.class);

    @Source("images/field_type/fieldColumnRef.png")
    ImageResource fieldColumnRef();

    @Source("images/field_type/fieldScripted.png")
    ImageResource fieldScripted();

    @Source("images/field_type/fieldStatic.png")
    ImageResource fieldStatic();

    @Source("images/data_type/valueBoolean.png")
    ImageResource valueBoolean();

    @Source("images/data_type/valueDate.png")
    ImageResource valueDate();

    @Source("images/data_type/valueDateTime.png")
    ImageResource valueDateTime();

    @Source("images/data_type/valueInteger.png")
    ImageResource valueInteger();

    @Source("images/data_type/valueNumber.png")
    ImageResource valueNumber();

    @Source("images/data_type/valueString.png")
    ImageResource valueString();

    @Source("images/data_type/valueTime.png")
    ImageResource valueTime();

    @Source("images/data_type/valueUnknown.png")
    ImageResource valueUnknown();

    @Source("images/purposeSecurity.png")
    ImageResource purposeSecurity();

    @Source("images/shapes/menu.png")
    ImageResource contextMenu();

}
