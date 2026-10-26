import javax.swing.JList;
import javax.swing.SwingUtilities;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class Stage4InsertTest {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                PropertyPanel property = new PropertyPanel();
                LayerCanvas canvas = new LayerCanvas(property);
                property.setCanvas(canvas);
                Stage4Panel panel = new Stage4Panel(canvas);
                JList<?> list = (JList<?>) read(panel, "list");
                List<Stage4Action> actions = ParameterList.stage4Actions;

                int before = actions.size();
                list.setSelectedIndex(2);
                invoke(panel, "add", Stage4Action.Type.SAVE);
                check("insert size", actions.size() == before + 1);
                check("insert below selection",
                        actions.get(3).type == Stage4Action.Type.SAVE);
                check("select inserted", list.getSelectedIndex() == 3);

                list.clearSelection();
                int n2 = actions.size();
                invoke(panel, "add", Stage4Action.Type.SOLVE);
                check("append without selection", actions.size() == n2 + 1
                        && actions.get(n2).type == Stage4Action.Type.SOLVE);
                check("select appended", list.getSelectedIndex() == n2);

                list.setSelectedIndex(2);
                Stage4Action anchor = actions.get(2);
                invoke(panel, "add", Stage4Action.Type.BOLT);
                BoltGroup g = new BoltGroup("BTEST");
                g.x1 = 100; g.y1 = 26.4; g.x2 = 108; g.y2 = 26.4;
                g.count = 8; g.spacing = 1;
                g.rebuild();
                ParameterList.boltGroups.add(g);
                Stage4Sync.syncBoltActions();
                invoke(panel, "boltGroupAdded", g);

                int anchorIndex = actions.indexOf(anchor);
                int boltIndex = indexOfBolt(actions, "BTEST");
                check("bolt inserted below selection", boltIndex == anchorIndex + 1);
                check("bolt selected", list.getSelectedIndex() == boltIndex);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("STAGE4 INSERT TEST PASSED");
    }

    private static int indexOfBolt(List<Stage4Action> actions, String name) {
        for (int i = 0; i < actions.size(); i++) {
            Stage4Action a = actions.get(i);
            if (a.type == Stage4Action.Type.BOLT && name.equals(a.name)) return i;
        }
        return -1;
    }

    private static void invoke(Object target, String name, Object arg) throws Exception {
        Class<?> type = arg instanceof Stage4Action.Type
                ? Stage4Action.Type.class : arg.getClass();
        Method m = target.getClass().getDeclaredMethod(name, type);
        m.setAccessible(true);
        m.invoke(target, arg);
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
