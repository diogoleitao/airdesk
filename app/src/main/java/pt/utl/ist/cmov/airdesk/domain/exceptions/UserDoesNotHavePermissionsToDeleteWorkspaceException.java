package pt.utl.ist.cmov.airdesk.domain.exceptions;

public class UserDoesNotHavePermissionsToDeleteWorkspaceException extends Exception {

    private static String message = "You don't have permissions to delete this workspace!";

    public UserDoesNotHavePermissionsToDeleteWorkspaceException() {
        super(message);
    }
}
