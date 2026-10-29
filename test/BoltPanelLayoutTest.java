import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.lang.reflect.Field;

// 阶段4 锚杆设置里，锚杆组列表与单根锚杆列表在任何窗口高度下都不得重叠
public class BoltPanelLayoutTest {
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
        System.out.println("BOLT PANEL LAYOUT TEST PASSED");
    }

    private static void run() throws Exception {
        ParameterList.syncFromLayers();
        ParameterList.boltGroups.clear();
        ParameterList.stage4Actions.clear();

        BoltGroup group = new BoltGroup("BOLT1");
        group.x1 = 100.5; group.y1 = 26.4; group.x2 = 107.5; group.y2 = 26.4;
        group.count = 8;
        group.length = 2.4;
        group.rebuild();
        ParameterList.boltGroups.add(group);

        Stage4Action boltAction = new Stage4Action(Stage4Action.Type.BOLT);
        boltAction.name = "BOLT1";
        ParameterList.stage4Actions.add(boltAction);

        PropertyPanel property = new PropertyPanel();
        LayerCanvas canvas = new LayerCanvas(property);
        property.setCanvas(canvas);
        StagePanel stages = new StagePanel(property, canvas);
        stages.setSelectedTab(3);

        Stage4Panel panel = find(stages, Stage4Panel.class);
        check("stage4 panel exists", panel != null);
        check("bolt card shown", showBoltCard(panel));

        JList<?> groupList = (JList<?>) read(panel, "bolts", "groupList");
        JList<?> boltList = (JList<?>) read(panel, "bolts", "boltList");
        JScrollPane groupScroll = (JScrollPane) SwingUtilities
                .getAncestorOfClass(JScrollPane.class, groupList);
        JScrollPane boltScroll = (JScrollPane) SwingUtilities
                .getAncestorOfClass(JScrollPane.class, boltList);

        for (int height : new int[]{420, 600, 780}) {
            stages.setSize(460, height);
            layoutAll(stages);
            Rectangle g = rect(groupScroll, panel);
            Rectangle b = rect(boltScroll, panel);
            check("heights positive at " + height, g.height > 0 && b.height > 0);
            check("no overlap at height " + height + " (groups y " + g.y + ".." + (g.y + g.height)
                    + ", bolts y " + b.y + ".." + (b.y + b.height) + ")", !g.intersects(b));
        }
    }

    private static boolean showBoltCard(Stage4Panel panel) throws Exception {
        @SuppressWarnings("unchecked")
        javax.swing.JList<Stage4Action> list =
                (javax.swing.JList<Stage4Action>) read(panel, "list");
        for (int i = 0; i < list.getModel().getSize(); i++) {
            if (list.getModel().getElementAt(i).type == Stage4Action.Type.BOLT) {
                list.setSelectedIndex(i);
                layoutAll(panel);
                JList<?> boltList = (JList<?>) read(panel, "bolts", "boltList");
                return boltList.isShowing() || boltList.getParent() != null;
            }
        }
        return false;
    }

    private static Rectangle rect(Component c, Component base) {
        return SwingUtilities.convertRectangle(c.getParent(), c.getBounds(), base);
    }

    private static void layoutAll(Container c) {
        c.doLayout();
        for (Component child : c.getComponents()) {
            if (child instanceof Container) layoutAll((Container) child);
        }
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

    private static Object read(Object owner, String... path) throws Exception {
        Object current = owner;
        for (String name : path) {
            Field f = current.getClass().getDeclaredField(name);
            f.setAccessible(true);
            current = f.get(current);
        }
        return current;
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
