package pt.utl.ist.airdesk.domain;

/**
 * Created by Diogo on 18/03/2015.
 */
public class Privileges {
	private boolean read;
	private boolean write;
	private boolean create;
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
}
