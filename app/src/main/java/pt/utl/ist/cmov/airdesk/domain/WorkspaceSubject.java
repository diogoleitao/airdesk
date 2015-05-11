package pt.utl.ist.cmov.airdesk.domain;

/**
 * Created by Diogo on 11/05/2015.
 */
public interface WorkspaceSubject {
    void register(Observer fo);

    void unregister(Observer fo);

    void notifyObservers();
}
