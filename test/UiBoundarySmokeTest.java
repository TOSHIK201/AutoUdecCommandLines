import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class UiBoundarySmokeTest {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PropertyPanel property = new PropertyPanel();
            LayerCanvas canvas = new LayerCanvas(property);
            property.setCanvas(canvas);
            canvas.setSize(800, 600);
            canvas.doLayout();

            StagePanel stage = new StagePanel(property, canvas);
            stage.setSize(460, 800);
            stage.doLayout();

            BoundaryPanel boundary = new BoundaryPanel(canvas);
            boundary.setSize(440, 700);
            boundary.doLayout();
            boundary.reload();

            canvas.setBoundaryMode(true);
            canvas.setSelectedBoundary(0);

            BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            canvas.paint(g);
            g.dispose();

            if (ParameterList.boundaries.isEmpty()) {
                throw new AssertionError("默认边界为空");
            }
            System.out.println("UI SMOKE PASSED, boundaries=" + ParameterList.boundaries.size());
        });
    }
}
