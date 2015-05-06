package pt.utl.ist.cmov.airdesk.domain;

/**
 * Created by Diogo on 05/05/2015.
 */
public interface Subject {

    void register(FileObserver o);
    void unregister(FileObserver o);
    void notifyObservers();
    Object getUpdate(FileObserver o);
}
