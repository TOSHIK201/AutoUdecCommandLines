import javax.swing.*;

public class FrameSmokeTest {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ConfigFrame frame = new ConfigFrame();
            frame.setVisible(false);
            frame.dispose();
            System.out.println("FRAME SMOKE PASSED");
        });
    }
}
