package pt.utl.ist.cmov.airdesk.domain.exceptions;

/**
 * Created by Diogo on 06/05/2015.
 */
public class ConcurrentFileEditingException extends Exception {

    private static String message = "Another user is already editing the file!";

    public ConcurrentFileEditingException() {
        super(message);
    }
}
