package pt.utl.ist.cmov.airdesk.domain.exceptions;

public class WorkspaceQuotaReachedException extends Exception {
    private static String message = "Workspace quota limit reached!";

    public WorkspaceQuotaReachedException() {
        super(message);
    }
}
