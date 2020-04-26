import java.util.Random;

public enum OpType {
	INSERT, DELETE, FIND;
	
	public static OpType getRandomColor() {
        Random random = new Random();
        return values()[random.nextInt(values().length)];
    }
}
