package pt.utl.ist.cmov.airdesk.domain;

/**
 * Created by Diogo on 05/05/2015.
 */
public interface Subject {

    void register(Observer o);
    void unregister(Observer o);
    void notifyObservers();
    Object getUpdate(Observer o);
}
