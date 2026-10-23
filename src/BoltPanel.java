import javax.swing.*;
import java.awt.*;

public class BoltPanel extends JPanel {
    private final LayerCanvas canvas;
    private final DefaultListModel<BoltGroup> groups = new DefaultListModel<>();
    private final JList<BoltGroup> groupList = new JList<>(groups);
    private final DefaultListModel<Bolt> bolts = new DefaultListModel<>();
    private final JList<Bolt> boltList = new JList<>(bolts);
    private final JTextField x1 = new JTextField(5), y1 = new JTextField(5);
    private final JTextField x2 = new JTextField(5), y2 = new JTextField(5);
    private final JTextField spacing = new JTextField(4), count = new JTextField(4);
    private final JTextField length = new JTextField(5);
    private final JTextField nodes = new JTextField(4), area = new JTextField(7);
    private final JTextField preten = new JTextField(7);
    private final JLabel gapLabel = new JLabel();
    private final JComboBox<Integer> cableMat = new JComboBox<>();
    private final JComboBox<Integer> groutMat = new JComboBox<>();
    private final JTextField rotate = new JTextField(5);
    private final JTextField bAngle = new JTextField(5), bLength = new JTextField(5);
    private final JTextField bNodes = new JTextField(4), bArea = new JTextField(7);
    private final JTextField bPreten = new JTextField(7);
    private final JComboBox<Integer> bCableMat = new JComboBox<>();
    private final JComboBox<Integer> bGroutMat = new JComboBox<>();
    private boolean binding;
    private Runnable onGroupsChanged;

    public BoltPanel(LayerCanvas canvas) {
        super(new BorderLayout());
        this.canvas = canvas;

        cableMat.setToolTipText("块体、锚杆、锚固剂共用 mat 材料表；默认顺延岩层最大材料号");
        groutMat.setToolTipText("默认取锚杆材料号 +1；节理 jmat 是独立表，不参与此顺延");

        JPanel top = new JPanel(new BorderLayout());
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        JButton draw = new JButton("画安装线");
        draw.addActionListener(e -> canvas.setBoltMode(true));
        JButton apply = new JButton("应用组参数");
        apply.addActionListener(e -> applyGroup());
        JButton del = new JButton("删除组");
        del.addActionListener(e -> deleteGroup());
        JButton clear = new JButton("清空全部锚杆");
        clear.addActionListener(e -> clearAll());
        tools.add(draw);
        tools.add(apply);
        tools.add(del);
        tools.add(clear);
        tools.add(new JLabel("批量旋转(°):"));
        tools.add(rotate);
        JButton rot = new JButton("旋转选中");
        rot.addActionListener(e -> rotateSelected());
        tools.add(rot);
        top.add(tools, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("锚杆组参数"));
        GridBagConstraints c = grid();
        row(form, c, 0, "安装线 X1/Y1", x1, y1);
        row(form, c, 1, "安装线 X2/Y2", x2, y2);
        row(form, c, 2, "间排距(m)", spacing, "根数", count);
        row(form, c, 3, "长度", length, "节点数", nodes);
        row(form, c, 4, "面积", area, "预紧力", preten);
        row(form, c, 5, "锚杆材料", cableMat, "锚固剂", groutMat);
        singleRow(form, c, 6, "自动留空", gapLabel);
        top.add(form, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        groupList.setVisibleRowCount(4);
        groupList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) bindGroup();
        });
        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createTitledBorder("锚杆组"));
        center.add(new JScrollPane(groupList), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        JPanel boltForm = new JPanel(new GridBagLayout());
        boltForm.setBorder(BorderFactory.createTitledBorder("单根锚杆（选中后可改）"));
        GridBagConstraints bc = grid();
        row(boltForm, bc, 0, "角度", bAngle, "长度", bLength);
        row(boltForm, bc, 1, "节点数", bNodes, "面积", bArea);
        row(boltForm, bc, 2, "锚杆材料", bCableMat, "锚固剂", bGroutMat);
        singleRow(boltForm, bc, 3, "预紧力", bPreten);
        JButton applyBolt = new JButton("应用单根");
        applyBolt.addActionListener(e -> applyBolt());
        bc.gridy = 4; bc.gridx = 0; bc.gridwidth = 3;
        boltForm.add(applyBolt, bc);
        boltList.setVisibleRowCount(5);
        boltList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        boltList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                bindBolt();
                updateCanvasSelection();
            }
        });
        bottom.add(new JScrollPane(boltList), BorderLayout.CENTER);
        bottom.add(boltForm, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        canvas.setBoltFactory(this::newFromForm);
        canvas.setBoltCreated(this::added);
        refreshMaterialBoxes();
        reload();
    }

    public void reload() {
        refreshMaterialBoxes();
        binding = true;
        groups.clear();
        for (BoltGroup g : ParameterList.boltGroups) groups.addElement(g);
        binding = false;
        if (groups.size() > 0) groupList.setSelectedIndex(0);
        else clearGroupForm();
    }

    private void refreshMaterialBoxes() {
        Object c = cableMat.getSelectedItem();
        Object g = groutMat.getSelectedItem();
        Object bc = bCableMat.getSelectedItem();
        Object bg = bGroutMat.getSelectedItem();
        cableMat.removeAllItems();
        for (Integer id : ParameterList.cableMaterialIds()) cableMat.addItem(id);
        groutMat.removeAllItems();
        for (Integer id : ParameterList.groutMaterialIds()) groutMat.addItem(id);
        bCableMat.removeAllItems();
        for (Integer id : ParameterList.cableMaterialIds()) bCableMat.addItem(id);
        bGroutMat.removeAllItems();
        for (Integer id : ParameterList.groutMaterialIds()) bGroutMat.addItem(id);
        if (c != null) cableMat.setSelectedItem(c);
        if (g != null) groutMat.setSelectedItem(g);
        if (bc != null) bCableMat.setSelectedItem(bc);
        if (bg != null) bGroutMat.setSelectedItem(bg);
    }

    public void setOnGroupsChanged(Runnable listener) {
        onGroupsChanged = listener;
    }

    public void selectGroup(String groupName) {
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).name.equals(groupName)) {
                groupList.setSelectedIndex(i);
                return;
            }
        }
    }

    public void startNewGroup() {
        groupList.clearSelection();
        clearGroupForm();
    }

    private BoltGroup newFromForm() {
        BoltGroup g = new BoltGroup("BOLT" + (ParameterList.boltGroups.size() + 1));
        g.spacing = parse(spacing.getText(), 1.0);
        g.count = (int) parse(count.getText(), 5);
        g.length = parse(length.getText(), 2.4);
        g.nodes = (int) parse(nodes.getText(), 12);
        g.area = parse(area.getText(), 1.8e-4);
        g.pretension = parse(preten.getText(), 8e4);
        g.cableMat = selectedInt(cableMat, ParameterList.cableMaterial());
        g.groutMat = selectedInt(groutMat, ParameterList.groutMaterial());
        return g;
    }

    private void added(BoltGroup g) {
        UndoManager.push();
        ParameterList.boltGroups.add(g);
        g.rebuild();
        groups.addElement(g);
        groupList.setSelectedIndex(groups.size() - 1);
        Stage4Sync.syncBoltActions();
        if (onGroupsChanged != null) onGroupsChanged.run();
        canvas.repaint();
    }

    private void deleteGroup() {
        int index = groupList.getSelectedIndex();
        if (index < 0) return;
        UndoManager.push();
        ParameterList.boltGroups.remove(index);
        groups.remove(index);
        if (groups.size() > 0) groupList.setSelectedIndex(Math.min(index, groups.size() - 1));
        else clearBoltList();
        updateCanvasSelection();
        Stage4Sync.syncBoltActions();
        if (onGroupsChanged != null) onGroupsChanged.run();
        canvas.repaint();
    }

    private void clearAll() {
        if (ParameterList.boltGroups.isEmpty()) return;
        int choice = JOptionPane.showConfirmDialog(this,
                "清除所有锚杆组？锚杆参数默认值会保留。", "清空锚杆",
                JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) return;
        UndoManager.push();
        ParameterList.boltGroups.clear();
        Stage4Sync.syncBoltActions();
        reload();
        if (onGroupsChanged != null) onGroupsChanged.run();
        canvas.repaint();
    }

    private void applyGroup() {
        BoltGroup g = groupList.getSelectedValue();
        if (g == null) return;
        UndoManager.push();
        g.x1 = parse(x1.getText(), g.x1);
        g.y1 = parse(y1.getText(), g.y1);
        g.x2 = parse(x2.getText(), g.x2);
        g.y2 = parse(y2.getText(), g.y2);
        g.spacing = parse(spacing.getText(), g.spacing);
        g.count = (int) parse(count.getText(), g.count);
        g.length = parse(length.getText(), g.length);
        g.nodes = (int) parse(nodes.getText(), g.nodes);
        g.area = parse(area.getText(), g.area);
        g.pretension = parse(preten.getText(), g.pretension);
        g.cableMat = selectedInt(cableMat, g.cableMat);
        g.groutMat = selectedInt(groutMat, g.groutMat);
        g.rebuild();
        gapLabel.setText(gapText(g));
        refreshBolts(g);
        canvas.repaint();
    }

    private void bindGroup() {
        if (binding) return;
        BoltGroup g = groupList.getSelectedValue();
        if (g == null) {
            clearGroupForm();
            return;
        }
        binding = true;
        x1.setText(fmt(g.x1)); y1.setText(fmt(g.y1));
        x2.setText(fmt(g.x2)); y2.setText(fmt(g.y2));
        spacing.setText(fmt(g.spacing));
        count.setText(String.valueOf(g.count));
        length.setText(fmt(g.length));
        nodes.setText(String.valueOf(g.nodes));
        area.setText(String.format(java.util.Locale.ROOT, "%.2e", g.area));
        preten.setText(String.format(java.util.Locale.ROOT, "%.1e", g.pretension));
        gapLabel.setText(gapText(g));
        cableMat.setSelectedItem(g.cableMat);
        groutMat.setSelectedItem(g.groutMat);
        binding = false;
        refreshBolts(g);
        updateCanvasSelection();
    }

    private void refreshBolts(BoltGroup g) {
        bolts.clear();
        for (Bolt b : g.bolts) bolts.addElement(b);
        gapLabel.setText(gapText(g));
        if (bolts.size() > 0) boltList.setSelectedIndex(0);
        updateCanvasSelection();
    }

    private void bindBolt() {
        Bolt b = boltList.getSelectedValue();
        if (b == null) {
            bAngle.setText(""); bLength.setText(""); bNodes.setText("");
            bArea.setText(""); bPreten.setText("");
        } else {
            bAngle.setText(fmt(b.angleDeg));
            bLength.setText(fmt(b.length));
            bNodes.setText(String.valueOf(b.nodes));
            bArea.setText(String.format(java.util.Locale.ROOT, "%.2e", b.area));
            bPreten.setText(String.format(java.util.Locale.ROOT, "%.1e", b.pretension));
            bCableMat.setSelectedItem(b.cableMat);
            bGroutMat.setSelectedItem(b.groutMat);
        }
    }

    private void applyBolt() {
        Bolt b = boltList.getSelectedValue();
        if (b == null) return;
        UndoManager.push();
        b.angleDeg = parse(bAngle.getText(), b.angleDeg);
        b.length = parse(bLength.getText(), b.length);
        b.nodes = (int) parse(bNodes.getText(), b.nodes);
        b.area = parse(bArea.getText(), b.area);
        b.pretension = parse(bPreten.getText(), b.pretension);
        b.cableMat = selectedInt(bCableMat, b.cableMat);
        b.groutMat = selectedInt(bGroutMat, b.groutMat);
        b.custom = true;
        canvas.repaint();
    }

    private void rotateSelected() {
        BoltGroup g = groupList.getSelectedValue();
        if (g == null) return;
        double deg = parse(rotate.getText(), 0);
        if (Math.abs(deg) < 1e-9) return;
        UndoManager.push();
        for (Bolt b : boltList.getSelectedValuesList()) {
            b.angleDeg += deg;
            b.custom = true;
        }
        bindBolt();
        canvas.repaint();
    }

    private void clearGroupForm() {
        binding = true;
        x1.setText(""); y1.setText(""); x2.setText(""); y2.setText("");
        spacing.setText("1.0"); count.setText("5"); length.setText("2.4"); nodes.setText("12");
        area.setText("1.8e-04"); preten.setText("8.0e+04");
        gapLabel.setText("—");
        refreshMaterialBoxes();
        cableMat.setSelectedItem(ParameterList.cableMaterial());
        groutMat.setSelectedItem(ParameterList.groutMaterial());
        binding = false;
        clearBoltList();
    }

    private void clearBoltList() {
        bolts.clear();
        bindBolt();
        updateCanvasSelection();
    }

    private void updateCanvasSelection() {
        canvas.setBoltSelection(groupList.getSelectedIndex(), boltList.getSelectedIndices());
    }

    private static double parse(String text, double fallback) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static int selectedInt(JComboBox<Integer> box, int fallback) {
        Object value = box.getSelectedItem();
        return value instanceof Integer ? (Integer) value : fallback;
    }

    private String fmt(double v) {
        if (v == Math.rint(v)) return String.format("%.0f", v);
        return String.format("%.2f", v);
    }

    private String gapText(BoltGroup g) {
        double len = Math.hypot(g.x2 - g.x1, g.y2 - g.y1);
        double span = (g.count - 1) * g.spacing;
        if (span > len + 1e-9) {
            return String.format("间排距过大，需要 %.2f m", span);
        }
        return String.format("两侧各 %.3f m", g.endOffset);
    }

    private GridBagConstraints grid() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(1, 3, 1, 3);
        return c;
    }

    private void row(JPanel panel, GridBagConstraints c, int row, String l1,
                     JComponent f1, String l2, JComponent f2) {
        c.gridy = row; c.gridx = 0;
        panel.add(new JLabel(l1), c);
        c.gridx = 1; f1.setPreferredSize(new Dimension(90, 22)); panel.add(f1, c);
        c.gridx = 2; panel.add(new JLabel(l2), c);
        c.gridx = 3; f2.setPreferredSize(new Dimension(90, 22)); panel.add(f2, c);
    }

    private void row(JPanel panel, GridBagConstraints c, int row, String l1,
                     JComponent f1, JComponent f2) {
        c.gridy = row; c.gridx = 0;
        panel.add(new JLabel(l1), c);
        c.gridx = 1; f1.setPreferredSize(new Dimension(90, 22)); panel.add(f1, c);
        c.gridx = 2; f2.setPreferredSize(new Dimension(90, 22)); panel.add(f2, c);
        c.gridx = 3;
    }

    private void singleRow(JPanel panel, GridBagConstraints c, int row,
                           String label, JComponent field) {
        c.gridy = row; c.gridx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1; c.gridwidth = 3;
        field.setPreferredSize(new Dimension(90, 22));
        panel.add(field, c);
        c.gridwidth = 1;
    }
}
