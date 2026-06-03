import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Stores all feedback submissions and enforces the one-response-per-course rule.
 *
 * Duplicate detection key:  "email::COURSEID"
 * A HashSet<String> provides O(1) average-time contains() and add(),
 * making duplicate checks extremely efficient even for large student bodies.
 */
public class FeedbackRepository {

    // HashSet stores composite keys to detect duplicates in O(1)
    private final Set<String> submissionKeys = new HashSet<>();

    // Full feedback objects stored in insertion order
    private final List<Feedback> feedbackList = new ArrayList<>();

    /**
     * Builds the unique key that identifies one student's response to one course.
     */
    private String buildKey(String email, String courseId) {
        return email.toLowerCase().trim() + "::" + courseId.trim().toUpperCase();
    }

    /**
     * Returns true if the student has already submitted feedback for the course.
     * O(1) lookup via HashSet.
     */
    public boolean hasSubmitted(String email, String courseId) {
        return submissionKeys.contains(buildKey(email, courseId));
    }

    /**
     * Saves a new Feedback object and records its key in the HashSet.
     * Callers must check hasSubmitted() before calling this.
     */
    public void save(Feedback feedback) {
        String key = buildKey(feedback.getStudentEmail(), feedback.getCourseId());
        submissionKeys.add(key);
        feedbackList.add(feedback);
    }

    /** Returns all feedback for a specific course. */
    public List<Feedback> getFeedbackByCourse(String courseId) {
        String normalised = courseId.trim().toUpperCase();
        return feedbackList.stream()
                .filter(f -> f.getCourseId().equals(normalised))
                .collect(Collectors.toList());
    }

    /** Returns all feedback submitted by a specific student. */
    public List<Feedback> getFeedbackByStudent(String email) {
        String normalised = email.toLowerCase().trim();
        return feedbackList.stream()
                .filter(f -> f.getStudentEmail().equals(normalised))
                .collect(Collectors.toList());
    }

    /** Returns every feedback entry in the system. */
    public List<Feedback> getAllFeedback() {
        return new ArrayList<>(feedbackList);
    }

    public int totalSubmissions() {
        return feedbackList.size();
    }
}
