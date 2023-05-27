package noMoreInactivity.model;

public class MouseClickInput extends MyInput {

	private int x;
    private int y;
    private int button;

    public MouseClickInput(int x, int y, int button) {
        this.x = x;
        this.y = y;
        this.button = button;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getButton() {
        return button;
    }
}
