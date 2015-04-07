package pt.utl.ist.cmov.airdesk.domain.exceptions;

/**
 * Created by Diogo on 07/04/2015.
 */
public class UserDoesNotHavePermissionsToDeleteWorkspaceException extends Exception {

    private static String message = "You don't have permissions to delete this workspace!";

    public UserDoesNotHavePermissionsToDeleteWorkspaceException() {
        super(message);
    }
}
