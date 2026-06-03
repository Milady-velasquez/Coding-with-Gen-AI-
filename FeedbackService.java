/**
 * Orchestrates the feedback submission workflow.
 * Applies business rules — email validation, duplicate prevention — before
 * delegating persistence to FeedbackRepository.
 *
 * This class acts as the Service layer in a simple layered architecture.
 */
public class FeedbackService {

    private final StudentRegistry    registry;
    private final FeedbackRepository repository;

    public FeedbackService(StudentRegistry registry, FeedbackRepository repository) {
        this.registry   = registry;
        this.repository = repository;
    }

    /**
     * Attempts to submit feedback and returns a SubmissionResult describing the outcome.
     *
     * Rules enforced (in order):
     *  1. Email must belong to a registered student  → INVALID_EMAIL
     *  2. Student must not have already responded    → DUPLICATE_SUBMISSION
     *  3. All checks pass                            → SUCCESS
     */
    public SubmissionResult submitFeedback(String email,
                                           String courseId,
                                           int    rating,
                                           String comment) {

        // ── Rule 1: validate email against the student registry ──────────────
        if (!registry.isRegistered(email)) {
            return SubmissionResult.invalidEmail(email);
        }

        // ── Rule 2: prevent duplicate submissions ────────────────────────────
        if (repository.hasSubmitted(email, courseId)) {
            Student student = registry.getStudent(email);
            return SubmissionResult.duplicate(student, courseId);
        }

        // ── Rule 3: persist the feedback ─────────────────────────────────────
        Feedback feedback = new Feedback(email, courseId, rating, comment);
        repository.save(feedback);

        Student student = registry.getStudent(email);
        return SubmissionResult.success(student, feedback);
    }
}
