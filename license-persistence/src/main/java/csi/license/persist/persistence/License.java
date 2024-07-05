package csi.license.persist.persistence;

import java.io.Serializable;
import java.util.Date;

/*
 * README: If you make any changes that affects the serialized image of
 * this class, you need to ensure that this does not break the
 * SignedLicenseReader!!
 *
 * The SignedLicenseReader relies on the serialized format of LicensePayload
 * and this class.  Changes to the serialized image will cause any attempts
 * to use old licenses to fail.
 */
@Deprecated
public class License implements Serializable {



    /*
     * TODO: update this to properly handle versioning?  If we will indeed version
     * the license this field needs to be updated accordingly.
     */
    private static final long serialVersionUID = 3251145079551417197L;

    private String _customer;
    private int _ver_major;
    private int _ver_minor;
    private int _ver_label;
    private int _user_count;
    private boolean _internal;
    private boolean _expiring; // expiring or permanent license
    private Date _expiration_date;
    private boolean _node_lock; // future

    public int get_user_count() {
        return _user_count;
    }

    public void set_user_count(final int _user_count) {
        this._user_count = _user_count;
    }

    public Date get_expiration_date() {
        return _expiration_date;
    }

    public void set_expiration_date(final Date _expiration_date) {
        this._expiration_date = _expiration_date;
    }

    public boolean get_node_lock() {
        return _node_lock;
    }

    public void set_node_lock(final boolean _node_lock) {
        this._node_lock = _node_lock;
    }

    public int get_ver_major() {
        return _ver_major;
    }

    public int get_ver_minor() {
        return _ver_minor;
    }

    public void set_ver_minor(final int _ver_minor) {
        this._ver_minor = _ver_minor;
    }

    public int get_ver_label() {
        return _ver_label;
    }

    public void set_ver_label(final int _ver_label) {
        this._ver_label = _ver_label;
    }

    public String get_customer() {
        return ((_customer != null) && (_customer.trim().length() > 0)) ? _customer : "";
    }

    public void set_customer(final String _customer) {
        this._customer = _customer;
    }

    public boolean get_internal() {
        return _internal;
    }

    public void set_internal(final boolean _internal) {
        this._internal = _internal;
    }

    public boolean is_expiring() {
        return _expiring;
    }

    public void set_expiring(final boolean _expiring) {
        this._expiring = _expiring;
    }

    public void set_ver_major(final int _ver_major) {
        this._ver_major = _ver_major;
    }

    @Override
    public String toString()
    {
        return "License [_customer=" + _customer + ", _ver_major=" + _ver_major + ", _ver_minor=" + _ver_minor
                + ", _ver_label=" + _ver_label + ", _user_count=" + _user_count + ", _internal=" + _internal
                + ", _expiring=" + _expiring + ", _expiration_date=" + _expiration_date + ", _node_lock=" + _node_lock
                + "]";
    }
}
