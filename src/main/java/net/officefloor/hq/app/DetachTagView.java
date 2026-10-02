package net.officefloor.hq.app;

/** The result of removing a tag: how many tags the project carries afterwards. */
public class DetachTagView {

    private final int remaining;

    public DetachTagView(int remaining) {
        this.remaining = remaining;
    }

    public int getRemaining() {
        return remaining;
    }
}
