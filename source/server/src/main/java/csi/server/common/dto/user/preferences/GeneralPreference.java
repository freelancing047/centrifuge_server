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
 * Created by centrifuge on 11/22/2016.
 */
@Entity
@Table(
        name="generalpreference",
        indexes = {
                @Index(columnList = "logonid", name = "idx_generalpreference_logonid", unique = false)
        })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class GeneralPreference implements IsSerializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String logonId;
    private String dataKey;
    private String dataValue;

    public GeneralPreference() {
    }

    public GeneralPreference(String logonIdIn, String dataKeyIn, String dataValueIn) {

        logonId = logonIdIn;
        dataKey = dataKeyIn;
        dataValue = dataValueIn;
    }

    public GeneralPreference(GeneralPreference preferenceIn) {

        copy(preferenceIn);
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

    public GeneralPreference copy(GeneralPreference preferenceIn) {

        logonId = preferenceIn.getLogonId();
        dataKey = preferenceIn.getDataKey();
        dataValue = preferenceIn.getDataValue();

        return this;
    }
}
