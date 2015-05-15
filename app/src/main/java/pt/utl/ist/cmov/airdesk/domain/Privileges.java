package pt.utl.ist.cmov.airdesk.domain;

import java.io.Serializable;

public class Privileges implements Serializable {

	private boolean read;

	private boolean write;

	private boolean create = false;

	private boolean delete = false;

	public Privileges() {
		this.setReadPrivilege(true);
		this.setWritePrivilege(true);
	}

	public Privileges(boolean read, boolean write, boolean create, boolean delete) {
		this.setReadPrivilege(read);
		this.setWritePrivilege(write);
		this.setCreatePrivilege(create);
		this.setDeletePrivilege(delete);
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

	public boolean[] getAll() {
		boolean[] privileges = new boolean[4];
		privileges[0] = canRead();
		privileges[1] = canWrite();
		privileges[2] = canCreate();
		privileges[3] = canDelete();

		return privileges;
	}

	public void setAll(boolean[] privileges) {
		setReadPrivilege(privileges[0]);
		setWritePrivilege(privileges[1]);
		setCreatePrivilege(privileges[2]);
		setDeletePrivilege(privileges[3]);
	}
}
