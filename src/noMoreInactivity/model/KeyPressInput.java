package noMoreInactivity.model;

public class KeyPressInput extends MyInput {
	
	private int keyCode;

    public KeyPressInput(int keyCode) {
        this.keyCode = keyCode;
    }

    public int getKeyCode() {
        return keyCode;
    }
}
