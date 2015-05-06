package pt.utl.ist.cmov.airdesk.domain;

/**
 * Created by Diogo on 06/05/2015.
 */
public interface WorkspaceObserver {

    void update();
    void setSubject(Subject s);
}
