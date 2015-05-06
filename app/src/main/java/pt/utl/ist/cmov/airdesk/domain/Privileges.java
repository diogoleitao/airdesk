package pt.utl.ist.cmov.airdesk.domain;

import java.io.Serializable;

public class Privileges implements Serializable {

	/**
	 * True if AirdeskBroadcastReceiver user can read AirdeskBroadcastReceiver file; false otherwise
	 */
	private boolean read;

	/**
	 * True if AirdeskBroadcastReceiver user can write to AirdeskBroadcastReceiver file; false otherwise
	 */
	private boolean write;

	/**
	 * True if AirdeskBroadcastReceiver user can create AirdeskBroadcastReceiver file; false otherwise
	 */
	private boolean create = false;

	/**
	 * True if AirdeskBroadcastReceiver user can delete AirdeskBroadcastReceiver file; false otherwise
	 */
	private boolean delete = false;

	public Privileges() {
        this.read = true;
        this.write = true;
    }

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

	public void setAll(boolean[] privileges) {
		setReadPrivilege(privileges[0]);
		setWritePrivilege(privileges[1]);
		setCreatePrivilege(privileges[2]);
		setDeletePrivilege(privileges[3]);
	}

    public boolean[] getAll() {
        boolean[] privileges = new boolean[4];
        privileges[0] = canRead();
        privileges[1] = canWrite();
        privileges[2] = canCreate();
        privileges[3] = canDelete();

        return privileges;
    }
}
