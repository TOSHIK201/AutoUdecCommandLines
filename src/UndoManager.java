import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class UndoManager {
    private static final int LIMIT = 50;
    private static final Deque<byte[]> STACK = new ArrayDeque<>();
    private static int suspendDepth;

    public static void push() {
        if (ParameterList.restoring || suspendDepth > 0) return;
        byte[] snapshot = ProjectIO.toBytes();
        if (!STACK.isEmpty() && Arrays.equals(STACK.peek(), snapshot)) return;
        STACK.push(snapshot);
        while (STACK.size() > LIMIT) STACK.removeLast();
    }

    public static boolean undo() {
        byte[] current = ProjectIO.toBytes();
        while (!STACK.isEmpty() && Arrays.equals(STACK.peek(), current)) {
            STACK.pop();
        }
        if (STACK.isEmpty()) return false;
        byte[] snapshot = STACK.pop();
        try {
            ProjectIO.loadBytes(snapshot);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean canUndo() {
        return !STACK.isEmpty();
    }

    public static void clear() {
        STACK.clear();
    }

    public static void suspend(Runnable action) {
        suspendDepth++;
        try {
            action.run();
        } finally {
            suspendDepth--;
        }
    }
}
