package pt.utl.ist.cmov.airdesk.domain.exceptions;

public class FileAlreadyExistsException extends Exception {

    private static String message = "File already exists!";

    public FileAlreadyExistsException() {
        super(message);
    }
}
