package pt.utl.ist.cmov.airdesk.domain.exceptions;

public class UserDoesNotHavePermissionsToCreateFilesException extends Exception {

    private static String message = "You don't have permissions to create files!";

    public UserDoesNotHavePermissionsToCreateFilesException() {
        super(message);
    }
}
