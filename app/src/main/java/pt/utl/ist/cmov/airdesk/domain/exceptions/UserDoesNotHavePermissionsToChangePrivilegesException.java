package pt.utl.ist.cmov.airdesk.domain.exceptions;

public class UserDoesNotHavePermissionsToChangePrivilegesException extends Exception {

    private static String message = "Only the workspace owner can change privileges!";

    public UserDoesNotHavePermissionsToChangePrivilegesException(){
        super(message);
    }
}
