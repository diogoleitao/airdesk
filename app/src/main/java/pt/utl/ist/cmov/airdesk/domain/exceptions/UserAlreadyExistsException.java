package pt.utl.ist.cmov.airdesk.domain.exceptions;

/**
 * Created by Diogo on 25/03/2015.
 */
public class UserAlreadyExistsException extends Exception {
    private static String message = "User already exists";

    public UserAlreadyExistsException() {
        super(message);
    }
}
