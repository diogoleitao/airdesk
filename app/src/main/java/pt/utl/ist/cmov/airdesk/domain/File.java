package pt.utl.ist.cmov.airdesk.domain;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Diogo on 25/03/2015.
 */
public class File implements Serializable, Subject {

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

    private ArrayList<FileObserver> fileObservers;

    public File(String name) {
		this.name = name;
		this.content = "";
        this.size = 0;
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
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public ArrayList<FileObserver> getFileObservers() {
        return fileObservers;
    }

	public void save(String content) {
		this.setContent(content);
        this.setSize(this.getContent().length() * 4);
        notifyObservers();
	}

    @Override
    public void register(FileObserver fo) {
        this.getFileObservers().add(fo);
    }

    @Override
    public void unregister(FileObserver fo) {
        this.getFileObservers().remove(fo);
    }

    @Override
    public void notifyObservers() {
        for (FileObserver fo : this.getFileObservers())
            fo.update();
    }

    @Override
    public Object getUpdate(FileObserver fo) {
        //TODO
        return null;
    }
}
