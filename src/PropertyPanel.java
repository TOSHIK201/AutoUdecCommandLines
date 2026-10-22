import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.DoubleConsumer;

public class PropertyPanel extends JPanel {
    private JPanel rockPanel, jointPanel;
    private JLabel layerLabel;
    private JTextField f_name, f_thickness, f_e, f_poisson, f_density, f_friction, f_cohesion, f_tension, f_mesh;
    private JButton colorBtn;
    private JTextField f_jkn, f_jks, f_jfric, f_jcoh, f_jten;
    private JPanel groupContainer;
    private int currentIndex = -1;
    private LayerCanvas canvas;

    public PropertyPanel() {
        setLayout(new BorderLayout());
        buildRockPanel();
        buildJointPanel();
    }

    public JPanel getRockPanel() { return rockPanel; }
    public JPanel getJointPanel() { return jointPanel; }
    public void setCanvas(LayerCanvas canvas) { this.canvas = canvas; }

    private void buildRockPanel() {
        rockPanel = new JPanel(new GridBagLayout());
        rockPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 3, 2, 3);
        c.weightx = 0;

        f_name = new JTextField();
        f_thickness = new JTextField();
        f_e = new JTextField();
        f_poisson = new JTextField();
        f_density = new JTextField();
        f_friction = new JTextField();
        f_cohesion = new JTextField();
        f_tension = new JTextField();
        f_mesh = new JTextField();

        addRockRow(rockPanel, c, 0, "岩层名称", f_name, "name");
        addRockRow(rockPanel, c, 1, "本层厚度(m)", f_thickness, "thickness");
        addRockRow(rockPanel, c, 2, "弹性模量E(GPa)", f_e, "E");
        addRockRow(rockPanel, c, 3, "泊松比", f_poisson, "poisson");
        addRockRow(rockPanel, c, 4, "密度(g/cm3)", f_density, "density");
        addRockRow(rockPanel, c, 5, "摩擦角(°)", f_friction, "friction");
        addRockRow(rockPanel, c, 6, "内聚力(MPa)", f_cohesion, "cohesion");
        addRockRow(rockPanel, c, 7, "抗拉强度(MPa)", f_tension, "tension");
        addRockRow(rockPanel, c, 8, "网格尺寸(m)", f_mesh, "mesh");

        c.gridy = 9; c.gridx = 0;
        rockPanel.add(new JLabel("显示颜色"), c);
        c.gridx = 1;
        colorBtn = new JButton("选择");
        colorBtn.setToolTipText("选择岩层显示颜色");
        colorBtn.setPreferredSize(new Dimension(70, 22));
        colorBtn.addActionListener(e -> chooseColor());
        rockPanel.add(colorBtn, c);

        c.gridy = 10; c.weighty = 1; c.gridx = 0; c.gridwidth = 2;
        rockPanel.add(new JPanel(), c);
    }

    private void buildJointPanel() {
        jointPanel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        layerLabel = new JLabel("当前层：1");
        layerLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        layerLabel.setForeground(new Color(0, 102, 204));
        top.add(layerLabel);
        jointPanel.add(top, BorderLayout.NORTH);

        groupContainer = new JPanel();
        groupContainer.setLayout(new BoxLayout(groupContainer, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(groupContainer);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        jointPanel.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        JPanel mech = new JPanel(new GridBagLayout());
        mech.setBorder(BorderFactory.createTitledBorder("本层节理力学参数"));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(1, 4, 1, 4);

        f_jkn = new JTextField(); f_jks = new JTextField(); f_jfric = new JTextField();
        f_jcoh = new JTextField(); f_jten = new JTextField();
        f_jkn.setToolTipText("支持科学计数法，如 1e9、2.5e10");
        f_jks.setToolTipText("支持科学计数法，如 2e10");
        f_jcoh.setToolTipText("支持科学计数法，如 1.5e6");
        f_jten.setToolTipText("支持科学计数法，如 1e7");
        addMechRow(mech, c, 0, 0, "jkn", f_jkn, "jkn");
        addMechRow(mech, c, 0, 2, "jks", f_jks, "jks");
        addMechRow(mech, c, 1, 0, "jfric", f_jfric, "jfric");
        addMechRow(mech, c, 1, 2, "jcoh", f_jcoh, "jcoh");
        addMechRow(mech, c, 2, 0, "jten", f_jten, "jten");

        JButton addGroup = new JButton("+ 新建节理组");
        addGroup.setToolTipText("为当前岩层新增一组节理");
        addGroup.addActionListener(e -> addJointGroup());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        btnRow.add(addGroup);

        bottom.add(mech, BorderLayout.CENTER);
        bottom.add(btnRow, BorderLayout.SOUTH);
        jointPanel.add(bottom, BorderLayout.SOUTH);
    }

    private void addRockRow(JPanel panel, GridBagConstraints c, int row, String label, JTextField field, String type) {
        c.gridy = row; c.gridx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        field.setPreferredSize(new Dimension(90, 22));
        panel.add(field, c);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { saveRock(field, type); }
        });
        field.addActionListener(e -> saveRock(field, type));
    }

    private void saveRock(JTextField field, String type) {
        if (currentIndex < 0 || currentIndex >= ParameterList.layerCount()) return;
        LayerData layer = ParameterList.layers.get(currentIndex);
        String text = field.getText().trim();
        if (text.isEmpty()) return;
        try {
            UndoManager.push();
            if ("name".equals(type)) {
                layer.name = text;
            } else {
                double v = Double.parseDouble(text);
                if ("thickness".equals(type) && v <= 0.01) return;
                switch (type) {
                    case "thickness": layer.thickness = v; break;
                    case "E": layer.E = v; break;
                    case "poisson": layer.poisson = v; break;
                    case "density": layer.density = v; break;
                    case "friction": layer.friction = v; break;
                    case "cohesion": layer.cohesion = v; break;
                    case "tension": layer.tension = v; break;
                    case "mesh": {
                        double old = layer.meshSize;
                        layer.meshSize = v;
                        for (JointSet js : layer.joints) {
                            if (Math.abs(js.spacing - old) < 1e-9) js.spacing = v;
                        }
                        break;
                    }
                }
                ParameterList.syncFromLayers();
                if ("thickness".equals(type) || "mesh".equals(type)) refreshGroups();
            }
            if (canvas != null) canvas.repaint();
        } catch (NumberFormatException ignored) {
        }
    }

    private void addMechRow(JPanel panel, GridBagConstraints c, int row, int col, String label,
                            JTextField field, String type) {
        c.gridy = row; c.gridx = col;
        panel.add(new JLabel(label), c);
        c.gridx = col + 1;
        field.setPreferredSize(new Dimension(72, 21));
        panel.add(field, c);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { saveMech(field, type); }
        });
        field.addActionListener(e -> saveMech(field, type));
    }

    private void saveMech(JTextField field, String type) {
        if (currentIndex < 0 || currentIndex >= ParameterList.layerCount()) return;
        try {
            double v = Double.parseDouble(field.getText().trim());
            LayerData layer = ParameterList.layers.get(currentIndex);
            UndoManager.push();
            switch (type) {
                case "jkn": layer.jkn = v; break;
                case "jks": layer.jks = v; break;
                case "jfric": layer.jfric = v; break;
                case "jcoh": layer.jcoh = v; break;
                case "jten": layer.jten = v; break;
            }
            ParameterList.syncFromLayers();
            if (canvas != null) canvas.repaint();
        } catch (NumberFormatException ignored) {
        }
    }

    private void chooseColor() {
        if (currentIndex < 0 || currentIndex >= ParameterList.layerCount()) return;
        if (isExcavationLayer(currentIndex)) return;
        LayerData layer = ParameterList.layers.get(currentIndex);
        Color color = JColorChooser.showDialog(this, "选择岩层颜色", layer.color);
        if (color != null) {
            UndoManager.push();
            layer.color = color;
            colorBtn.setBackground(color);
            if (canvas != null) canvas.repaint();
        }
    }

    private void addJointGroup() {
        if (currentIndex < 0 || currentIndex >= ParameterList.layerCount()) return;
        LayerData layer = ParameterList.layers.get(currentIndex);
        List<Double> hs = ParameterList.heights();
        double bottom = hs.get(currentIndex + 1);
        UndoManager.push();
        layer.joints.add(new JointSet(ParameterList.jointAngle, ParameterList.jointStartX, bottom));
        refreshGroups();
        if (canvas != null) canvas.repaint();
    }

    private void refreshGroups() {
        UndoManager.suspend(this::refreshGroupsInternal);
    }

    private void refreshGroupsInternal() {
        groupContainer.removeAll();
        if (currentIndex < 0 || currentIndex >= ParameterList.layerCount()) {
            groupContainer.revalidate();
            groupContainer.repaint();
            return;
        }
        List<JointSet> joints = ParameterList.layers.get(currentIndex).joints;
        for (int i = 0; i < joints.size(); i++) {
            final JointSet target = joints.get(i);
            JointGroupPanel panel = new JointGroupPanel(i, target, canvas, () -> {
                if (joints.size() <= 1) {
                    JOptionPane.showMessageDialog(this, "至少保留一组节理");
                    return;
                }
                UndoManager.push();
                joints.remove(target);
                refreshGroups();
                if (canvas != null) canvas.repaint();
            }, this::onLinkToggled, this::onSpacingChanged);
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            int prefH = Math.max(150, panel.getPreferredSize().height);
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, prefH));
            groupContainer.add(panel);
            groupContainer.add(Box.createVerticalStrut(6));
        }
        groupContainer.revalidate();
        groupContainer.repaint();
    }

    private boolean isExcavationLayer(int index) {
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            if (e.layerIndex == index) return true;
        }
        return false;
    }

    private void onLinkToggled(JointSet source) {
        if (currentIndex < 0 || currentIndex >= ParameterList.layerCount()) return;
        if (source.linkSpacing) {
            for (JointSet other : ParameterList.layers.get(currentIndex).joints) {
                if (other != source && other.linkSpacing) {
                    source.spacing = other.spacing;
                    break;
                }
            }
        }
        refreshGroups();
        if (canvas != null) canvas.repaint();
    }

    private void onSpacingChanged(JointSet source) {
        if (currentIndex < 0 || currentIndex >= ParameterList.layerCount()) return;
        if (source.linkSpacing) {
            for (JointSet other : ParameterList.layers.get(currentIndex).joints) {
                if (other != source && other.linkSpacing) other.spacing = source.spacing;
            }
            refreshGroups();
        }
        if (canvas != null) canvas.repaint();
    }

    public void bindData(int index) {
        UndoManager.suspend(() -> bindDataInternal(index));
    }

    private void bindDataInternal(int index) {
        currentIndex = index;
        if (index < 0 || index >= ParameterList.layerCount()) return;
        LayerData layer = ParameterList.layers.get(index);
        layerLabel.setText("当前层：" + (index + 1) + "  " + layer.name);

        f_name.setText(layer.name);
        f_thickness.setText(fmt(layer.thickness));
        f_e.setText(fmt(layer.E));
        f_poisson.setText(fmt(layer.poisson));
        f_density.setText(fmt(layer.density));
        f_friction.setText(fmt(layer.friction));
        f_cohesion.setText(fmt(layer.cohesion));
        f_tension.setText(fmt(layer.tension));
        f_mesh.setText(fmt(layer.meshSize));

        f_jkn.setText(sci(layer.jkn));
        f_jks.setText(sci(layer.jks));
        f_jfric.setText(fmt(layer.jfric));
        f_jcoh.setText(sci(layer.jcoh));
        f_jten.setText(sci(layer.jten));
        colorBtn.setBackground(layer.color);
        boolean marked = isExcavationLayer(index);
        colorBtn.setEnabled(!marked);
        refreshGroups();
    }

    private String sci(double v) {
        if (Math.abs(v) < 1e-12) return "0";
        String s = String.format("%.3e", v);
        String[] parts = s.split("e");
        String mant = parts[0].replaceAll("0+$", "").replaceAll("\\.$", "");
        int exp = Integer.parseInt(parts[1]);
        return mant + "e" + exp;
    }

    private String fmt(double v) {
        if (v == Math.rint(v)) return String.format("%.0f", v);
        return String.format("%.4f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
