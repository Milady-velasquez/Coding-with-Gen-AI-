/**
 * Represents the outcome of a feedback submission attempt.
 * Uses a sealed-style enum + factory methods instead of throwing exceptions,
 * keeping flow control in the service layer clean and testable.
 */
public class SubmissionResult {

    public enum Status {
        SUCCESS,
        INVALID_EMAIL,
        DUPLICATE_SUBMISSION
    }

    private final Status  status;
    private final String  message;
    private final boolean successful;

    private SubmissionResult(Status status, String message) {
        this.status     = status;
        this.message    = message;
        this.successful = (status == Status.SUCCESS);
    }

    // ── Factory methods ──────────────────────────────────────────────────────

    public static SubmissionResult success(Student student, Feedback feedback) {
        String msg = String.format(
            "✅  Feedback submitted successfully!\n" +
            "    Student  : %s (%s)\n" +
            "    Course   : %s\n" +
            "    Rating   : %d/5\n" +
            "    Submitted: %s",
            student.getName(), student.getEmail(),
            feedback.getCourseId(),
            feedback.getRating(),
            feedback.getSubmittedAt()
        );
        return new SubmissionResult(Status.SUCCESS, msg);
    }

    public static SubmissionResult invalidEmail(String email) {
        String msg = String.format(
            "❌  Invalid Email\n" +
            "    \"%s\" is not registered in the student system.\n" +
            "    Please use your official school email address.",
            email
        );
        return new SubmissionResult(Status.INVALID_EMAIL, msg);
    }

    public static SubmissionResult duplicate(Student student, String courseId) {
        String msg = String.format(
            "⚠️  Duplicate Submission Detected\n" +
            "    %s, you have already submitted feedback for course %s.\n" +
            "    Only one response per course is allowed.",
            student.getName(), courseId.toUpperCase()
        );
        return new SubmissionResult(Status.DUPLICATE_SUBMISSION, msg);
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public Status  getStatus()      { return status; }
    public String  getMessage()     { return message; }
    public boolean isSuccessful()   { return successful; }

    @Override
    public String toString() { return message; }
}
