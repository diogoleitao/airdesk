package pt.utl.ist.cmov.airdesk.domain.exceptions;

public class TopicAlreadyAddedException extends Exception {

    private static String message = "The workspace already has that topic!";

    public TopicAlreadyAddedException() {
        super(message);
    }
}
