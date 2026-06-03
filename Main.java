import java.util.Scanner;
import java.util.List;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    // All available courses — add or remove as needed
    private static final String[] COURSES = {
        "CS123 - Intro to Programming",
        "CS201 - Data Structures",
        "MATH101 - Calculus I",
        "ENG110 - English Composition",
        "BIO201 - Biology Fundamentals"
    };

    // Extract just the course code from a course entry (e.g. "CS123")
    private static String courseCode(int index) {
        return COURSES[index].split(" - ")[0].trim();
    }

    public static void main(String[] args) {

        StudentRegistry    registry   = new StudentRegistry();
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
                default -> System.out.println("  [!] Invalid option. Please enter 1, 2, or 3.\n");
            }
        }

        scanner.close();
    }

    // =========================================================================
    //  FLOW 1 - Submit Feedback
    // =========================================================================

    private static void submitFeedbackFlow(FeedbackService service,
                                           FeedbackRepository repository,
                                           StudentRegistry registry) {
        printLine();
        System.out.println("  SUBMIT COURSE FEEDBACK");
        printLine();

        // Step 1: Email
        String email = promptEmail(registry);
        if (email == null) return;

        Student student = registry.getStudent(email);
        System.out.println();
        System.out.println("  Welcome, " + student.getName() + "!");

        // Step 2: Pick a course from the list
        String courseId = promptCourseSelection(email, repository);
        if (courseId == null) return;

        // Step 3: Rating
        int rating = promptRating();
        if (rating == -1) return;

        // Step 4: Comment
        String comment = promptComment();

        // Submit
        System.out.println();
        printLine();
        SubmissionResult result = service.submitFeedback(email, courseId, rating, comment);
        System.out.println(result.getMessage());
        printLine();
        System.out.println();
    }

    // ── Step 1: Email ─────────────────────────────────────────────────────────
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
                System.out.println("  [!] Invalid Email - \"" + input + "\" is not registered.");
                System.out.println("      Please use your official school email address.\n");
            }
        }
    }

    // ── Step 2: Select a course from numbered list ────────────────────────────
    private static String promptCourseSelection(String email, FeedbackRepository repository) {
        while (true) {
            System.out.println();
            System.out.println("  Step 2/4 | Select a course to review:");
            System.out.println();

            for (int i = 0; i < COURSES.length; i++) {
                String code    = courseCode(i);
                String already = repository.hasSubmitted(email, code) ? "  [submitted]" : "";
                System.out.printf("    [%d] %s%s%n", i + 1, COURSES[i], already);
            }

            System.out.println();
            System.out.print("  Enter a number (1-" + COURSES.length + ") or 'back' to cancel: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("back")) return null;

            try {
                int choice = Integer.parseInt(input);
                if (choice < 1 || choice > COURSES.length) {
                    System.out.println("  [!] Please enter a number between 1 and " + COURSES.length + ".");
                    continue;
                }

                String selected = courseCode(choice - 1);

                if (repository.hasSubmitted(email, selected)) {
                    System.out.println("  [!] You have already submitted feedback for " + selected + ".");
                    System.out.println("      Please choose a different course.\n");
                } else {
                    System.out.println("  [OK] Course selected: " + COURSES[choice - 1]);
                    return selected;
                }

            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid input. Please enter a number.");
            }
        }
    }

    // ── Step 3: Rating ────────────────────────────────────────────────────────
    private static int promptRating() {
        while (true) {
            System.out.println();
            System.out.println("  Step 3/4 | Select a rating:");
            System.out.println();
            System.out.println("    [1] * - - - -  Poor");
            System.out.println("    [2] * * - - -  Fair");
            System.out.println("    [3] * * * - -  Good");
            System.out.println("    [4] * * * * -  Very Good");
            System.out.println("    [5] * * * * *  Excellent");
            System.out.println();
            System.out.print("  Enter a number (1-5) or 'back' to cancel: ");
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

    // ── Step 4: Comment ───────────────────────────────────────────────────────
    private static String promptComment() {
        System.out.println();
        System.out.print("  Step 4/4 | Enter your feedback comment (or press Enter to skip): ");
        String comment = scanner.nextLine().trim();
        if (comment.isEmpty()) {
            comment = "(No comment provided)";
            System.out.println("  [OK] Skipped.");
        } else {
            System.out.println("  [OK] Comment recorded.");
        }
        return comment;
    }

    // =========================================================================
    //  FLOW 2 - View Feedback
    // =========================================================================

    private static void viewFeedbackFlow(FeedbackRepository repository) {
        printLine();
        System.out.println("  VIEW SUBMITTED FEEDBACK");
        printLine();
        System.out.println();
        System.out.println("  Select a course to view (or press Enter for all):");
        System.out.println();

        for (int i = 0; i < COURSES.length; i++) {
            System.out.printf("    [%d] %s%n", i + 1, COURSES[i]);
        }

        System.out.println();
        System.out.print("  Enter a number or press Enter for all: ");
        String input = scanner.nextLine().trim();

        List<Feedback> results;
        String label;

        if (input.isEmpty()) {
            results = repository.getAllFeedback();
            label   = "ALL COURSES";
        } else {
            try {
                int choice = Integer.parseInt(input);
                if (choice < 1 || choice > COURSES.length) {
                    System.out.println("  [!] Invalid choice. Showing all feedback.\n");
                    results = repository.getAllFeedback();
                    label   = "ALL COURSES";
                } else {
                    String code = courseCode(choice - 1);
                    results = repository.getFeedbackByCourse(code);
                    label   = COURSES[choice - 1];
                }
            } catch (NumberFormatException e) {
                results = repository.getAllFeedback();
                label   = "ALL COURSES";
            }
        }

        System.out.println();
        if (results.isEmpty()) {
            System.out.println("  No feedback submitted yet for: " + label);
        } else {
            System.out.println("  " + results.size() + " submission(s) — " + label);
            System.out.println();
            for (int i = 0; i < results.size(); i++) {
                Feedback f     = results.get(i);
                String   stars = "*".repeat(f.getRating()) + "-".repeat(5 - f.getRating());
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
        System.out.println("  [1] Submit feedback for a course");
        System.out.println("  [2] View submitted feedback");
        System.out.println("  [3] Exit");
        System.out.print("  Enter your choice (1-3): ");
    }

    private static void printLine() {
        System.out.println("  ================================================");
    }
}


/*
PROGRAM OUTPUT 

  ----jGRASP exec: java Main
   ================================================
        COLLEGE COURSE FEEDBACK SYSTEM
     Submit and review feedback for your courses
   ================================================
 
   MAIN MENU
   ---------
   [1] Submit feedback for a course
   [2] View submitted feedback
   [3] Exit
   Enter your choice (1-3): 1
   ================================================
   SUBMIT COURSE FEEDBACK
   ================================================
   Step 1/4 | Enter your school email (or 'back' to cancel): mil@university.edu
   [OK] Email verified.
 
   Welcome, Mil!
 
   Step 2/4 | Select a course to review:
 
     [1] CS123 - Intro to Programming
     [2] CS201 - Data Structures
     [3] MATH101 - Calculus I
     [4] ENG110 - English Composition
     [5] BIO201 - Biology Fundamentals
 
   Enter a number (1-5) or 'back' to cancel: 3
   [OK] Course selected: MATH101 - Calculus I
 
   Step 3/4 | Select a rating:
 
     [1] * - - - -  Poor
     [2] * * - - -  Fair
     [3] * * * - -  Good
     [4] * * * * -  Very Good
     [5] * * * * *  Excellent
 
   Enter a number (1-5) or 'back' to cancel: 4
   [OK] Rating accepted: [****-] 4/5
 
   Step 4/4 | Enter your feedback comment (or press Enter to skip): 
   [OK] Skipped.
 
   ================================================
 ?  Feedback submitted successfully!
     Student  : Mil (mil@university.edu)
     Course   : MATH101
     Rating   : 4/5
     Submitted: 2026-06-03 12:19:09
   ================================================
 
   MAIN MENU
   ---------
   [1] Submit feedback for a course
   [2] View submitted feedback
   [3] Exit
   Enter your choice (1-3): 

*/