import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;

// 开挖区域属性改动后，阶段4 的“开挖区域”下拉应重新取自最新开挖层
public class Stage4GroupRefreshTest {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                run();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("STAGE4 GROUP REFRESH TEST PASSED");
    }

    private static void run() throws Exception {
        PropertyPanel property = new PropertyPanel();
        LayerCanvas canvas = new LayerCanvas(property);
        property.setCanvas(canvas);
        StagePanel stages = new StagePanel(property, canvas);
        stages.setSelectedTab(3);

        Stage4Panel panel = find(stages, Stage4Panel.class);
        check("stage4 panel exists", panel != null);
        JComboBox<?> combo = (JComboBox<?>) read(panel, "group");
        int before = combo.getItemCount();
        check("baseline groups", before > 0);

        ExcavationLayer e = ParameterList.excavationLayers.get(0);
        e.steps = e.steps + 3;
        ParameterList.syncExcavationGeometry();

        stages.setSelectedTab(3);
        int after = combo.getItemCount();
        check("combo refreshed on entering stage 4 (before=" + before + " after=" + after + ")",
                after == before + 3);
        check("newest step group listed", contains(combo,
                "state_" + e.steps + "_" + e.name));
    }

    private static boolean contains(JComboBox<?> combo, String item) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (item.equals(combo.getItemAt(i))) return true;
        }
        return false;
    }

    private static <T> T find(Container root, Class<T> type) {
        for (Component c : root.getComponents()) {
            if (type.isInstance(c)) return type.cast(c);
            if (c instanceof Container) {
                T found = find((Container) c, type);
                if (found != null) return found;
            }
        }
        return null;
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
