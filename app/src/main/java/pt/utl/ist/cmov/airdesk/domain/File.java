package pt.utl.ist.cmov.airdesk.domain;

/**
 * Created by Diogo on 25/03/2015.
 */
public class File {
    private String name;
    private String content = "";

    public File() {}

    public File(String name){
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void edit(String content) {
        this.content += content;
    }

    public void delete(String content) {
        this.content.charAt(content.charAt(0));
    }
}
