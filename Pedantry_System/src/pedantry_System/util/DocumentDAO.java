package pedantry_System.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import PedantrySystem.dao.DatabaseConnection;
import pedantry_System.entity.Document;
import pedantry_System.entity.User;

public class DocumentDAO {
    private DatabaseConnection db;
    private UserDAO userDAO;

    public DocumentDAO(DatabaseConnection db) {
        this.db = db;
        this.userDAO = new UserDAO(db);
    }

    // Search documents by keyword (title or content)
    public List<Document> search(String keyword) {
        List<Document> docs = new ArrayList<>();
        String sql = "SELECT * FROM documents WHERE LOWER(title) LIKE LOWER(?) OR LOWER(content) LIKE LOWER(?) ORDER BY upload_date DESC";
        try (PreparedStatement pstmt = db.getConnection().prepareStatement(sql)) {
            String likePattern = "%" + keyword + "%";
            pstmt.setString(1, likePattern);
            pstmt.setString(2, likePattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    docs.add(new Document(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getInt("uploader_id"),
                        rs.getTimestamp("upload_date")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Search error: " + e.getMessage());
        }
        return docs;
    }

    // Get all documents
    public List<Document> getAll() {
        List<Document> docs = new ArrayList<>();
        String sql = "SELECT * FROM documents ORDER BY upload_date DESC";
        try (Statement stmt = db.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                docs.add(new Document(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getInt("uploader_id"),
                    rs.getTimestamp("upload_date")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Get all documents error: " + e.getMessage());
        }
        return docs;
    }

    // Get documents by user ID (for reports)
    public List<Document> getByUser (int userId) {
        List<Document> docs = new ArrayList<>();
        String sql = "SELECT * FROM documents WHERE uploader_id = ? ORDER BY upload_date DESC";
        try (PreparedStatement pstmt = db.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    docs.add(new Document(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getInt("uploader_id"),
                        rs.getTimestamp("upload_date")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Get user documents error: " + e.getMessage());
        }
        return docs;
    }

    // Create/Upload document (updates user num_uploads)
    public boolean create(Document doc) {
        String sql = "INSERT INTO documents (title, content, uploader_id) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, doc.getTitle());
            pstmt.setString(2, doc.getContent());
            pstmt.setInt(3, doc.getUploaderId());
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                // Get generated ID if needed (not used here)
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        doc.setId(generatedKeys.getInt(1));
                    }
                }
                // Update user's num_uploads
                User user = userDAO.getById(doc.getUploaderId());
                if (user != null) {
                    user.setNumUploads(user.getNumUploads() + 1);
                    userDAO.update(user);
                }
                simulateEmailAlert("New document uploaded: " + doc.getTitle() + " by " + doc.getUploaderId());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Create document error: " + e.getMessage());
        }
        return false;
    }

    // Get document by ID (for download/view)
    public Document getById(int id) {
        String sql = "SELECT * FROM documents WHERE id = ?";
        try (PreparedStatement pstmt = db.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Document(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getInt("uploader_id"),
                        rs.getTimestamp("upload_date")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Get document by ID error: " + e.getMessage());
        }
        return null;
    }

    // Delete document (admin only; decrements user num_uploads if applicable)
    public boolean delete(int id) {
        Document doc = getById(id);
        if (doc == null) return false;

        String sql = "DELETE FROM documents WHERE id = ?";
        try (PreparedStatement pstmt = db.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                // Decrement user's num_uploads
                User user = userDAO.getById(doc.getUploaderId());
                if (user != null) {
                    user.setNumUploads(Math.max(0, user.getNumUploads() - 1));
                    userDAO.update(user);
                }
                simulateEmailAlert("Document deleted: " + doc.getTitle() + " (ID: " + id + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Delete document error: " + e.getMessage());
        }
        return false;
    }

    // Simulate email alert (console print; extend with JavaMail for real emails)
    private void simulateEmailAlert(String message) {
        System.out.println("\n[EMAIL ALERT SIMULATED] " + message);
        // For real implementation: Use javax.mail to send via SMTP (e.g., Gmail)
    }
}