/**
 * Entry point — wires up the system and runs demonstration scenarios.
 *
 * Class hierarchy summary:
 *   Student            – domain entity (name, email, id)
 *   Feedback           – domain entity (email, courseId, rating, comment)
 *   StudentRegistry    – HashMap<email, Student>  for O(1) lookup
 *   FeedbackRepository – HashSet<key> for O(1) duplicate detection + ArrayList for storage
 *   FeedbackService    – business rules (validate → deduplicate → persist)
 *   SubmissionResult   – outcome value object (SUCCESS / INVALID_EMAIL / DUPLICATE)
 */
public class Main {

    public static void main(String[] args) {

        // ── 1. Bootstrap the registry with enrolled students ─────────────────
        StudentRegistry registry = new StudentRegistry();
        registry.registerStudent(new Student("Alice Johnson",  "alice.johnson@university.edu",  "S1001"));
        registry.registerStudent(new Student("Bob Martinez",   "bob.martinez@university.edu",   "S1002"));
        registry.registerStudent(new Student("Carol Williams", "carol.williams@university.edu", "S1003"));
        registry.registerStudent(new Student("David Chen",     "david.chen@university.edu",     "S1004"));

        // ── 2. Wire up the service ────────────────────────────────────────────
        FeedbackRepository repository = new FeedbackRepository();
        FeedbackService    service    = new FeedbackService(registry, repository);

        printHeader("COLLEGE COURSE FEEDBACK SYSTEM");
        System.out.printf("Registered students: %d%n%n", registry.size());

        // ── 3. Run demonstration scenarios ───────────────────────────────────

        // Scenario A: valid submission
        scenario("A — Valid submission (Alice, CS101)",
            service.submitFeedback(
                "alice.johnson@university.edu", "CS101", 5,
                "Excellent course! The labs were very hands-on."
            )
        );

        // Scenario B: valid submission — different student, same course
        scenario("B — Valid submission (Bob, CS101)",
            service.submitFeedback(
                "bob.martinez@university.edu", "CS101", 4,
                "Good content but the pacing was a bit fast."
            )
        );

        // Scenario C: duplicate submission — Alice tries CS101 again
        scenario("C — Duplicate submission (Alice tries CS101 again)",
            service.submitFeedback(
                "alice.johnson@university.edu", "CS101", 3,
                "Changed my mind, it was just okay."
            )
        );

        // Scenario D: unregistered email
        scenario("D — Unregistered email",
            service.submitFeedback(
                "random.person@gmail.com", "CS101", 2,
                "I hacked in!"
            )
        );

        // Scenario E: valid submission — same student, different course
        scenario("E — Valid submission (Alice, MATH201 — different course)",
            service.submitFeedback(
                "alice.johnson@university.edu", "MATH201", 4,
                "Professor explains concepts very clearly."
            )
        );

        // Scenario F: empty/malformed email
        scenario("F — Empty email string",
            service.submitFeedback(
                "", "CS101", 3, "Test comment"
            )
        );

        // Scenario G: case-insensitive email check
        scenario("G — Same email with different capitalisation (Carol)",
            service.submitFeedback(
                "CAROL.WILLIAMS@UNIVERSITY.EDU", "BIO301", 5,
                "Fascinating subject, loved the field trips."
            )
        );

        // Scenario H: Carol tries BIO301 again (duplicate for case-normalised email)
        scenario("H — Duplicate after case-normalised submission (Carol, BIO301)",
            service.submitFeedback(
                "carol.williams@university.edu", "BIO301", 1,
                "Just kidding, it was terrible."
            )
        );

        // ── 4. Summary report ─────────────────────────────────────────────────
        printSummary(repository);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void scenario(String label, SubmissionResult result) {
        System.out.println("━".repeat(60));
        System.out.println("Scenario " + label);
        System.out.println();
        System.out.println(result.getMessage());
        System.out.println();
    }

    private static void printHeader(String title) {
        System.out.println("═".repeat(60));
        System.out.println("  " + title);
        System.out.println("═".repeat(60));
        System.out.println();
    }

    private static void printSummary(FeedbackRepository repository) {
        System.out.println("━".repeat(60));
        System.out.println("FINAL SUMMARY — All stored feedback entries");
        System.out.println("━".repeat(60));
        var all = repository.getAllFeedback();
        if (all.isEmpty()) {
            System.out.println("  (no feedback recorded)");
        } else {
            for (int i = 0; i < all.size(); i++) {
                System.out.printf("%n  [%d] %s%n", i + 1, all.get(i));
            }
        }
        System.out.printf("%n  Total valid submissions: %d%n", repository.totalSubmissions());
        System.out.println("═".repeat(60));
    }
}
