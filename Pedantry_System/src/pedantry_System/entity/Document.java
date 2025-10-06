package pedantry_System.entity;

import java.sql.Timestamp;

public class Document {
    private int id;
    private String title;
    private String content;  // HTML as text
    private int uploaderId;
    private Timestamp uploadDate;

    // Constructors, getters, setters
    public Document() {}

    public Document(int id, String title, String content, int uploaderId, Timestamp uploadDate) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.uploaderId = uploaderId;
        this.uploadDate = uploadDate;
    }

    // Getters and setters...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getUploaderId() { return uploaderId; }
    public void setUploaderId(int uploaderId) { this.uploaderId = uploaderId; }
    public Timestamp getUploadDate() { return uploadDate; }
    public void setUploadDate(Timestamp uploadDate) { this.uploadDate = uploadDate; }
}
