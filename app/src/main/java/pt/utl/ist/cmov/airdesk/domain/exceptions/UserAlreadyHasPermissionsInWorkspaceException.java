package pt.utl.ist.cmov.airdesk.domain.exceptions;

public class UserAlreadyHasPermissionsInWorkspaceException extends Exception {
    private static String message = "User already has permissions!";

    public UserAlreadyHasPermissionsInWorkspaceException() {
        super(message);
    }
}
