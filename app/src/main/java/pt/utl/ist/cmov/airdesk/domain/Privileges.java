package pt.utl.ist.cmov.airdesk.domain;

public class Privileges {

	/**
	 * True if a user can read a file; false otherwise
	 */
	private boolean read;

	/**
	 * True if a user can write to a file; false otherwise
	 */
	private boolean write;

	/**
	 * True if a user can create a file; false otherwise
	 */
	private boolean create;

	/**
	 * True if a user can delete a file; false otherwise
	 */
	private boolean delete;

	public Privileges() {}

	public Privileges(boolean read, boolean write, boolean create, boolean delete) {
		this.read = read;
		this.write = write;
		this.create = create;
		this.delete = delete;
	}

	public boolean canRead() {
		return this.read;
	}

	public boolean canWrite() {
		return this.write;
	}

	public boolean canCreate() {
		return this.create;
	}

	public boolean canDelete() {
		return this.delete;
	}

	public void setReadPrivilege(boolean read) {
		this.read = read;
	}

	public void setWritePrivilege(boolean write) {
		this.write = write;
	}

	public void setCreatePrivilege(boolean create) {
		this.create = create;
	}

	public void setDeletePrivilege(boolean delete) {
		this.delete = delete;
	}

	public void setAll(boolean accessPrivilege) {
		setReadPrivilege(accessPrivilege);
		setWritePrivilege(accessPrivilege);
		setCreatePrivilege(accessPrivilege);
		setDeletePrivilege(accessPrivilege);
	}
}
