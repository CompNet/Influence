package klout;

/**
 * Created by nicolas on 27/02/15.
 */
public class KloutException extends Exception {
    private Long id;
    public KloutException() {
        super();
    }

    public KloutException(String message) {
        super(message);
    }
    public KloutException(String message, Long id) {
        this(message);
        this.id=id;
    }

    public Long getId() {
        return id;
    }
}
