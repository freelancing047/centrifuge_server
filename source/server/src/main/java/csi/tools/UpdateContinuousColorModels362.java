package csi.tools;

import csi.server.common.model.visualization.matrix.MatrixSettings;
import csi.server.dao.CsiPersistenceManager;
import csi.shared.core.color.ContinuousColorModel;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import java.util.Collection;

public class UpdateContinuousColorModels362 implements CustomTaskChange {

    @Override
    public void execute(Database database) throws CustomChangeException {
        CsiPersistenceManager.begin();
        Collection<MatrixSettings> listOfSettings = CsiPersistenceManager.getMetaEntityManager().createQuery(
                "SELECT e from MatrixSettings e", MatrixSettings.class).getResultList();
        for(MatrixSettings settings : listOfSettings) {
            if(settings.getColorModel() instanceof ContinuousColorModel) {
                fix((ContinuousColorModel) settings.getColorModel());
            }
        }
        CsiPersistenceManager.commit();
        CsiPersistenceManager.close();
    }

    private void fix(ContinuousColorModel model) {
        int endX = model.getStartX();
        int endY = model.getStartY();
        int startX = model.getEndX();
        int startY = model.getEndY();
        model.setEndX(endX);
        model.setEndY(endY);
        model.setStartX(startX);
        model.setStartY(startY);
    }

    @Override
    public String getConfirmationMessage() { return null; }

    @Override
    public void setUp() throws SetupException { }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) { }

    @Override
    public ValidationErrors validate(Database database) { return null; }
}
