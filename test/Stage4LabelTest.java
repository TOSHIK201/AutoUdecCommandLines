import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.lang.reflect.Field;

public class Stage4LabelTest {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                PropertyPanel property = new PropertyPanel();
                LayerCanvas canvas = new LayerCanvas(property);
                property.setCanvas(canvas);
                Stage4Panel panel = new Stage4Panel(canvas);

                JLabel region = (JLabel) read(panel, "regionLabel");
                check("default region label", region.getText().contains("开切眼"));
                check("default region range", region.getText().contains("100.00"));

                JComboBox<?> combo = (JComboBox<?>) read(panel, "group");
                check("group count", combo.getItemCount() >= 8);
                javax.swing.ListCellRenderer<Object> renderer =
                        (javax.swing.ListCellRenderer<Object>) combo.getRenderer();
                java.awt.Component item = renderer
                        .getListCellRendererComponent(new javax.swing.JList<>(), "state_1_9", 0, false, false);
                check("combo label", item instanceof JLabel
                        && "9# 第 1 步（state_1_9）".equals(((JLabel) item).getText()));
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("STAGE4 LABEL TEST PASSED");
    }

    private static Object read(Stage4Panel panel, String field) throws Exception {
        Field f = Stage4Panel.class.getDeclaredField(field);
        f.setAccessible(true);
        return f.get(panel);
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
