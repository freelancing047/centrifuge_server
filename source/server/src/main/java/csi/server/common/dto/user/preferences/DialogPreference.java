package csi.server.common.dto.user.preferences;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Created by centrifuge on 5/20/2016.
 */
@Entity
@Table(
        name="dialogpreference",
        indexes = {
                @Index(columnList = "logonid", name = "idx_dialogpreference_logonid", unique = false)
        })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DialogPreference implements IsSerializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String logonId;
    private String dialogKey;
    private String dataKey;
    private String dataValue;

    public DialogPreference() {
    }

    public DialogPreference(DialogPreference preferenceIn) {

        copy(preferenceIn);
    }

    public DialogPreference(String logonIdIn, String dialogKeyIn, String dataKeyIn, String dataValueIn) {

        logonId = logonIdIn;
        dialogKey = dialogKeyIn;
        dataKey = dataKeyIn;
        dataValue = dataValueIn;
    }

    public void setId(Long filterIdIn) {

        id = filterIdIn;
    }

    public Long getId() {
        return id;
    }

    public void setLogonId(String logonIdIn) {

        logonId = logonIdIn;
    }

    public String getLogonId() {

        return logonId;
    }

    public void setDialogKey(String dialogKeyIn) {

        dialogKey = dialogKeyIn;
    }

    public String getDialogKey() {

        return dialogKey;
    }

    public void setDataKey(String dataKeyIn) {

        dataKey = dataKeyIn;
    }

    public String getDataKey() {

        return dataKey;
    }

    public void setDataValue(String dataValueIn) {

        dataValue = dataValueIn;
    }

    public String getDataValue() {

        return dataValue;
    }

    public DialogPreference copy(DialogPreference preferenceIn) {

        logonId = preferenceIn.getLogonId();
        dialogKey = preferenceIn.getDialogKey();
        dataKey = preferenceIn.getDataKey();
        dataValue = preferenceIn.getDataValue();

        return this;
    }
}
