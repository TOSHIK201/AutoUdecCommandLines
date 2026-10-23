import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class PerfTest {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PropertyPanel property = new PropertyPanel();
            LayerCanvas canvas = new LayerCanvas(property);
            property.setCanvas(canvas);
            canvas.setSize(1000, 800);
            canvas.doLayout();
            canvas.fitToScreen();

            BufferedImage image = new BufferedImage(1000, 800, BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < 20; i++) paint(canvas, image);

            long start = System.nanoTime();
            int frames = 200;
            for (int i = 0; i < frames; i++) paint(canvas, image);
            long ms = (System.nanoTime() - start) / 1_000_000;
            System.out.printf("PAINT %.2f ms/frame (%d frames in %d ms)%n",
                    (double) ms / frames, frames, ms);

            try {
                java.lang.reflect.Field f = LayerCanvas.class.getDeclaredField("jointCache");
                f.setAccessible(true);
                Object first = f.get(canvas);
                paint(canvas, image);
                Object second = f.get(canvas);
                System.out.println("CACHE REUSED: " + (first == second));
                java.util.List<?> layers = (java.util.List<?>) second;
                int total = 0;
                for (Object layer : layers) total += ((java.util.List<?>) layer).size();
                System.out.println("JOINT LINES: " + total);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            canvas.setView(20, 60, 40);
            for (int i = 0; i < 10; i++) paint(canvas, image);
            long zoomStart = System.nanoTime();
            for (int i = 0; i < 100; i++) paint(canvas, image);
            long zoomMs = (System.nanoTime() - zoomStart) / 1_000_000;
            System.out.printf("PAINT ZOOM20 %.2f ms/frame%n", (double) zoomMs / 100);
        });
    }

    private static void paint(JComponent c, BufferedImage image) {
        Graphics2D g = image.createGraphics();
        c.paint(g);
        g.dispose();
    }
}
