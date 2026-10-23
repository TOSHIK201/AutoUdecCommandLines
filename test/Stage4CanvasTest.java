import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

public class Stage4CanvasTest {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PropertyPanel property = new PropertyPanel();
            LayerCanvas canvas = new LayerCanvas(property);
            property.setCanvas(canvas);
            canvas.setSize(1000, 800);
            canvas.doLayout();
            canvas.fitToScreen();
            canvas.setActiveStage(3);

            BoltGroup[] bolt = new BoltGroup[1];
            canvas.setBoltFactory(() -> {
                BoltGroup g = new BoltGroup("T");
                g.spacing = 0;
                return g;
            });
            canvas.setBoltCreated(g -> { bolt[0] = g; g.rebuild(); });
            canvas.setBoltMode(true);
            click(canvas, 300, 300);
            click(canvas, 500, 300);
            if (bolt[0] == null || bolt[0].bolts.size() != 5) {
                throw new AssertionError("锚杆安装线创建失败");
            }

            Pline[] pline = new Pline[1];
            canvas.setPlineFactory(Pline::new);
            canvas.setPlineCreated(p -> pline[0] = p);
            canvas.setPlineMode(true);
            click(canvas, 300, 420);
            click(canvas, 700, 420);
            if (pline[0] == null || Math.abs(pline[0].x2 - pline[0].x1) < 1) {
                throw new AssertionError("测线创建失败");
            }
            System.out.println("STAGE4 CANVAS PASSED");
        });
    }

    private static void click(LayerCanvas canvas, int x, int y) {
        long now = System.currentTimeMillis();
        canvas.dispatchEvent(new MouseEvent(canvas, MouseEvent.MOUSE_PRESSED, now,
                InputEvent.BUTTON1_DOWN_MASK, x, y, 1, false, MouseEvent.BUTTON1));
        canvas.dispatchEvent(new MouseEvent(canvas, MouseEvent.MOUSE_RELEASED, now + 5,
                0, x, y, 1, false, MouseEvent.BUTTON1));
    }
}
