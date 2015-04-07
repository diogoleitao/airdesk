package pt.utl.ist.cmov.airdesk.domain.exceptions;

/**
 * Created by Diogo on 07/04/2015.
 */
public class UserDoesNotExistException extends Exception {

    private static String message = "The user isn't registered/does not exist!";

    public UserDoesNotExistException() {
        super(message);
    }
}
