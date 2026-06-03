/**
 * Represents a registered student in the system.
 * Encapsulates student identity data.
 */
public class Student {
    private String name;
    private String email;
    private String studentId;

    public Student(String name, String email, String studentId) {
        this.name = name;
        this.email = email.toLowerCase().trim();
        this.studentId = studentId;
    }

    public String getName()      { return name; }
    public String getEmail()     { return email; }
    public String getStudentId() { return studentId; }

    @Override
    public String toString() {
        return String.format("Student[id=%s, name=%s, email=%s]", studentId, name, email);
    }
}
