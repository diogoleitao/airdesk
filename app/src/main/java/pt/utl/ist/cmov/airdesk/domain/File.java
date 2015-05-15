package pt.utl.ist.cmov.airdesk.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Diogo on 25/03/2015.
 */
public class File implements Serializable {

	/**
	 * The name of the file
	 */
	private String name;

	/**
	 * A string representing the content of the file
	 */
	private String content;

    /**
     * The size of the file in kB (each char is 4kB)
     */
    private int size;

    /**
     * The timestamp of the file's last edit
     */
    private Date timestamp;

    public boolean open() {
        boolean wasOpen = open;
        this.open=true;
        return wasOpen;
    }

    public void close() {
        this.open = false;
    }

    boolean open = false;

    public File(String name) {
        this.setName(name);
        this.setContent("");
        this.setSize(0);
    }

	public String getName() {
		return this.name;
	}

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
        this.timestamp = Calendar.getInstance().getTime();
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

	public void save(String content) {
		this.setContent(content);
        this.setSize(this.getContent().length() * 4);
        this.timestamp = Calendar.getInstance().getTime();
	}

    public Date getTimestamp() {
        return timestamp;
    }

}
