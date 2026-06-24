package GYM.exception;

public class MemberNotFoundException extends Exception {

    public MemberNotFoundException(String message) {
        super(message);
    }

    public MemberNotFoundException(int id) {
        super("Member with ID " + id + " was not found.");
    }
}