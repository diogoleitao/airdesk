package pt.utl.ist.cmov.airdesk.domain.exceptions;

/**
 * Created by Diogo on 08/04/2015.
 */
public class TopicAlreadyAddedException extends Exception {

    private static String message = "The workspace already has that topic!";

    public TopicAlreadyAddedException() {
        super(message);
    }
}
