import javax.swing.*;
import java.awt.*;

public class Stage4Panel extends JPanel {
    private final LayerCanvas canvas;
    private final DefaultListModel<Stage4Action> model = new DefaultListModel<>();
    private final JList<Stage4Action> list = new JList<>(model);
    private final CardLayout cards = new CardLayout();
    private final JPanel detail = new JPanel(cards);
    private final JComboBox<String> group = new JComboBox<>();
    private final JCheckBox solve = new JCheckBox("该步后解算", true);
    private final JTextField saveName = new JTextField(12);
    private final JTextField plotName = new JTextField(12);
    private final JTextField logName = new JTextField(16);
    private final JTextField logPline = new JTextField(4);
    private final JComboBox<String> logData = new JComboBox<>(new String[]{
            "ydis", "xdisp", "syy", "sxx", "sxy", "disp", "nstress",
            "sig1", "sig2", "sstress", "xvelocity", "yvelocity"});
    private final JTextField matCable = new JTextField(5), matGrout = new JTextField(5);
    private final JTextField matYmod = new JTextField(8), matDens = new JTextField(7);
    private final JTextField matYield = new JTextField(8), matYcomp = new JTextField(8);
    private final JTextField matKbond = new JTextField(8), matSbond = new JTextField(8);
    private final JComboBox<String> plotCombo = new JComboBox<>();
    private final JPanel plotRows = new JPanel();
    private final JLabel actionLabel = new JLabel();
    private final BoltPanel bolts;
    private final PlinePanel plines;
    private JSplitPane split;
    private JButton collapse;
    private boolean collapsed;
    private boolean binding;

    public Stage4Panel(LayerCanvas canvas) {
        super(new BorderLayout());
        this.canvas = canvas;
        bolts = new BoltPanel(canvas);
        bolts.setOnGroupsChanged(this::reload);
        plines = new PlinePanel(canvas);
        for (PlotPreset p : ParameterList.plotPresets4) plotCombo.addItem(p.label);
        plotRows.setLayout(new BoxLayout(plotRows, BoxLayout.Y_AXIS));

        JPanel left = new JPanel(new BorderLayout());
        JPanel tools = new JPanel();
        tools.setLayout(new BoxLayout(tools, BoxLayout.Y_AXIS));
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        addButton(row1, "开挖", Stage4Action.Type.EXCAVATE);
        addButton(row1, "解算", Stage4Action.Type.SOLVE);
        addButton(row1, "锚杆", Stage4Action.Type.BOLT);
        addButton(row1, "测线", Stage4Action.Type.PLINE);
        addButton(row2, "材质", Stage4Action.Type.MATERIAL);
        addButton(row2, "保存", Stage4Action.Type.SAVE);
        addButton(row2, "绘图", Stage4Action.Type.PLOT);
        addButton(row2, "日志", Stage4Action.Type.LOG);
        JButton up = new JButton("上移");
        up.addActionListener(e -> move(-1));
        JButton down = new JButton("下移");
        down.addActionListener(e -> move(1));
        JButton del = new JButton("删除");
        del.addActionListener(e -> delete());
        collapse = new JButton("收起设置");
        collapse.addActionListener(e -> toggleDetail());
        row3.add(up); row3.add(down); row3.add(del); row3.add(collapse);
        tools.add(row1); tools.add(row2); tools.add(row3);
        left.add(tools, BorderLayout.NORTH);

        list.setVisibleRowCount(12);
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetail();
        });
        left.add(new JScrollPane(list), BorderLayout.CENTER);
        left.setPreferredSize(new Dimension(460, 240));

        detail.add(excavateCard(), "excavate");
        detail.add(saveCard(), "save");
        detail.add(solveCard(), "solve");
        detail.add(materialCard(), "material");
        detail.add(bolts, "bolt");
        detail.add(plines, "pline");
        detail.add(logCard(), "log");
        detail.add(plotCard(), "plot");
        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, left, detail);
        split.setDividerLocation(240);
        split.setResizeWeight(0.3);
        split.setDividerSize(6);
        add(split, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JButton generate = new JButton("生成阶段4");
        generate.addActionListener(e -> Main.generateStep4());
        bottom.add(generate);
        add(bottom, BorderLayout.SOUTH);
        reload();
    }

    private JPanel excavateCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("开挖动作"));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(1, 3, 1, 3);
        c.gridy = 0; c.gridx = 0; c.gridwidth = 2;
        panel.add(actionLabel, c);
        c.gridwidth = 1;
        c.gridy = 1; c.gridx = 0; panel.add(new JLabel("开挖分组"), c);
        c.gridx = 1; group.setEditable(true);
        group.setPreferredSize(new Dimension(240, 22));
        panel.add(group, c);
        c.gridy = 2; c.gridx = 1; panel.add(solve, c);
        JButton apply = new JButton("应用");
        apply.addActionListener(e -> applyExcavate());
        c.gridy = 3; c.gridx = 1; panel.add(apply, c);
        return panel;
    }

    private JPanel saveCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("保存动作"));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(1, 3, 1, 3);
        c.gridy = 0; c.gridx = 0; panel.add(new JLabel("保存文件名"), c);
        c.gridx = 1; saveName.setPreferredSize(new Dimension(240, 22)); panel.add(saveName, c);
        JButton apply = new JButton("应用");
        apply.addActionListener(e -> applySave());
        c.gridy = 1; c.gridx = 1; panel.add(apply, c);
        return panel;
    }

    private JPanel solveCard() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        panel.setBorder(BorderFactory.createTitledBorder("解算动作"));
        panel.add(new JLabel("执行 solve"));
        return panel;
    }

    private JPanel materialCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("锚杆/锚固剂材质"));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(1, 3, 1, 3);
        addMatRow(panel, c, 0, "锚杆材料号", matCable, "锚固剂材料号", matGrout);
        addMatRow(panel, c, 1, "cb_ymod", matYmod, "cb_dens", matDens);
        addMatRow(panel, c, 2, "cb_yield", matYield, "cb_ycomp", matYcomp);
        addMatRow(panel, c, 3, "cb_kbond", matKbond, "cb_sbond", matSbond);
        JButton apply = new JButton("应用");
        apply.addActionListener(e -> applyMaterial());
        c.gridy = 4; c.gridx = 1; panel.add(apply, c);
        return panel;
    }

    private JPanel logCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("日志输出"));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(1, 3, 1, 3);
        c.gridy = 0; c.gridx = 0; panel.add(new JLabel("日志文件名"), c);
        c.gridx = 1; c.gridwidth = 3;
        logName.setPreferredSize(new Dimension(260, 22));
        panel.add(logName, c);
        c.gridwidth = 1;
        c.gridy = 1; c.gridx = 0; panel.add(new JLabel("测线号"), c);
        c.gridx = 1; logPline.setPreferredSize(new Dimension(60, 22)); panel.add(logPline, c);
        c.gridx = 2; panel.add(new JLabel("数据"), c);
        c.gridx = 3; logData.setEditable(true);
        logData.setPreferredSize(new Dimension(150, 22)); panel.add(logData, c);
        c.gridy = 2; c.gridx = 0; c.gridwidth = 4;
        panel.add(new JLabel("测线号留空表示不依赖测线；同名自动加序号"), c);
        c.gridwidth = 1;
        JButton apply = new JButton("应用");
        apply.addActionListener(e -> applyLog());
        c.gridy = 3; c.gridx = 1; panel.add(apply, c);
        return panel;
    }

    private JPanel plotCard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("绘图项（每个绘图动作独立，可逐项设置）"));
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 1));
        row1.add(new JLabel("图名"));
        plotName.setPreferredSize(new Dimension(220, 22));
        row1.add(plotName);
        JButton applyName = new JButton("应用图名");
        applyName.addActionListener(e -> applyPlotName());
        row1.add(applyName);
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 1));
        row2.add(new JLabel("添加绘图项"));
        plotCombo.setPreferredSize(new Dimension(240, 22));
        row2.add(plotCombo);
        JButton add = new JButton("添加");
        add.addActionListener(e -> addPlotItem());
        row2.add(add);
        top.add(row1);
        top.add(row2);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(plotRows), BorderLayout.CENTER);
        return panel;
    }

    public void reload() {
        binding = true;
        Stage4Action keep = list.getSelectedValue();
        model.clear();
        for (Stage4Action a : ParameterList.stage4Actions) model.addElement(a);
        group.removeAllItems();
        for (String g : availableGroups()) group.addItem(g);
        binding = false;
        if (keep != null && ParameterList.stage4Actions.contains(keep)) {
            list.setSelectedValue(keep, true);
        } else if (model.size() > 0) {
            list.setSelectedIndex(0);
        } else {
            showDetail();
        }
    }

    private void showDetail() {
        Stage4Action a = list.getSelectedValue();
        if (a == null) {
            cards.show(detail, "solve");
            actionLabel.setText("未选择动作");
            return;
        }
        switch (a.type) {
            case BOLT:
                bolts.reload();
                if (a.name != null) bolts.selectGroup(a.name);
                cards.show(detail, "bolt");
                break;
            case PLINE:
                plines.reload();
                cards.show(detail, "pline");
                break;
            case PLOT:
                cards.show(detail, "plot");
                bindPlot(a);
                break;
            case LOG:
                cards.show(detail, "log");
                bindLog(a);
                break;
            case MATERIAL:
                cards.show(detail, "material");
                bindMaterial(a);
                break;
            case EXCAVATE:
                cards.show(detail, "excavate");
                bindExcavate(a);
                break;
            case SAVE:
                cards.show(detail, "save");
                saveName.setText(a.name == null ? "" : a.name);
                break;
            case SOLVE:
                cards.show(detail, "solve");
                break;
        }
    }

    private void bindExcavate(Stage4Action a) {
        binding = true;
        actionLabel.setText(a.type.label + (a.group == null ? "" : " · " + a.group));
        solve.setSelected(a.solve);
        if (a.group != null) group.setSelectedItem(a.group);
        binding = false;
    }

    private void bindMaterial(Stage4Action a) {
        matCable.setText(String.valueOf(a.cableMat > 0 ? a.cableMat : ParameterList.cableMaterial()));
        matGrout.setText(String.valueOf(a.groutMat > 0 ? a.groutMat : ParameterList.groutMaterial()));
        matYmod.setText(fmt(a.cbYmod > 0 ? a.cbYmod : ParameterList.cableYmod));
        matDens.setText(fmt(a.cbDensity > 0 ? a.cbDensity : ParameterList.cableDensity));
        matYield.setText(fmt(a.cbYield > 0 ? a.cbYield : ParameterList.cableYield));
        matYcomp.setText(fmt(a.cbYcomp > 0 ? a.cbYcomp : ParameterList.cableYcomp));
        matKbond.setText(fmt(a.cbKbond > 0 ? a.cbKbond : ParameterList.groutKbond));
        matSbond.setText(fmt(a.cbSbond > 0 ? a.cbSbond : ParameterList.groutSbond));
    }

    private void bindLog(Stage4Action a) {
        logName.setText(a.name == null ? "" : a.name);
        logPline.setText(a.pline == null ? "" : String.valueOf(a.pline));
        if (a.data != null) logData.setSelectedItem(a.data);
    }

    private void bindPlot(Stage4Action a) {
        plotName.setText(a.name == null ? "" : a.name);
        plotRows.removeAll();
        for (PlotItem item : a.plotItems) {
            plotRows.add(plotRow(a, item));
        }
        plotRows.revalidate();
        plotRows.repaint();
    }

    private JPanel plotRow(Stage4Action action, PlotItem item) {
        JPanel root = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        root.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        JLabel label = new JLabel(item.label);
        label.setPreferredSize(new Dimension(110, 20));
        label.setToolTipText(item.command);
        root.add(label);
        root.add(new JLabel("图头"));
        JTextField title = new JTextField(item.title, 10);
        title.setPreferredSize(new Dimension(95, 22));
        bindPlotField(title, v -> item.title = v, action);
        root.add(title);
        root.add(new JLabel("文件"));
        JTextField file = new JTextField(item.fileName, 10);
        file.setPreferredSize(new Dimension(100, 22));
        bindPlotField(file, v -> item.fileName = v, action);
        root.add(file);
        if (item.histCommand != null) {
            JCheckBox console = new JCheckBox("控制台", item.console);
            console.addActionListener(e -> {
                UndoManager.push();
                item.console = console.isSelected();
            });
            root.add(console);
        }
        JButton remove = new JButton("移除");
        remove.addActionListener(e -> {
            UndoManager.push();
            action.plotItems.remove(item);
            bindPlot(action);
            refreshActionRow(action);
        });
        root.add(remove);
        return root;
    }

    private void bindPlotField(JTextField field, java.util.function.Consumer<String> setter,
                               Stage4Action action) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                String text = field.getText().trim();
                if (text.isEmpty()) return;
                UndoManager.push();
                setter.accept(text);
                refreshActionRow(action);
            }
        });
        field.addActionListener(e -> {
            String text = field.getText().trim();
            if (text.isEmpty()) return;
            UndoManager.push();
            setter.accept(text);
            refreshActionRow(action);
        });
    }

    private void refreshActionRow(Stage4Action action) {
        int index = ParameterList.stage4Actions.indexOf(action);
        if (index >= 0) model.set(index, action);
    }

    private void addPlotItem() {
        Stage4Action a = list.getSelectedValue();
        if (a == null || a.type != Stage4Action.Type.PLOT) return;
        PlotPreset preset = findPreset((String) plotCombo.getSelectedItem());
        if (preset == null) return;
        UndoManager.push();
        a.plotItems.add(PlotItem.from(preset));
        bindPlot(a);
        refreshActionRow(a);
    }

    private PlotPreset findPreset(String label) {
        for (PlotPreset p : ParameterList.plotPresets4) {
            if (p.label.equals(label)) return p;
        }
        return null;
    }

    private void addPlotLabels(Stage4Action action, String... labels) {
        for (String label : labels) {
            PlotPreset preset = findPreset(label);
            if (preset != null) action.plotItems.add(PlotItem.from(preset));
        }
    }

    private void applyExcavate() {
        Stage4Action a = list.getSelectedValue();
        if (a == null || a.type != Stage4Action.Type.EXCAVATE) return;
        UndoManager.push();
        a.group = String.valueOf(group.getEditor().getItem()).trim();
        a.solve = solve.isSelected();
        reload();
    }

    private void applySave() {
        Stage4Action a = list.getSelectedValue();
        if (a == null || a.type != Stage4Action.Type.SAVE) return;
        UndoManager.push();
        a.name = saveName.getText().trim();
        reload();
    }

    private void applyPlotName() {
        Stage4Action a = list.getSelectedValue();
        if (a == null || a.type != Stage4Action.Type.PLOT) return;
        UndoManager.push();
        a.name = plotName.getText().trim();
        refreshActionRow(a);
    }

    private void applyMaterial() {
        Stage4Action a = list.getSelectedValue();
        if (a == null || a.type != Stage4Action.Type.MATERIAL) return;
        UndoManager.push();
        a.cableMat = parseInt(matCable.getText(), ParameterList.cableMaterial());
        a.groutMat = parseInt(matGrout.getText(), ParameterList.groutMaterial());
        a.cbYmod = parseDouble(matYmod.getText(), ParameterList.cableYmod);
        a.cbDensity = parseDouble(matDens.getText(), ParameterList.cableDensity);
        a.cbYield = parseDouble(matYield.getText(), ParameterList.cableYield);
        a.cbYcomp = parseDouble(matYcomp.getText(), ParameterList.cableYcomp);
        a.cbKbond = parseDouble(matKbond.getText(), ParameterList.groutKbond);
        a.cbSbond = parseDouble(matSbond.getText(), ParameterList.groutSbond);
        bolts.reload();
        reload();
    }

    private void applyLog() {
        Stage4Action a = list.getSelectedValue();
        if (a == null || a.type != Stage4Action.Type.LOG) return;
        UndoManager.push();
        String oldAuto = autoLogName(a.pline, a.data, a);
        String text = logPline.getText().trim();
        Integer pline = text.isEmpty() ? null : parseInt(text, 0);
        String data = String.valueOf(logData.getEditor().getItem()).trim();
        a.pline = pline;
        a.data = data;
        String current = logName.getText().trim();
        if (current.isEmpty() || current.equals(oldAuto)) {
            a.name = autoLogName(pline, data, a);
        } else {
            a.name = current;
        }
        reload();
    }

    private String autoLogName(Integer pline, String data, Stage4Action self) {
        String context = lastExcavationGroup();
        StringBuilder sb = new StringBuilder("after_");
        sb.append(context.replaceAll("[^A-Za-z0-9_\\-]+", "_"));
        if (pline != null) sb.append("_pline").append(pline);
        sb.append('_').append(data == null || data.trim().isEmpty()
                ? "data" : data.trim().replaceAll("[^A-Za-z0-9_\\-]+", "_"));
        String base = sb.append(".log").toString();
        String name = base;
        int n = 2;
        while (logNameExists(name, self)) {
            name = base.substring(0, base.length() - 4) + "_" + n++ + ".log";
        }
        return name;
    }

    private boolean logNameExists(String name, Stage4Action self) {
        for (Stage4Action a : ParameterList.stage4Actions) {
            if (a != self && a.type == Stage4Action.Type.LOG
                    && name.equals(a.name)) return true;
        }
        return false;
    }

    private void toggleDetail() {
        collapsed = !collapsed;
        detail.setVisible(!collapsed);
        collapse.setText(collapsed ? "展开设置" : "收起设置");
        SwingUtilities.invokeLater(() -> {
            if (collapsed) split.setDividerLocation(1.0);
            else split.setDividerLocation(240);
        });
    }

    private void addButton(JPanel tools, String text, Stage4Action.Type type) {
        JButton b = new JButton(text);
        b.addActionListener(e -> add(type));
        tools.add(b);
    }

    private void add(Stage4Action.Type type) {
        if (type == Stage4Action.Type.BOLT) {
            Stage4Sync.syncBoltActions();
            reload();
            int index = lastBoltActionIndex();
            if (index >= 0) {
                list.setSelectedIndex(index);
                showDetail();
            }
            bolts.startNewGroup();
            canvas.setBoltMode(true);
            return;
        }
        UndoManager.push();
        Stage4Action a = new Stage4Action(type);
        if (type == Stage4Action.Type.EXCAVATE) a.group = (String) group.getSelectedItem();
        if (type == Stage4Action.Type.SAVE) a.name = "step_4";
        if (type == Stage4Action.Type.LOG) {
            a.pline = ParameterList.plines.isEmpty() ? null : 1;
            a.data = "ydis";
            a.name = autoLogName(a.pline, a.data, a);
        }
        if (type == Stage4Action.Type.PLOT) {
            String context = lastExcavationGroup();
            a.name = "after_" + context;
            if (context.contains("openOffCut")) {
                addPlotLabels(a, "块体+锚杆");
            } else {
                addPlotLabels(a, "Y应力+块体+锚杆", "Y位移+块体+锚杆");
            }
        }
        if (type == Stage4Action.Type.MATERIAL) {
            a.cableMat = ParameterList.cableMaterial();
            a.groutMat = ParameterList.groutMaterial();
        }
        ParameterList.stage4Actions.add(a);
        model.addElement(a);
        list.setSelectedIndex(model.size() - 1);
    }

    private void move(int delta) {
        int index = list.getSelectedIndex();
        int target = index + delta;
        if (index < 0 || target < 0 || target >= model.size()) return;
        UndoManager.push();
        Stage4Action a = ParameterList.stage4Actions.remove(index);
        ParameterList.stage4Actions.add(target, a);
        model.remove(index);
        model.add(target, a);
        list.setSelectedIndex(target);
    }

    private void delete() {
        int index = list.getSelectedIndex();
        if (index < 0) return;
        UndoManager.push();
        Stage4Action a = model.get(index);
        boolean clearBolts = false;
        if (a.type == Stage4Action.Type.BOLT) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "是否同时清除所有锚杆组？\n选“否”只删除动作，锚杆数据保留但不再显示。",
                    "删除锚杆动作", JOptionPane.YES_NO_CANCEL_OPTION);
            if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) return;
            if (choice == JOptionPane.YES_OPTION) {
                ParameterList.boltGroups.clear();
                bolts.reload();
                clearBolts = true;
            }
        }
        ParameterList.stage4Actions.remove(index);
        model.remove(index);
        if (clearBolts) Stage4Sync.syncBoltActions();
        if (clearBolts) {
            reload();
            return;
        }
        if (model.size() > 0) list.setSelectedIndex(Math.min(index, model.size() - 1));
        else showDetail();
    }

    private int lastBoltActionIndex() {
        for (int i = ParameterList.stage4Actions.size() - 1; i >= 0; i--) {
            if (ParameterList.stage4Actions.get(i).type == Stage4Action.Type.BOLT) return i;
        }
        return -1;
    }

    private String lastExcavationGroup() {
        for (int i = ParameterList.stage4Actions.size() - 1; i >= 0; i--) {
            Stage4Action a = ParameterList.stage4Actions.get(i);
            if (a.type == Stage4Action.Type.EXCAVATE
                    && a.group != null && !a.group.trim().isEmpty()) {
                return a.group.trim();
            }
        }
        return "plot";
    }

    private void addMatRow(JPanel panel, GridBagConstraints c, int row, String l1,
                           JTextField f1, String l2, JTextField f2) {
        c.gridy = row; c.gridx = 0;
        panel.add(new JLabel(l1), c);
        c.gridx = 1; f1.setPreferredSize(new Dimension(95, 22)); panel.add(f1, c);
        c.gridx = 2; panel.add(new JLabel(l2), c);
        c.gridx = 3; f2.setPreferredSize(new Dimension(95, 22)); panel.add(f2, c);
    }

    private int parseInt(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private double parseDouble(String text, double fallback) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private String fmt(double v) {
        if (Math.abs(v) >= 1e6) return String.format(java.util.Locale.ROOT, "%.2e", v);
        if (v == Math.rint(v)) return String.format("%.0f", v);
        return String.format("%.2f", v);
    }

    private java.util.List<String> availableGroups() {
        java.util.List<String> names = new java.util.ArrayList<>();
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            names.add("openOffCut_" + e.name);
            for (int i = 1; i <= e.stepCount(); i++) {
                names.add("state_" + i + "_" + e.name);
            }
        }
        return names;
    }
}
