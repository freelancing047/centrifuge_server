package csi.client.gwt.dataview.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

interface EditorResource extends ClientBundle {

    EditorResource IMPL = (EditorResource) GWT.create(EditorResource.class);

    ImageResource access();

    ImageResource accessDisabled();

    ImageResource custom();

    ImageResource customDisabled();

    ImageResource excel();

    ImageResource excelDisabled();

    ImageResource generic();

    ImageResource genericDisabled();

    ImageResource linked();

    ImageResource linkedDisabled();

    ImageResource mysql();

    ImageResource mysqlDisabled();

    ImageResource oracle();

    ImageResource oracleDisabled();

    ImageResource postgres();

    ImageResource postgresDisabled();

    ImageResource reserved();

    ImageResource reservedDisabled();

    ImageResource sql();

    ImageResource sqlDisabled();

    ImageResource table();

    ImageResource tableDisabled();

    ImageResource text();

    ImageResource textDisabled();

    ImageResource view();

    ImageResource viewDisabled();

    ImageResource webService();

    ImageResource webServiceDisabled();

    ImageResource xml();

    ImageResource xmlDisabled();
    
    ImageResource defaultFolder();
    
    ImageResource addIcon();
    
    ImageResource editIcon();
    
    ImageResource removeIcon();

    ImageResource customQueryIcon();

    ImageResource installerIcon();

    ImageResource icon2boolean();
    
    ImageResource icon2date_time();
    
    ImageResource icon2date();
    
    ImageResource icon2integer();
    
    ImageResource icon2number();
    
    ImageResource icon2string();
    
    ImageResource icon2time();

    ImageResource icon2unknown();
    
    ImageResource square();
    
    ImageResource icon_LDAP();
    
    ImageResource icon_LDAPdisabled();
    
    ImageResource impala();
    
    ImageResource impalaDisabled();

    ImageResource unknown();
}
