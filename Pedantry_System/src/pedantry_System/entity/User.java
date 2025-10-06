package pedantry_System.entity;

public class User {
    private int id;
    private String username;
    private String password;
    private String email;
    private String role;
    private int numUploads;

    // Constructors, getters, setters
    public User() {}

    public User(int id, String username, String password, String email, String role, int numUploads) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.numUploads = numUploads;
    }

    // Getters and setters...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public int getNumUploads() { return numUploads; }
    public void setNumUploads(int numUploads) { this.numUploads = numUploads; }
}
