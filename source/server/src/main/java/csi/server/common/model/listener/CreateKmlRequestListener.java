package csi.server.common.model.listener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.common.dto.CreateKmlRequest;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.dao.CsiPersistenceManager;
import csi.startup.CleanUpThread;

/**
 * Created by centrifuge on 4/17/2017.
 */
public class CreateKmlRequestListener {
   private static final Logger LOG = LogManager.getLogger(CreateKmlRequestListener.class);

   private static final Pattern PIPE_PATTERN = Pattern.compile("|");

    @PostPersist
    public void onPostPersist(CreateKmlRequest parentIn) {


        // Capture set of current FieldDef ids
//        parentIn.setFieldDefIds(buildUntetheredFieldDefString(buildUntetheredFieldDefSet(parentIn)));
    }

    @PostLoad
    public void onPostLoad(CreateKmlRequest parentIn) {

        // Capture set of current FieldDef ids
//        parentIn.setFieldDefIds(buildUntetheredFieldDefString(buildUntetheredFieldDefSet(parentIn)));
    }

    @PostUpdate
    public void onPostUpdate(CreateKmlRequest parentIn) {

        // Capture updated set of current FieldDef ids
//        Set<String> newFieldDefList = buildUntetheredFieldDefSet(parentIn);

        // Remove lingering FieldDefs
//        removeLingeringFieldDefs(parentIn, newFieldDefList);
    }

    @PostRemove
    public void onPostRemove(CreateKmlRequest parentIn) {

        // Remove lingering FieldDefs
//        removeLingeringFieldDefs(parentIn);
    }

   private String buildUntetheredFieldDefString(Set<String> setIn) {
      return setIn.stream().collect(Collectors.joining("|"));
   }

    private Set<String> buildUntetheredFieldDefSet(CreateKmlRequest parentIn) {

        Set<String> mySet = new HashSet<>();

        try {
            String myDataViewUuid = parentIn.getDataviewUuid();
            DataView myDataView = (null != myDataViewUuid)
                                        ? CsiPersistenceManager.findObject(DataView.class, myDataViewUuid)
                                        : null;
            FieldListAccess myDataModel = (null != myDataView)
                                            ? myDataView.getMeta().getModelDef().getFieldListAccess() : null;
            List<FieldDef> myBaseList = parentIn.getFields();

            // Capture set of current FieldDef ids
            for (FieldDef myFieldDef : myBaseList) {

                String myUuid = (null != myFieldDef) ? myFieldDef.getUuid() : null;

                if ((null != myUuid) && (!isRegistered(myDataModel, myUuid))) {

                    mySet.add(myUuid);
                }
            }

        } catch (Exception myException) {

            LOG.error("Failed removing lingering FieldDefs from KML request.", myException);
        }
        return mySet.isEmpty() ? null : mySet;
    }

    private boolean isRegistered(FieldListAccess dataModelIn, String uuidIn) {

        boolean myRegistrationFlag = false;

        if ((null != dataModelIn) && (null != uuidIn)) {

            myRegistrationFlag = (null != dataModelIn.getFieldDefByUuid(uuidIn));
        }
        return myRegistrationFlag;
    }

   private void removeLingeringFieldDefs(CreateKmlRequest parentIn) {
      String myConcatenatedIds = parentIn.getFieldDefIds();
      String[] myArray = (myConcatenatedIds == null) ? null : PIPE_PATTERN.split(myConcatenatedIds);

      if (myArray != null) {
         for (int i = 0; i < myArray.length; i++) {
            String myId = myArray[i];

            if ((null != myId) && (0 < myId.length())) {
               CleanUpThread.scheduleDelete(FieldDef.class, myId);
            }
         }
      }
   }

   private void removeLingeringFieldDefs(CreateKmlRequest parentIn, Set<String> newFieldDefList) {
      if ((newFieldDefList != null) && !newFieldDefList.isEmpty()) {
         String myConcatenatedIds = parentIn.getFieldDefIds();
         String[] myArray = (myConcatenatedIds == null) ? null : PIPE_PATTERN.split(myConcatenatedIds);

         if (myArray != null) {
            for (int i = 0; i < myArray.length; i++) {
               String myId = myArray[i];

               if ((null != myId) && (0 < myId.length()) && !newFieldDefList.contains(myId)) {
                  CleanUpThread.scheduleDelete(FieldDef.class, myId);
               }
            }
         }
      } else {
         removeLingeringFieldDefs(parentIn);
      }
   }
}
