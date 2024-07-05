/** 
 *  Copyright (c) 2008-2013 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.server.common.service.api;

import java.io.IOException;
import java.util.List;

import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.SelectionListData.SharingInitializationRequest;
import csi.server.common.dto.SelectionListData.SharingRequest;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.linkup.TemplateResponse;
import csi.server.common.model.ConnectionDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.linkup.LinkupMapDef;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.util.ValuePair;
import csi.shared.gwt.vortex.VortexService;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface DataViewDefActionsServiceProtocol extends VortexService {


    public List<String> listAllTemplateNames() throws CentrifugeException;


    public List<String> listUserTemplateNames() throws CentrifugeException;

    
    public List<ResourceBasics> listUserTemplateBasics() throws CentrifugeException;


    public List<ResourceBasics> listUserTemplateBasics(AclControlType accessModeIn) throws CentrifugeException;

    public List<ResourceBasics> listTemplateEditBasics() throws CentrifugeException;

    public List<ResourceBasics> listTemplateBasics(AclControlType accessModeIn) throws CentrifugeException;

    public List<ResourceBasics> listTemplateExportBasics() throws CentrifugeException;

    public List<ResourceBasics> listLinkupTemplateBasics() throws CentrifugeException;

    public List<List<ResourceBasics>> getTemplateOverWriteControlLists() throws CentrifugeException;

    public List<String> listTemplateNames(AclControlType accessModeIn) throws CentrifugeException;

    public List<ResourceBasics> listSampleBasics(AclControlType accessModeIn) throws CentrifugeException;

    public List<List<ResourceBasics>> getSampleOverWriteControlLists() throws CentrifugeException;

    public List<String> listSampleNames(AclControlType accessModeIn) throws CentrifugeException;


    public List<FieldDef> listFieldDefs(String uuid) throws CentrifugeException;

    public Response<String, TemplateResponse> getLinkupTemplate(LinkupMapDef linkupIn);

    public Response<String, TemplateResponse> getLinkupTemplate(String templateIdIn);

    public Response<String, TemplateResponse> getTemplate(String templateIdIn, AclControlType modeIn);

    // Used to save a template -- follows security protocols
    public DataViewDef save(DataViewDef def) throws CentrifugeException;

    // Used to update data sources -- follows security protocol
    public Response<String, DataViewDef> editTemplate(DataViewDef templateIn);

    public DataViewDef cloneDataViewDef(String uuid) throws CentrifugeException, IOException ;

    // NOT USED -- follows security protocols
    public DataViewDef saveDataViewDefAs(String uuid, String newName) throws CentrifugeException;


    public Boolean canCreateConnectionType(ConnectionDef def) throws CentrifugeException;


    // NOT USED -- follows security protocols
    public String deleteDataViewDef(String uuid) throws CentrifugeException;


    public DataView createDataViewFromTemplate(String uuid, String name) throws CentrifugeException;

    public List<String> testCoreFieldReferences(String templateUuidIn);

    public List<String> testFieldReferences(String templateUuidIn);

    public List<String> testFieldReferences(String templateUuidIn, List<String> fieldUuidsIn);

    public List<String> testFieldReferenceAndReturnViz(String templateUuidIn, String fieldUuidIn);

    public Response<String, DataViewDef> createTemplate(String requestIdIn, String nameIn, String commentsIn,
                                                        AdHocDataSource dataSourceIn, boolean overwriteIn,
                                                        SharingInitializationRequest sharingRequestIn);
}
