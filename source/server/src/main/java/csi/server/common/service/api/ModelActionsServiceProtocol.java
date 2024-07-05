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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import csi.server.common.dto.AccessRights;
import csi.server.common.dto.AuthDO;
import csi.server.common.dto.LaunchParam;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.SelectionListData.SharingInitializationRequest;
import csi.server.common.dto.user.preferences.ResourceFilter;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.interfaces.MapByDataType;
import csi.server.common.model.ModelObject;
import csi.server.common.model.Resource;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.util.ValuePair;
import csi.shared.gwt.vortex.VortexService;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface ModelActionsServiceProtocol extends VortexService {

    public Response<String,? extends Resource>
    createDataResource(String requestIdIn, String nameIn, String commentsIn, AclResourceType resourceTypeIn,
                       AdHocDataSource dataSourceIn, List<LaunchParam> parametersIn, List<AuthDO> credentialsIn,
                       CapcoInfo capcoInfoIn, SecurityTagsInfo tagInfoIn, boolean overwriteIn,
                       SharingInitializationRequest sharingRequestIn);

    // Used for saving worksheet state
    public ModelObject save(ModelObject obj) throws CentrifugeException;

    // NOT USED!
//    public void saveList(List<ModelObject> objs) throws CentrifugeException;

    // NOT USED! except in a unit test
//    public Response<String, Resource> saveAs(String srcUuid, String newName, String newRemarks, boolean forceOverWriteIn);

    // Used for saving to new dataview or saving as a template -- appears to follow security measures
    public Response<String, Resource> saveCurrentAs(String newName, String newRemarks, Resource res, boolean forceOverWriteIn);


    // NOT USED!
//    public String getUniqueResourceName(String resName) throws CentrifugeException;


    // NOT USED!
//    public boolean isUniqueResourceName(String name) throws CentrifugeException;


    // NOT USED!
//    public List<String> testResourceNames(List<String> names) throws CentrifugeException;


    // NOT USED!
//    public ResourceDO resourceInfo(String uuid) throws CentrifugeException;


    public List<ResourceBasics> listUserResourceBasics(AclResourceType resourceTypeIn)
            throws CentrifugeException;

    public List<ResourceBasics> listUserResourceBasics(AclResourceType resourceTypeIn, String ownerIn)
            throws CentrifugeException;

    public List<List<ResourceBasics>> getOverWriteControlLists(AclResourceType resourceTypeIn) throws CentrifugeException;

    public List<ResourceBasics> getFilteredResourceList(AclResourceType resourceTypeIn,
                                                        ResourceFilter filterIn, AclControlType permissionIn)
            throws CentrifugeException;

    public List<List<String>> delete(AclResourceType resourceTypeIn, List<String> listIn) throws CentrifugeException;

    public Response<String, ValuePair<List<? extends MapByDataType>, ValuePair<CapcoInfo, SecurityTagsInfo>>>
            getSecurityInfo(String uuidIn, AclResourceType typeIn, AclControlType modeIn);

    public Response<String, ValuePair<Boolean, ValuePair<CapcoInfo, SecurityTagsInfo>>>
            classifyResource(String uuidIn, AclResourceType typeIn, CapcoInfo capcoIn, SecurityTagsInfo tagsIn);

    public Response<String, ValuePair<String, String>> renameResource(String uuidIn, AclResourceType typeIn,
                                                                       String nameIn, String remarksIn);

    public AccessRights verifyAccess(String uuidIn);

    public ValuePair<String, Boolean> isAuthorized(String uuidIn, AclControlType permissionIn);

    public ValuePair<String, Boolean> isAuthorized(String uuidIn, AclControlType permissionIn, boolean doSecurityIn);

    public ValuePair<String, Boolean> isOwner(String uuidIn);

    public void cancelTask(String taskIdIn);

    public Response<String, Resource> saveDataviewAsDataview(String uuidIn, String newNameIn, String newRemarksIn, boolean forceIn) throws CentrifugeException;

    public Response<String, Resource> saveTemplateAsTemplate(String uuidIn, String newNameIn, String newRemarksIn, boolean forceIn) throws CentrifugeException;

    public Response<String, Resource> saveDataviewAsTemplate(String uuidIn, String newNameIn, String newRemarksIn, boolean forceIn) throws CentrifugeException;

    public List<List<ResourceBasics>> getResourceOverWriteControlLists(AclResourceType typeIn) throws CentrifugeException;

    public Map<String, List<ResourceBasics>> getAdminResourceLists(AclResourceType typeIn, Collection<String> userList)
            throws CentrifugeException;
}