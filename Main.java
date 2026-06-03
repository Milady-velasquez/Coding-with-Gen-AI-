import java.util.Scanner;
import java.util.List;
 
/**
 * Interactive console UI for the Course Feedback System.
 * Guides the student step-by-step: email → course → rating → comment.
 */
public class Main {
 
    private static final Scanner scanner = new Scanner(System.in);
 
    public static void main(String[] args) {
 
        // ── Bootstrap registered students ────────────────────────────────────
        StudentRegistry registry = new StudentRegistry();
        registry.registerStudent(new Student("Alice Johnson",  "alice@university.edu",  "S1001"));
        registry.registerStudent(new Student("Bob Martinez",   "bob@university.edu",    "S1002"));
        registry.registerStudent(new Student("Carol Williams", "carol@university.edu",  "S1003"));
        registry.registerStudent(new Student("David Chen",     "david@university.edu",  "S1004"));
 
        FeedbackRepository repository = new FeedbackRepository();
        FeedbackService    service    = new FeedbackService(registry, repository);
 
        printBanner();
 
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
 
            switch (choice) {
                case "1" -> submitFeedbackFlow(service, repository, registry);
                case "2" -> viewFeedbackFlow(repository);
                case "3" -> {
                    printLine();
                    System.out.println("  Thank you for using the Feedback System. Goodbye!");
                    printLine();
                    running = false;
                }
                default  -> System.out.println("  [!] Invalid option. Please enter 1, 2, or 3.\n");
            }
        }
 
        scanner.close();
    }
 
    // =========================================================================
    //  FLOW 1 — Submit Feedback
    // =========================================================================
 
    private static void submitFeedbackFlow(FeedbackService service,
                                           FeedbackRepository repository,
                                           StudentRegistry registry) {
        printLine();
        System.out.println("  SUBMIT COURSE FEEDBACK");
        printLine();
 
        // ── Step 1: Email ─────────────────────────────────────────────────────
        String email = promptEmail(registry);
        if (email == null) return;   // user chose to go back
 
        Student student = registry.getStudent(email);
        System.out.println();
        System.out.println("  Welcome, " + student.getName() + "!");
 
        // ── Step 2: Course ID ─────────────────────────────────────────────────
        String courseId = promptCourseId(email, repository);
        if (courseId == null) return;
 
        // ── Step 3: Rating ────────────────────────────────────────────────────
        int rating = promptRating();
        if (rating == -1) return;
 
        // ── Step 4: Comment ───────────────────────────────────────────────────
        String comment = promptComment();
 
        // ── Submit ────────────────────────────────────────────────────────────
        System.out.println();
        printLine();
        SubmissionResult result = service.submitFeedback(email, courseId, rating, comment);
        System.out.println(result.getMessage());
        printLine();
        System.out.println();
    }
 
    // ── Step 1 helper: keep asking until a valid registered email is entered ──
    private static String promptEmail(StudentRegistry registry) {
        while (true) {
            System.out.print("  Step 1/4 | Enter your school email (or 'back' to cancel): ");
            String input = scanner.nextLine().trim();
 
            if (input.equalsIgnoreCase("back")) return null;
            if (input.isEmpty()) {
                System.out.println("  [!] Email cannot be empty. Please try again.\n");
                continue;
            }
 
            if (registry.isRegistered(input)) {
                System.out.println("  [OK] Email verified.");
                return input.toLowerCase();
            } else {
                System.out.println("  [!] Invalid Email — \"" + input + "\" is not registered.");
                System.out.println("      Please use your official school email address.\n");
            }
        }
    }
 
    // ── Step 2 helper: keep asking until a valid, non-duplicate course is given
    private static String promptCourseId(String email, FeedbackRepository repository) {
        while (true) {
            System.out.println();
            System.out.print("  Step 2/4 | Enter the Course ID (e.g. CS101) or 'back': ");
            String input = scanner.nextLine().trim().toUpperCase();
 
            if (input.equalsIgnoreCase("BACK")) return null;
            if (input.isEmpty()) {
                System.out.println("  [!] Course ID cannot be empty. Please try again.");
                continue;
            }
 
            if (repository.hasSubmitted(email, input)) {
                System.out.println("  [!] You have already submitted feedback for course " + input + ".");
                System.out.println("      Only one response per course is allowed.");
                System.out.println("      Please enter a different course ID, or type 'back' to cancel.");
            } else {
                System.out.println("  [OK] Course ID accepted: " + input);
                return input;
            }
        }
    }
 
    // ── Step 3 helper: keep asking until a number 1-5 is given ───────────────
    private static int promptRating() {
        while (true) {
            System.out.println();
            System.out.print("  Step 3/4 | Rate this course from 1 (Poor) to 5 (Excellent): ");
            String input = scanner.nextLine().trim();
 
            if (input.equalsIgnoreCase("back")) return -1;
 
            try {
                int rating = Integer.parseInt(input);
                if (rating >= 1 && rating <= 5) {
                    String stars = "*".repeat(rating) + "-".repeat(5 - rating);
                    System.out.println("  [OK] Rating accepted: [" + stars + "] " + rating + "/5");
                    return rating;
                } else {
                    System.out.println("  [!] Please enter a number between 1 and 5.");
                }
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid input. Please enter a number between 1 and 5.");
            }
        }
    }
 
    // ── Step 4 helper: comment (optional, press Enter to skip) ───────────────
    private static String promptComment() {
        System.out.println();
        System.out.print("  Step 4/4 | Enter your feedback comment (or press Enter to skip): ");
        String comment = scanner.nextLine().trim();
        if (comment.isEmpty()) {
            comment = "(No comment provided)";
            System.out.println("  [OK] No comment — that's fine!");
        } else {
            System.out.println("  [OK] Comment recorded.");
        }
        return comment;
    }
 
    // =========================================================================
    //  FLOW 2 — View Feedback
    // =========================================================================
 
    private static void viewFeedbackFlow(FeedbackRepository repository) {
        printLine();
        System.out.println("  VIEW SUBMITTED FEEDBACK");
        printLine();
        System.out.print("  Enter Course ID to view feedback (or press Enter for all): ");
        String input = scanner.nextLine().trim();
 
        List<Feedback> results = input.isEmpty()
            ? repository.getAllFeedback()
            : repository.getFeedbackByCourse(input);
 
        System.out.println();
        if (results.isEmpty()) {
            System.out.println("  No feedback found" + (input.isEmpty() ? "." : " for course " + input.toUpperCase() + "."));
        } else {
            String label = input.isEmpty() ? "ALL COURSES" : "Course: " + input.toUpperCase();
            System.out.println("  Showing " + results.size() + " submission(s) — " + label);
            System.out.println();
            for (int i = 0; i < results.size(); i++) {
                Feedback f = results.get(i);
                String stars = "*".repeat(f.getRating()) + "-".repeat(5 - f.getRating());
                System.out.println("  #" + (i + 1) + " ----------------------------------------");
                System.out.println("  Course    : " + f.getCourseId());
                System.out.println("  Email     : " + f.getStudentEmail());
                System.out.println("  Rating    : [" + stars + "] " + f.getRating() + "/5");
                System.out.println("  Comment   : " + f.getComment());
                System.out.println("  Submitted : " + f.getSubmittedAt());
                System.out.println();
            }
        }
        printLine();
        System.out.println();
    }
 
    // =========================================================================
    //  UI Helpers
    // =========================================================================
 
    private static void printBanner() {
        printLine();
        System.out.println("       COLLEGE COURSE FEEDBACK SYSTEM");
        System.out.println("    Submit and review feedback for your courses");
        printLine();
        System.out.println();
    }
 
    private static void printMenu() {
        System.out.println("  MAIN MENU");
        System.out.println("  ---------");
        System.out.println("  1. Submit feedback for a course");
        System.out.println("  2. View submitted feedback");
        System.out.println("  3. Exit");
        System.out.print("  Enter your choice (1-3): ");
    }
 
    private static void printLine() {
        System.out.println("  ================================================");
    }
}