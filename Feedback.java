/**
 * Represents a single feedback submission from a student for a course.
 * Immutable once created — feedback entries should not be edited after submission.
 */
public class Feedback {
    private final String studentEmail;
    private final String courseId;
    private final int rating;          // 1–5
    private final String comment;
    private final String submittedAt;  // ISO-style timestamp string

    public Feedback(String studentEmail, String courseId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        this.studentEmail = studentEmail.toLowerCase().trim();
        this.courseId     = courseId.trim().toUpperCase();
        this.rating       = rating;
        this.comment      = comment.trim();
        this.submittedAt  = java.time.LocalDateTime.now()
                               .format(java.time.format.DateTimeFormatter
                                   .ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getStudentEmail() { return studentEmail; }
    public String getCourseId()     { return courseId; }
    public int    getRating()       { return rating; }
    public String getComment()      { return comment; }
    public String getSubmittedAt()  { return submittedAt; }

    @Override
    public String toString() {
        return String.format(
            "Feedback[course=%s, email=%s, rating=%d/5, submitted=%s]\n  Comment: %s",
            courseId, studentEmail, rating, submittedAt, comment
        );
    }
}
