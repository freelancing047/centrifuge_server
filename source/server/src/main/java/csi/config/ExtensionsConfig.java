package csi.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import csi.server.business.cachedb.DataSyncListener;
import csi.server.business.helper.DataCacheHelper;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.extension.Classification;
import csi.server.common.model.extension.Labels;

public class ExtensionsConfig extends AbstractConfigurationSettings {
    private boolean classificationRequired = false;
    private boolean allowClassificationUserInput = true;
    private boolean labelsRequired = false;
    private boolean allowLabelsUserInput = true;

    @Override
    public void validate() throws ConfigurationException, CentrifugeException {
        super.validate();

        DataCacheHelper helper = new DataCacheHelper();
        Set<DataSyncListener> listeners = helper.getSyncListeners();
        List<String> msgs = new ArrayList<String>();

        if (isClassificationRequired() && !isListenerPresent(listeners, Classification.NAME)) {
            msgs.add("Classification processing is marked required and is not configured.");
        }

        if (isLabelsRequired() && !isListenerPresent(listeners, Labels.NAME)) {
            msgs.add("Security Label processing is marked required and is not configured.");
        }

        if (!msgs.isEmpty()) {
            throw new ConfigurationException(msgs.stream().collect(Collectors.joining("\n")));
        }
    }

    private boolean isListenerPresent(Set<DataSyncListener> listeners, String name) {
        boolean isPresent = false;
        for (DataSyncListener listener : listeners) {
            if (listener.providesSupport(name)) {
                isPresent = true;
                break;
            }
        }

        return isPresent;
    }

    public boolean isClassificationRequired() {
        return classificationRequired;
    }

    public void setClassificationRequired(boolean classificationRequired) {
        this.classificationRequired = classificationRequired;
    }

    public boolean isAllowClassificationUserInput() {
        return allowClassificationUserInput;
    }

    public void setAllowClassificationUserInput(boolean allowClassificationUserInput) {
        this.allowClassificationUserInput = allowClassificationUserInput;
    }

    public boolean isLabelsRequired() {
        return labelsRequired;
    }

    public void setLabelsRequired(boolean labelsRequired) {
        this.labelsRequired = labelsRequired;
    }

    public boolean isAllowLabelsUserInput() {
        return allowLabelsUserInput;
    }

    public void setAllowLabelsUserInput(boolean allowLabelsUserInput) {
        this.allowLabelsUserInput = allowLabelsUserInput;
    }
}
