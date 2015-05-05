package pt.utl.ist.cmov.airdesk.domain;

/**
 * Created by Diogo on 05/05/2015.
 */
public interface Observer {

    void update();
    void setSubject(Subject s);
}
