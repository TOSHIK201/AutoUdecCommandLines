import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

public class StagePanel extends JPanel {
    private final LayerCanvas canvas;
    private JCheckBox call2, call3;
    private final JTabbedPane tabs = new JTabbedPane();
    private JTabbedPane subStage1;

    public StagePanel(PropertyPanel propertyPanel, LayerCanvas canvas) {
        this.canvas = canvas;
        setLayout(new BorderLayout());
        tabs.addTab("阶段1 岩层", buildStage1(propertyPanel));
        tabs.addTab("阶段2 节理", buildStage2(propertyPanel));
        tabs.addTab("阶段3 边界", buildStage3());
        tabs.addTab("阶段4 支护/开挖", new Stage4Panel(canvas));
        tabs.addChangeListener(e -> {
            int i = tabs.getSelectedIndex();
            canvas.setActiveStage(i);
            if (i == 0 || i == 1) propertyPanel.bindData(canvas.getSelectedIndex());
        });
        add(tabs, BorderLayout.CENTER);
    }

    public int getSelectedTab() {
        return tabs.getSelectedIndex();
    }

    public void setSelectedTab(int index) {
        if (index >= 0 && index < tabs.getTabCount()) tabs.setSelectedIndex(index);
    }

    public int getSelectedSubTab() {
        return subStage1 == null ? 0 : subStage1.getSelectedIndex();
    }

    public void setSelectedSubTab(int index) {
        if (subStage1 != null && index >= 0 && index < subStage1.getTabCount()) {
            subStage1.setSelectedIndex(index);
        }
    }

    public void setAutoRun(boolean auto) {
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

    private JPanel buildStage1(PropertyPanel propertyPanel) {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new BorderLayout());

        JPanel modelRow = new JPanel(new GridBagLayout());
        GridBagConstraints c = grid();
        JTextField fLength = new JTextField(String.valueOf(ParameterList.modelLength), 6);
        JTextField fTitle = new JTextField(ParameterList.step1Title, 18);
        addRow(modelRow, c, 0, "模型总长(m)", fLength, 95);
        addRow(modelRow, c, 1, "阶段1图标题", fTitle, 240);
        bindDouble(fLength, v -> ParameterList.modelLength = v);
        bindString(fTitle, v -> ParameterList.step1Title = v);
        top.add(modelRow, BorderLayout.NORTH);

        JLabel hint = new JLabel("开挖属性可独立新建；“设为开挖煤层”用选中岩层中部快速建立");
        hint.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        top.add(hint, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);

        subStage1 = new JTabbedPane();
        subStage1.addTab("岩层属性", new JScrollPane(propertyPanel.getRockPanel()));
        ExcavationPropertyPanel exPanel = new ExcavationPropertyPanel(canvas);
        subStage1.addTab("开挖属性", exPanel);
        subStage1.addChangeListener(e -> {
            if (subStage1.getSelectedIndex() == 1) exPanel.reload();
        });
        panel.add(subStage1, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.add(new StagePlotPanel("阶段1绘图", ParameterList.plotPresets1, false, canvas::repaint),
                BorderLayout.NORTH);
        south.add(bottomBar("自动调用阶段2", ParameterList.s1Calls2, v -> ParameterList.s1Calls2 = v,
                "生成阶段1", Main::generateStep1), BorderLayout.SOUTH);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildStage2(PropertyPanel propertyPanel) {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints c = grid();
        JTextField fTitle = new JTextField(ParameterList.step2Title, 18);
        addRow(top, c, 0, "阶段2图标题", fTitle, 240);
        bindString(fTitle, v -> ParameterList.step2Title = v);
        panel.add(top, BorderLayout.NORTH);
        panel.add(propertyPanel.getJointPanel(), BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout());
        south.add(new StagePlotPanel("阶段2绘图", ParameterList.plotPresets2, false, canvas::repaint),
                BorderLayout.NORTH);
        south.add(bottomBar("自动调用阶段3", ParameterList.s2Calls3, v -> ParameterList.s2Calls3 = v,
                "生成阶段2", Main::generateStep2), BorderLayout.SOUTH);
        panel.add(south, BorderLayout.SOUTH);
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
    private GridBagConstraints grid() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 4, 2, 4);
        return c;
    }
    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent field, int width) {
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 0;
        if (field instanceof JTextField) {
            field.setPreferredSize(new Dimension(width, 22));
        }
        panel.add(field, c);
        c.gridx = 2;
        c.weightx = 1.0;
        panel.add(new JLabel(""), c);
        c.weightx = 0;
    }
    private void bindDouble(JTextField field, DoubleConsumer setter) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { saveDouble(field, setter); }
        });
        field.addActionListener(e -> saveDouble(field, setter));
    }

    private void saveDouble(JTextField field, DoubleConsumer setter) {
        try {
            String text = field.getText().trim();
            if (!text.isEmpty()) {
                UndoManager.push();
                setter.accept(Double.parseDouble(text));
                if (canvas != null) canvas.repaint();
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void bindInt(JTextField field, IntConsumer setter) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { saveInt(field, setter); }
        });
        field.addActionListener(e -> saveInt(field, setter));
    }
    private void saveInt(JTextField field, IntConsumer setter) {
        try {
            String text = field.getText().trim();
            if (!text.isEmpty()) {
                UndoManager.push();
                setter.accept(Integer.parseInt(text));
                if (canvas != null) canvas.repaint();
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void bindString(JTextField field, Consumer<String> setter) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { saveString(field, setter); }
        });
        field.addActionListener(e -> saveString(field, setter));
    }

    private void saveString(JTextField field, Consumer<String> setter) {
        String text = field.getText().trim();
        if (!text.isEmpty()) {
            UndoManager.push();
            setter.accept(text);
        }
    }
}
