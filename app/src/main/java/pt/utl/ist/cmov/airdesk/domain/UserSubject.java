package pt.utl.ist.cmov.airdesk.domain;

/**
 * Created by Diogo on 05/05/2015.
 */
public interface UserSubject {
    void register(Observer wo);

    void unregister(Observer fo);

    void notifyObservers();
}
