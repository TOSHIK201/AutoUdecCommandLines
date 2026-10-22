import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

public class DragExcavationTest {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PropertyPanel property = new PropertyPanel();
            LayerCanvas canvas = new LayerCanvas(property);
            property.setCanvas(canvas);
            canvas.setSize(800, 600);
            canvas.doLayout();
            canvas.fitToScreen();

            ExcavationLayer[] made = new ExcavationLayer[1];
            canvas.setExcavationFactory(() -> new ExcavationLayer("拖框测试", 0, 0, 0, 0));
            canvas.setExcavationCreated(e -> {
                made[0] = e;
                ParameterList.excavationLayers.add(e);
                canvas.setSelectedExcavation(0);
            });
            canvas.setExcavationMode(true);

            long now = System.currentTimeMillis();
            canvas.dispatchEvent(new MouseEvent(canvas, MouseEvent.MOUSE_PRESSED, now,
                    InputEvent.BUTTON1_DOWN_MASK, 200, 300, 1, false, MouseEvent.BUTTON1));
            canvas.dispatchEvent(new MouseEvent(canvas, MouseEvent.MOUSE_DRAGGED, now + 10,
                    InputEvent.BUTTON1_DOWN_MASK, 420, 420, 0, false, MouseEvent.NOBUTTON));
            canvas.dispatchEvent(new MouseEvent(canvas, MouseEvent.MOUSE_RELEASED, now + 20,
                    0, 420, 420, 1, false, MouseEvent.BUTTON1));

            if (made[0] == null) throw new AssertionError("没有创建开挖区域");
            if (made[0].x2 - made[0].x1 < 0.1 || made[0].y2 - made[0].y1 < 0.1) {
                throw new AssertionError("矩形尺寸异常");
            }
            System.out.println("DRAG TEST PASSED: "
                    + made[0].x1 + "," + made[0].y1 + " -> " + made[0].x2 + "," + made[0].y2);

            double oldX2 = made[0].x2;
            double scale = canvas.getScale();
            int h = canvas.getHeight();
            int hx = (int) Math.round(canvas.getOriginX() + made[0].x2 * scale);
            int hy = (int) Math.round(h - canvas.getOriginY() - made[0].y2 * scale);
            canvas.dispatchEvent(new MouseEvent(canvas, MouseEvent.MOUSE_PRESSED, now + 100,
                    InputEvent.BUTTON1_DOWN_MASK, hx, hy, 1, false, MouseEvent.BUTTON1));
            canvas.dispatchEvent(new MouseEvent(canvas, MouseEvent.MOUSE_DRAGGED, now + 110,
                    InputEvent.BUTTON1_DOWN_MASK, hx - 60, hy + 20, 0, false, MouseEvent.NOBUTTON));
            canvas.dispatchEvent(new MouseEvent(canvas, MouseEvent.MOUSE_RELEASED, now + 120,
                    0, hx - 60, hy + 20, 1, false, MouseEvent.BUTTON1));
            if (Math.abs(made[0].x2 - oldX2) < 0.1) throw new AssertionError("角点拖动未生效");
            System.out.println("HANDLE TEST PASSED: x2 " + oldX2 + " -> " + made[0].x2);
        });
    }
}
