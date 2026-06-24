package gym.exception;


public class ExpiredMembershipException extends Exception {

    private int memberId;

    public ExpiredMembershipException(String message, int memberId) {
        super(message);
        this.memberId = memberId;
    }

    public int getMemberId() {
        return memberId;
    }
}