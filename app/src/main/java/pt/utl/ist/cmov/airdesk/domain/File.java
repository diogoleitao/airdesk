package pt.utl.ist.cmov.airdesk.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

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

    private ArrayList<Observer> observers;

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

    public ArrayList<Observer> getObservers() {
        return observers;
    }

    public void setObservers(ArrayList<Observer> observers) {
        this.observers = observers;
    }

	public void save(String content) {
		this.setContent(content);
        this.size = this.content.length() * 4;
	}

    @Override
    public void register(Observer o) {
        this.getObservers().add(o);
    }

    @Override
    public void unregister(Observer o) {
        this.getObservers().remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer o : this.getObservers())
            o.notify();
    }

    @Override
    public Object getUpdate(Observer o) {
        //TODO
        return null;
    }
}
