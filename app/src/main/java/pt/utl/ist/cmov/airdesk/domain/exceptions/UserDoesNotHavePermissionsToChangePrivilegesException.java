package pt.utl.ist.cmov.airdesk.domain.exceptions;

/**
 * Created by Tiago on 08/04/2015.
 */
public class UserDoesNotHavePermissionsToChangePrivilegesException extends Exception {

    private static String message = "Only the workspace owner can change privileges!";

    public UserDoesNotHavePermissionsToChangePrivilegesException(){
        super(message);
    }
}
