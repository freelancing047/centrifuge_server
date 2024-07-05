package csi.server.common.service.api;

import java.util.List;

import csi.server.common.dto.user.preferences.ResourceFilter;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.icons.Icon;
import csi.server.common.model.icons.IconResultDto;
import csi.shared.core.icon.IconInfoDto;
import csi.shared.gwt.vortex.VortexService;

public interface IconActionsServiceProtocol extends VortexService {

    /**
     * Uploads a base64 icon to database
     * 
     * @param base64
     * @param fileName 
     * @return string representation of uuid
     * @throws CentrifugeException 
     */
    String uploadIcon(String base64, String fileName) throws CentrifugeException;

    /**
     * @param iconId - uuid of icon
     * @return base64 representation of icon
     * @throws CentrifugeException
     */
    String getBase64Image(String iconId) throws CentrifugeException;
    
    /**
     * @param uuid - icon uuid
     * @return dataUrl for access to an icon via servlet
     * @throws CentrifugeException
     */
    String getDataUrlImage(String uuid) throws CentrifugeException;

    String countIcons(String queryText) throws CentrifugeException;

    Icon getIcon(String uuid) throws CentrifugeException;
    
    List<String> listAvailableTags() throws CentrifugeException;

    void addTagsToIcons(List<String> ids, List<String> tags) throws CentrifugeException;

    void addTag(String iconUuid, List<String> items) throws CentrifugeException;

    void removeTag(String iconUuid, String string) throws CentrifugeException;

    IconResultDto listIcons(Integer start, Integer end, String text, Integer totalCount, Integer lastDbRow, ResourceFilter resFilter) throws CentrifugeException;

    String editIconData(String base64, String uuid) throws CentrifugeException;

    void deleteIcon(String iconUuid) throws CentrifugeException;
    void deleteIcons(List<String> iconUuids) throws CentrifugeException;

    boolean hasIconManagementAccess();

    IconInfoDto getIconInfo(String tag) throws CentrifugeException;

    IconInfoDto getIconInfo(ResourceFilter tag) throws CentrifugeException;

    IconInfoDto getIconInfo(ResourceFilter tag, String tagText) throws CentrifugeException;

    List<Icon> filterIcons(String override, ResourceFilter filter, String tag);

    void editName(String text, String iconUuid);

}
