import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class UiPreview {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                PropertyPanel property = new PropertyPanel();
                LayerCanvas canvas = new LayerCanvas(property);
                property.setCanvas(canvas);
                StagePanel stage = new StagePanel(property, canvas);
                stage.setSelectedTab(2);

                JFrame frame = new JFrame();
                frame.setUndecorated(true);
                frame.setLayout(new BorderLayout());
                frame.add(canvas, BorderLayout.CENTER);
                frame.add(stage, BorderLayout.EAST);
                frame.setSize(1280, 860);
                frame.doLayout();
                canvas.setSize(820, 860);
                stage.setSize(460, 860);
                canvas.doLayout();
                stage.doLayout();
                layoutAll(stage);

                BufferedImage image = new BufferedImage(1280, 860, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics();
                canvas.paint(g);
                g.translate(820, 0);
                stage.paint(g);
                g.dispose();
                ImageIO.write(image, "png", new File("test_out/stage3.png"));
                frame.dispose();
                System.out.println("preview written");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void layoutAll(Container c) {
        c.doLayout();
        for (Component child : c.getComponents()) {
            if (child instanceof Container) layoutAll((Container) child);
        }
    }
}
