package pt.utl.ist.cmov.airdesk.domain.exceptions;

/**
 * Created by Diogo on 07/04/2015.
 */
public class FileAlreadyExistsException extends Exception {

    private static String message = "File already exists!";

    public FileAlreadyExistsException() {
        super(message);
    }
}
