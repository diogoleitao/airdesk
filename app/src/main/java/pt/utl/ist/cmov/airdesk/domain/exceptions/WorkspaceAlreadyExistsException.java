package pt.utl.ist.cmov.airdesk.domain.exceptions;

/**
 * Created by Diogo on 25/03/2015.
 */
public class WorkspaceAlreadyExistsException extends Exception {
	private static String message = "Workspace name already exists";

	public WorkspaceAlreadyExistsException() {
		super(message);
	}
}
