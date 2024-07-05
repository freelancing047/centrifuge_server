package csi.server.business.service.theme;

import java.util.ArrayList;
import java.util.List;

import csi.security.CsiSecurityManager;
import csi.security.queries.AclRequest;
import csi.server.business.helper.ModelHelper;
import csi.server.business.service.annotation.Operation;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.themes.Theme;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.visualization.graph.RelGraphViewDef;
import csi.server.common.service.api.ThemeActionsServiceProtocol;
import csi.server.dao.CsiPersistenceManager;

public class ThemeActionsService implements ThemeActionsServiceProtocol {
    private static final String VISUALIZATION_TYPE_NAME = "visualizationType";

    @Operation
    public static List<ResourceBasics> listAuthorizedUserThemes(AclControlType permissionsIn) throws CentrifugeException {
        List<ResourceBasics> myResults = AclRequest.listAuthorizedUserThemes(new AclControlType[]{permissionsIn});

        return (null != myResults) ? myResults : new ArrayList<ResourceBasics>();
    }

    @Operation
    public static List<ResourceBasics> listAuthorizedUserGraphThemes(AclControlType permissionsIn) throws CentrifugeException {
        List<ResourceBasics> myResults = AclRequest.listAuthorizedUserGraphThemes(new AclControlType[]{permissionsIn});

        return (null != myResults) ? myResults : new ArrayList<ResourceBasics>();
    }

    @Operation
    public static List<ResourceBasics> listAuthorizedUserMapThemes(AclControlType permissionsIn) throws CentrifugeException {
        List<ResourceBasics> myResults = AclRequest.listAuthorizedUserMapThemes(new AclControlType[]{permissionsIn});

        return (null != myResults) ? myResults : new ArrayList<ResourceBasics>();
    }

    @Operation
    public List<List<ResourceBasics>> getThemeOverWriteControlLists() throws CentrifugeException {

        List<ResourceBasics> mySelectionList = AclRequest.listAuthorizedUserThemes(new AclControlType[]{AclControlType.DELETE});
        List<ResourceBasics> myRejectionList = AclRequest.listUserThemes();
        List<ResourceBasics> myConflictList = AclRequest.listAuthorizedUserThemes(new AclControlType[]{AclControlType.DELETE});

        return ModelHelper.generateOverWriteControlLists(mySelectionList, myRejectionList, myConflictList);
    }

    @Override
    public void deleteTheme(String uuid) throws CentrifugeException{
        Theme theme = CsiPersistenceManager.findObject(Theme.class, uuid);

        if(theme != null) {
         CsiPersistenceManager.deleteObject(theme);
      }
    }

    @Override
    public void saveTheme(Theme theme) throws CentrifugeException {
        Theme existingTheme = CsiPersistenceManager.findObject(Theme.class, theme.getUuid());
        if(existingTheme != null){
            CsiPersistenceManager.merge(theme);
        } else {
            CsiPersistenceManager.persist(theme);
        }
    }


    @Override
    public List<ResourceBasics> listThemes() throws CentrifugeException {
        return listAuthorizedUserThemes(AclControlType.READ);
    }

    @Override
    public Theme findTheme(String uuid) {
        if((uuid == null) || uuid.isEmpty()){
            return null;
        }

        return CsiPersistenceManager.findObject(Theme.class, uuid);

    }

    @Override
    public MapTheme findMapTheme(String uuid){
        if((uuid == null) || uuid.isEmpty()){
            return null;
        }

        if(checkAuthorization(uuid)){
            return CsiPersistenceManager.findObject(MapTheme.class, uuid);
        }
        return null;
    }

    /**
     * returns false on not authorized and if the object is null
     *
     * @param uuid
     * @return
     */
   public static boolean checkAuthorization(String uuid) {
      return CsiSecurityManager.isAuthorized(uuid, AclControlType.READ);
   }

    @Override
    public GraphTheme findGraphTheme(String uuid){
        if((uuid == null) || uuid.isEmpty()){
            return null;
        }


        if(checkAuthorization(uuid)){
            return CsiPersistenceManager.findObject(GraphTheme.class, uuid);
        }
        return null;
    }

    @Override
    public List<ResourceBasics> listThemesByType(VisualizationType type) throws CentrifugeException{

        switch(type){
            case RELGRAPH: return listAuthorizedUserGraphThemes(AclControlType.READ);
            case RELGRAPH_V2: return listAuthorizedUserGraphThemes(AclControlType.READ);
            case GEOSPATIAL_V2: return listAuthorizedUserMapThemes(AclControlType.READ);
            default: return null;
        }
    }

    @Override
    public void deleteThemes(List<String> myItemList) throws CentrifugeException {
        for(String uuid: myItemList){
            deleteTheme(uuid);
        }
    }

    public static void updateOptionSet(RelGraphViewDef rgDef) throws CentrifugeException {
        if((rgDef.getThemeUuid() == null) && (rgDef.getOptionSetName() != null) && (rgDef.getOptionSetName().length() > 0)){
            String optionSetName = rgDef.getOptionSetName();
            if(optionSetName.equals("Circular")){
                optionSetName = "Graph-Circular";
            } else if(optionSetName.equals("Baseline")){
                optionSetName = "Graph-Baseline";
            }
            List<ResourceBasics> resources = ThemeActionsService.listAuthorizedUserGraphThemes(AclControlType.READ);
            for(ResourceBasics resource: resources){
                if(resource.getName().equals(optionSetName)){
                    rgDef.setThemeUuid(resource.getUuid());
                    rgDef.setOptionSetName(null);
                    CsiPersistenceManager.merge(rgDef);
                    break;
                }
            }
        }
    }

//    public static List<Theme> retrieveItems(Set<String> uuids, VisualizationType type) {
//
//        EntityManager em = CsiPersistenceManager.getMetaEntityManager();
//        CriteriaBuilder cb = em.getCriteriaBuilder();
//        CriteriaQuery<Theme> q = cb.createQuery(Theme.class);
//        Root<Theme> root = q.from(Theme.class);
//        Predicate vizEqual = cb.equal(root.get(VISUALIZATION_TYPE_NAME), type.toString());
//
//        ParameterExpression<List> p = cb.parameter(List.class);
//
//        q.select(root).where(root.get(CsiPersistenceManager.PRIMARY_KEY_NAME).in(uuids), vizEqual);
//
//        TypedQuery<Theme> query = em.createQuery(q);
//
//        List<Theme> results = query.getResultList();
//        return results;
//    }

}
