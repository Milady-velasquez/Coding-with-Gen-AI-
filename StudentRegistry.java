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

    /**
     * Returns true if the given email belongs to a registered student.
     */
    public boolean isRegistered(String email) {
        return registeredStudents.containsKey(email.toLowerCase().trim());
    }

    /**
     * Returns the Student for the given email, or null if not found.
     */
    public Student getStudent(String email) {
        return registeredStudents.get(email.toLowerCase().trim());
    }

    public Collection<Student> getAllStudents() {
        return registeredStudents.values();
    }

    public int size() {
        return registeredStudents.size();
    }
}
