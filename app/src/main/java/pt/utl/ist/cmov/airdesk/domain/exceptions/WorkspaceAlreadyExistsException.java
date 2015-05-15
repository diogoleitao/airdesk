package pt.utl.ist.cmov.airdesk.domain.exceptions;

public class WorkspaceAlreadyExistsException extends Exception {
	private static String message = "Workspace name already exists!";

	public WorkspaceAlreadyExistsException() {
		super(message);
	}
}
