package csi.server.common.service.api;

import java.util.List;

import csi.server.common.dto.resource.ResourceIdentifyingDTO;
import csi.server.common.dto.SelectionListData.SelectorBasics;
import csi.shared.gwt.vortex.VortexService;

public interface UserFileActionsServiceProtocol extends VortexService {

    public List<List<SelectorBasics>> getFileOverWriteControlLists(String subPathIn, List<String> extensionListIn);

    public Integer getMaxUploadSize();

}
