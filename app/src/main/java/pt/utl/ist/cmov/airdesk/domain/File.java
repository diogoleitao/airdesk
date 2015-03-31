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

	public File() {}

	public File(String name) {
		this.name = name;
		this.content = "";
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
        if (this.content.equals("")) {
            this.content = content;
        } else {
            this.content += content;
        }
    }

	public void save(String content) {
		this.content = content;
	}

	public String delete() {
        this.content = this.content.substring(0, this.content.length() - 1);
        return this.content;
	}
}
