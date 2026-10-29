import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import java.lang.reflect.Field;
import java.util.List;

public class UiStructureTest {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                PropertyPanel property = new PropertyPanel();
                LayerCanvas canvas = new LayerCanvas(property);
                property.setCanvas(canvas);
                StagePanel stage = new StagePanel(property, canvas);

                List<?> buttons = (List<?>) read(stage, "stageButtons");
                check("stage tabs", buttons.size() == 4);
                check("stage1 title", ((JToggleButton) buttons.get(0)).getText()
                        .equals("阶段1 建模与节理"));
                check("stage2 title", ((JToggleButton) buttons.get(1)).getText()
                        .equals("阶段2 网格与材料"));
                check("stage1 first", ((JToggleButton) buttons.get(0)).isSelected());
                // 阶段页面按需构建：逐个打开后再检查卡片齐全
                for (int i = 0; i < 4; i++) stage.setSelectedTab(i);
                JPanel content = (JPanel) read(stage, "stageContent");
                check("stage cards", content.getComponentCount() == 4);

                JTabbedPane sub1 = (JTabbedPane) read(stage, "subStage1");
                check("stage1 sub tabs", sub1.getTabCount() == 3);
                check("stage1 layer tab", sub1.getTitleAt(0).equals("岩层几何"));
                check("stage1 joint tab", sub1.getTitleAt(1).equals("节理几何"));
                check("stage1 excavation tab", sub1.getTitleAt(2).equals("开挖几何"));

                JTabbedPane sub2 = (JTabbedPane) read(stage, "subStage2");
                check("stage2 sub tabs", sub2.getTabCount() == 2);
                check("stage2 rock tab", sub2.getTitleAt(0).equals("岩体材料与网格"));
                check("stage2 joint tab", sub2.getTitleAt(1).equals("节理材料"));
                check("boundary ui types", BoundaryCondition.uiTypes().length == 3);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("UI STRUCTURE TEST PASSED");
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
