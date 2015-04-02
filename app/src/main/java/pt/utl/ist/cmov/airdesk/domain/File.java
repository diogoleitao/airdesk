package pt.utl.ist.cmov.airdesk.domain;

/**
 * Created by Diogo on 25/03/2015.
 */
public class File {

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

	public File() {}

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

	public void save(String content) {
		this.content = content;
        this.size = this.content.length() * 4;
	}
}
