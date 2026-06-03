import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
 
/**
 * Maintains the registry of enrolled students.
 * Uses a HashMap keyed by email for O(1) lookup during validation.
 */
public class StudentRegistry {
 
    // Key: lowercase email → Value: Student object
    private final Map<String, Student> registeredStudents = new HashMap<>();
 
    /**
     * Registers a new student. Duplicate emails are silently overwritten
     * (use enroll() for guarded registration in real systems).
     */
    public void registerStudent(Student student) {
        registeredStudents.put(student.getEmail(), student);
    }
 
    /** Domain that is accepted for open enrollment. */
    private static final String VALID_DOMAIN = "@university.edu";
 
    /**
     * Returns true if the email is a pre-registered student
     * OR has the official school domain (@university.edu).
     */
    public boolean isRegistered(String email) {
        String normalised = email.toLowerCase().trim();
        if (registeredStudents.containsKey(normalised)) return true;
        return normalised.endsWith(VALID_DOMAIN) && normalised.length() > VALID_DOMAIN.length();
    }
 
    /**
     * Returns the Student for the given email.
     * If the email has the school domain but is not pre-registered,
     * a Student is created on-the-fly from the email's local part.
     */
    public Student getStudent(String email) {
        String normalised = email.toLowerCase().trim();
        if (registeredStudents.containsKey(normalised)) {
            return registeredStudents.get(normalised);
        }
        // Auto-create from email: "mil.jones@university.edu" -> "Mil Jones"
        String localPart = normalised.replace(VALID_DOMAIN, "");
        String name = toTitleCase(localPart.replace(".", " ").replace("_", " "));
        Student dynamic = new Student(name, normalised, "AUTO-" + localPart.toUpperCase());
        registeredStudents.put(normalised, dynamic); // cache for this session
        return dynamic;
    }
 
    /** Capitalises the first letter of each word. */
    private String toTitleCase(String input) {
        String[] words = input.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1))
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }
 
    public Collection<Student> getAllStudents() {
        return registeredStudents.values();
    }
 
    public int size() {
        return registeredStudents.size();
    }
}
 