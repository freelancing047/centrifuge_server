package csi.server.dao.jpa;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import csi.server.dao.CsiPersistenceManager;
import csi.server.dao.GenericDAO;

public abstract class AbstractJPADAO<T, ID extends Serializable> implements GenericDAO<T, ID> {

    protected Class<T> beanType;
    protected BeanInfo beanInfo;

    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public AbstractJPADAO() {
        beanType = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        try {
            beanInfo = Introspector.getBeanInfo(beanType, Object.class);
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
    }

    public AbstractJPADAO(EntityManager entityManager) {
        super();

        this.entityManager = entityManager;
    }

    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        EntityManager entityManager = getEntityManager();
        List<T> resultList = entityManager.createQuery("from " + beanType.getName()).getResultList();
        return resultList;
    }

    public T findById(ID id, boolean lock) {
        EntityManager entityManager = getEntityManager();
        T entity = entityManager.find(beanType, id);
        if (lock) {
            entityManager.lock(entity, LockModeType.WRITE);
        }

        return entity;
    }

    public void makePersistent(T entity) {
        EntityManager entityManager = getEntityManager();
        entityManager.persist(entity);
    }

    public T merge(T entity) {
        EntityManager entityManager = getEntityManager();
        T updated = entityManager.merge(entity);

        return updated;
    }

    /**
     * "Make transient" -- kick its butt out of the DB.
     */
    public void makeTransient(T entity) {
        EntityManager entityManager = getEntityManager();
        entityManager.remove(entity);
    }

    public void flush() {
        EntityManager entityManager = getEntityManager();
        entityManager.flush();
    }

    protected EntityManager getEntityManager() {
        if (entityManager == null) {
            entityManager = CsiPersistenceManager.getMetaEntityManager();
        }

        return entityManager;
    }
}
