package pt.utl.ist.cmov.airdesk.domain.exceptions;

/**
 * Created by Diogo on 25/03/2015.
 */
public class WorkspaceQuotaReachedException extends Exception {
    private static String message = "Workspace quota limit reached!";

    public WorkspaceQuotaReachedException() {
        super(message);
    }
}
