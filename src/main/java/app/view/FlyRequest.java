package app.view;

/**
 * A request to the swipe card: "fly away as Present/Absent/Late".
 * It is a normal class (no equals), so two requests with the same status are still different values
 * and both trigger the card.
 */
public final class FlyRequest {
    private final String status;

    public FlyRequest(String status) {
        if (!"P".equals(status) && !"A".equals(status) && !"L".equals(status)) {
            throw new IllegalArgumentException("Status must be P, A or L but was: " + status);
        }
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
