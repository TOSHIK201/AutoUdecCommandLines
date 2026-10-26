import javax.swing.SwingUtilities;
import java.lang.reflect.Method;

public class HandleSnapTest {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                PropertyPanel property = new PropertyPanel();
                LayerCanvas canvas = new LayerCanvas(property);
                property.setCanvas(canvas);
                canvas.setSize(1000, 800);
                canvas.doLayout();
                canvas.fitToScreen();

                Method snap = LayerCanvas.class.getDeclaredMethod(
                        "snapExcavationY", double.class, double.class, boolean.class);
                snap.setAccessible(true);

                double top = (double) snap.invoke(canvas, 26.35, 21.6, true);
                check("snap to layer", Math.abs(top - 26.4) < 1e-9);

                double clampedTop = (double) snap.invoke(canvas, 21.65, 21.6, true);
                check("minimum height top", clampedTop >= 21.6 + 0.1 - 1e-9);

                double clampedBottom = (double) snap.invoke(canvas, 26.35, 26.4, false);
                check("minimum height bottom", clampedBottom <= 26.4 - 0.1 + 1e-9);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("HANDLE SNAP TEST PASSED");
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
