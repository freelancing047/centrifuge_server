/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.server.dao.jpa.xml;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;

import com.google.common.base.Objects;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class SerializedXMLType implements UserType {

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        String xml = fromObject(value);
        return fromString(xml);
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) deepCopy(value);
    }

    @Override
    public boolean equals(Object o1, Object o2) throws HibernateException {
        return Objects.equal(o1, o2);
    }

    @Override
    public int hashCode(Object instance) throws HibernateException {
        return instance.hashCode();
    }
/*
    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        //FIXME: class not currently used
        return null;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        //FIXME: class not currently used
    }
    */


    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        String xml = (String) StandardBasicTypes.STRING.nullSafeGet(rs, names[0],session,owner);
        if (xml != null) {
            return fromString(xml);
        } else {
            return null;
        }
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            StandardBasicTypes.STRING.nullSafeSet(st, null, index,session);
        } else {
            StandardBasicTypes.STRING.nullSafeSet(st, fromObject(value), index, session);
        }
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }

    @Override
    public Class<?> returnedClass() {
        return null;
    }

    @Override
    public int[] sqlTypes() {
        return new int[] { Types.VARCHAR };
    }

    private static String fromObject(Object instance) {
        String retVal = XMLSerializedEntityXStreamFactory.get().toXML(instance);
        return retVal;
    }

    private static Object fromString(String xml) {
        return XMLSerializedEntityXStreamFactory.get().fromXML(xml);
    }
}
