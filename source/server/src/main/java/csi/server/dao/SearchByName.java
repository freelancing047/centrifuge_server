package csi.server.dao;

public interface SearchByName<T> {

    T findByName(String name);

}
