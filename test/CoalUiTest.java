import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;

public class CoalUiTest {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                PropertyPanel property = new PropertyPanel();
                LayerCanvas canvas = new LayerCanvas(property);
                property.setCanvas(canvas);
                StagePanel stage = new StagePanel(property, canvas);

                JTabbedPane sub1 = (JTabbedPane) read(stage, "subStage1");
                JPanel excavation = (JPanel) sub1.getComponentAt(2);
                Object form = read(excavation, "form");
                check("coal button in parameter form",
                        findButton((Container) form, "开挖煤层") != null);
                check("old button removed",
                        findButton(excavation, "设为开挖煤层") == null);

                JList<?> exList = (JList<?>) read(excavation, "list");
                exList.setSelectedIndex(1);
                check("canvas layer follows list", canvas.getSelectedIndex()
                        == ParameterList.excavationLayers.get(1).layerIndex);
                ExcavationLayer first = ParameterList.excavationLayers.get(0);
                double firstX1 = first.x1;
                double firstX2 = first.x2;
                findButton((Container) form, "开挖煤层").doClick();
                check("second excavation kept", exList.getSelectedIndex() == 1);
                check("first excavation untouched",
                        Math.abs(first.x1 - firstX1) < 1e-9
                                && Math.abs(first.x2 - firstX2) < 1e-9);

                BoltPanel bolts = new BoltPanel(canvas);
                JComboBox<?> bind = (JComboBox<?>) read(bolts, "bindBox");
                check("absolute option", "绝对坐标".equals(bind.getItemAt(0)));
                boolean found = false;
                for (int i = 1; i < bind.getItemCount(); i++) {
                    if ("9# 开切眼".equals(bind.getItemAt(i))) found = true;
                }
                check("open cut option", found);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("COAL UI TEST PASSED");
    }

    private static JButton findButton(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton && text.equals(((JButton) c).getText())) return (JButton) c;
            if (c instanceof Container) {
                JButton found = findButton((Container) c, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static Object read(Object target, String field) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        return f.get(target);
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
