import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class StagePanel extends JPanel {
    private final LayerCanvas canvas;
    private final PropertyPanel propertyPanel;
    private JCheckBox call2, call3;
    private final CardLayout stageCards = new CardLayout();
    private final JPanel stageContent = new JPanel(stageCards);
    private final ButtonGroup stageGroup = new ButtonGroup();
    private final java.util.List<JToggleButton> stageButtons = new java.util.ArrayList<>();
    private int selectedStage = 0;
    private JTabbedPane subStage1;
    private JTabbedPane subStage2;
    private Stage4Panel stage4;
    private final boolean[] built = new boolean[4];
    private boolean autoRun;

    public StagePanel(PropertyPanel propertyPanel, LayerCanvas canvas) {
        this.canvas = canvas;
        this.propertyPanel = propertyPanel;
        setLayout(new BorderLayout());
        add(buildStageHeader(), BorderLayout.NORTH);
        add(stageContent, BorderLayout.CENTER);
        selectStage(0);
    }

    public int getSelectedTab() {
        return selectedStage;
    }

    public void setSelectedTab(int index) {
        selectStage(index);
    }

    public int getSelectedSubTab() {
        if (selectedStage == 0 && subStage1 != null) return subStage1.getSelectedIndex();
        if (selectedStage == 1 && subStage2 != null) return subStage2.getSelectedIndex();
        return 0;
    }

    public void setSelectedSubTab(int index) {
        if (subStage1 != null && index >= 0 && index < subStage1.getTabCount()) {
            subStage1.setSelectedIndex(index);
        }
        if (subStage2 != null && index >= 0 && index < subStage2.getTabCount()) {
            subStage2.setSelectedIndex(index);
        }
    }

    public void setAutoRun(boolean auto) {
        autoRun = auto;
        if (auto) {
            ParameterList.s1Calls2 = true;
            ParameterList.s2Calls3 = true;
        }
        if (call2 != null) {
            if (auto) call2.setSelected(true);
            call2.setEnabled(!auto);
        }
        if (call3 != null) {
            if (auto) call3.setSelected(true);
            call3.setEnabled(!auto);
        }
    }

    // 阶段页面按需构建：启动时只建当前阶段，避免一次性加载全部 Swing 组件
    private void ensureStage(int index) {
        if (index < 0 || index >= built.length || built[index]) return;
        built[index] = true;
        switch (index) {
            case 0:
                stageContent.add(buildStage1(propertyPanel), "0");
                break;
            case 1:
                stageContent.add(buildStage2(propertyPanel), "1");
                break;
            case 2:
                stageContent.add(buildStage3(), "2");
                break;
            default:
                stage4 = new Stage4Panel(canvas);
                stageContent.add(stage4, "3");
        }
    }

    private JPanel buildStageHeader() {
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.add(stageButton("阶段1 建模与节理", 0));
        JPanel lower = new JPanel(new GridLayout(1, 3));
        lower.add(stageButton("阶段2 网格与材料", 1));
        lower.add(stageButton("阶段3 边界", 2));
        lower.add(stageButton("阶段4 支护/开挖", 3));
        header.add(lower);
        return header;
    }

    private JToggleButton stageButton(String text, int index) {
        JToggleButton button = new JToggleButton(text);
        button.setFocusPainted(false);
        button.setMargin(new Insets(3, 4, 3, 4));
        button.addActionListener(e -> selectStage(index));
        stageGroup.add(button);
        stageButtons.add(button);
        return button;
    }

    private void selectStage(int index) {
        if (index < 0 || index >= stageButtons.size()) return;
        selectedStage = index;
        ensureStage(index);
        stageCards.show(stageContent, String.valueOf(index));
        stageButtons.get(index).setSelected(true);
        canvas.setActiveStage(index);
        // 开挖区域可在阶段1修改，进入阶段4 时重建下拉列表，避免列出旧的开挖布
        if (index == 3 && stage4 != null) stage4.reload();
        if (index == 0 || index == 1) {
            propertyPanel.bindData(canvas.getSelectedIndex());
        }
    }

    private JPanel buildStage1(PropertyPanel propertyPanel) {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new BorderLayout());

        JPanel modelRow = new JPanel(new GridBagLayout());
        GridBagConstraints c = StageUiSupport.grid();
        JTextField fLength = new JTextField(String.valueOf(ParameterList.modelLength), 6);
        JTextField fTitle = new JTextField(ParameterList.step1Title, 18);
        StageUiSupport.addRow(modelRow, c, 0, "模型总长(m)", fLength, 95);
        StageUiSupport.addRow(modelRow, c, 1, "阶段1图标题", fTitle, 240);
        StageUiSupport.bindDouble(fLength, v -> ParameterList.modelLength = v, canvas);
        StageUiSupport.bindString(fTitle, v -> ParameterList.step1Title = v);
        top.add(modelRow, BorderLayout.NORTH);

        panel.add(top, BorderLayout.NORTH);

        subStage1 = new JTabbedPane();
        subStage1.addTab("岩层几何", new JScrollPane(propertyPanel.getLayerPanel()));
        subStage1.addTab("节理几何", propertyPanel.getJointGeometryPanel());
        ExcavationPropertyPanel exPanel = new ExcavationPropertyPanel(canvas);
        subStage1.addTab("开挖几何", exPanel);
        subStage1.addChangeListener(e -> {
            if (subStage1.getSelectedIndex() == 2) exPanel.reload();
        });
        panel.add(subStage1, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.add(new StagePlotPanel("阶段1绘图", ParameterList.plotPresets1, false, canvas::repaint),
                BorderLayout.NORTH);
        south.add(bottomBar("自动调用阶段2", ParameterList.s1Calls2, v -> ParameterList.s1Calls2 = v,
                "生成阶段1", Main::generateStep1), BorderLayout.SOUTH);
        panel.add(south, BorderLayout.SOUTH);
        if (autoRun && call2 != null) call2.setEnabled(false);
        return panel;
    }

    private JPanel buildStage2(PropertyPanel propertyPanel) {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints c = StageUiSupport.grid();
        JTextField fTitle = new JTextField(ParameterList.step2Title, 18);
        StageUiSupport.addRow(top, c, 0, "阶段2图标题", fTitle, 240);
        StageUiSupport.bindString(fTitle, v -> ParameterList.step2Title = v);
        panel.add(top, BorderLayout.NORTH);

        subStage2 = new JTabbedPane();
        subStage2.addTab("岩体材料与网格", new JScrollPane(propertyPanel.getRockMaterialPanel()));
        subStage2.addTab("节理材料", new JScrollPane(propertyPanel.getJointMaterialPanel()));
        panel.add(subStage2, BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout());
        south.add(new StagePlotPanel("阶段2绘图", ParameterList.plotPresets2, false, canvas::repaint),
                BorderLayout.NORTH);
        south.add(bottomBar("自动调用阶段3", ParameterList.s2Calls3, v -> ParameterList.s2Calls3 = v,
                "生成阶段2", Main::generateStep2), BorderLayout.SOUTH);
        panel.add(south, BorderLayout.SOUTH);
        if (autoRun && call3 != null) call3.setEnabled(false);
        return panel;
    }

    private JPanel buildStage3() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new BoundaryPanel(canvas), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        JButton gen3 = new JButton("生成阶段3");
        gen3.addActionListener(e -> Main.generateStep3());
        bottom.add(gen3);
        JPanel south = new JPanel(new BorderLayout());
        south.add(new StagePlotPanel("阶段3绘图", ParameterList.plotPresets3, true, canvas::repaint),
                BorderLayout.NORTH);
        south.add(bottom, BorderLayout.SOUTH);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel bottomBar(String checkText, boolean selected, Consumer<Boolean> setter,
                             String buttonText, Runnable action) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        JCheckBox check = new JCheckBox(checkText, selected);
        check.setToolTipText("生成后在文件末尾调用下一阶段");
        if (checkText.contains("阶段2")) call2 = check;
        if (checkText.contains("阶段3")) call3 = check;
        check.addActionListener(e -> {
            UndoManager.push();
            setter.accept(check.isSelected());
        });
        JButton button = new JButton(buttonText);
        button.setToolTipText("生成当前阶段的 UDEC 命令文件");
        button.addActionListener(e -> action.run());
        bar.add(check);
        bar.add(button);
        return bar;
    }
}
