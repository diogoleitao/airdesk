package pt.utl.ist.cmov.airdesk.domain.exceptions;

/**
 * Created by Tiago on 08/04/2015.
 */
public class UserAlreadyHasPermissionsInWorkspaceException extends Exception {
    private static String message = "User already has permissions!";

    public UserAlreadyHasPermissionsInWorkspaceException() {
        super(message);
    }
}
