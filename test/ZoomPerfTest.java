import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

public class ZoomPerfTest {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                PropertyPanel property = new PropertyPanel();
                LayerCanvas canvas = new LayerCanvas(property);
                property.setCanvas(canvas);
                canvas.setSize(1000, 800);
                canvas.doLayout();
                canvas.fitToScreen();
                BufferedImage image = new BufferedImage(1000, 800, BufferedImage.TYPE_INT_RGB);
                paint(canvas, image);

                Field cacheField = LayerCanvas.class.getDeclaredField("jointCache");
                cacheField.setAccessible(true);
                Object previous = cacheField.get(canvas);
                int rebuilds = 0;
                long start = System.nanoTime();
                for (int i = 0; i < 40; i++) {
                    canvas.zoomAt(500, 400, i < 20 ? -1 : 1);
                    paint(canvas, image);
                    Object current = cacheField.get(canvas);
                    if (current != previous) rebuilds++;
                    previous = current;
                }
                long ms = (System.nanoTime() - start) / 1_000_000;
                System.out.printf("ZOOM %.2f ms/frame, rebuilds=%d%n",
                        (double) ms / 40, rebuilds);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void paint(javax.swing.JComponent c, BufferedImage image) {
        Graphics2D g = image.createGraphics();
        c.paint(g);
        g.dispose();
    }
}
