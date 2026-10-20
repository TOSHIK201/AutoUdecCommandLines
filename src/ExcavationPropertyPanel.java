import javax.swing.*;
import java.awt.*;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

public class ExcavationPropertyPanel extends JPanel {
    private final LayerCanvas canvas;
    private final JTextField fCut = new JTextField(6);
    private final JTextField fHeight = new JTextField(6);
    private final JTextField fSteps = new JTextField(5);
    private final JTextField fDist = new JTextField(6);
    private final JTextField fX = new JTextField(6);
    private final JTextField fY = new JTextField(6);
    private final JComboBox<String> fPreset = new JComboBox<>(new String[]{"开切眼 + 工作面推进"});
    private final JComboBox<Util.Direction> fDir = new JComboBox<>(Util.Direction.values());
    private final JCheckBox fCentered = new JCheckBox("整个开挖区域居中");
    private final JCheckBox fCoal = new JCheckBox("设为开挖煤层");
    private final JLabel status = new JLabel();
    private ExcavationLayer current;

    public ExcavationPropertyPanel(LayerCanvas canvas) {
        this.canvas = canvas;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        status.setForeground(new Color(0, 102, 204));
        status.setBorder(BorderFactory.createEmptyBorder(2, 4, 6, 4));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        fCoal.setFont(new Font("微软雅黑", Font.BOLD, 12));
        fCoal.setToolTipText("勾选后当前层作为开挖煤层，颜色变黑");
        top.add(fCoal);
        top.add(status);
        add(top, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 4, 3, 4);

        addRow(form, c, 0, "开挖预设", fPreset);
        addRow(form, c, 1, "开切眼宽度(m)", fCut);
        addRow(form, c, 2, "整体高度(m)", fHeight);
        addRow(form, c, 3, "开挖步数", fSteps);
        addRow(form, c, 4, "开挖步距(m)", fDist);
        addRow(form, c, 5, "开挖方向", fDir);
        addRow(form, c, 6, "", fCentered);
        addRow(form, c, 7, "X位置(m)", fX);
        addRow(form, c, 8, "Y位置(m)", fY);
        add(form, BorderLayout.CENTER);

        JLabel hint = new JLabel("<html>每个开挖层独立设置。<br>"
                + "整体高度填 0 表示按该层厚度；居中时 X 自动、Y 取层底界；<br>"
                + "不居中时 X/Y 可留空：X 按方向贴左/右边界，Y 留空取层底界。</html>");
        hint.setBorder(BorderFactory.createEmptyBorder(6, 4, 4, 4));
        add(hint, BorderLayout.SOUTH);

        fPreset.addActionListener(e -> {
            if (current != null) current.preset = String.valueOf(fPreset.getSelectedItem());
        });
        bindDouble(fCut, v -> { if (current != null) current.cutWidth = v; });
        bindDouble(fHeight, v -> { if (current != null) current.overallHeight = v; });
        bindInt(fSteps, v -> { if (current != null) current.steps = v; });
        bindDouble(fDist, v -> { if (current != null) current.stepDist = v; });
        fDir.addActionListener(e -> {
            if (current != null) current.direction = (Util.Direction) fDir.getSelectedItem();
            repaintCanvas();
        });
        fCoal.addActionListener(e -> toggleCoal());
        fCentered.addActionListener(e -> updateCenteredMode());
        bindOptionalDouble(fX, v -> { if (current != null) current.originX = v; });
        bindOptionalDouble(fY, v -> { if (current != null) current.originY = v; });

        canvas.addSelectionListener(this::bindToLayer);
        bindToLayer(canvas.getSelectedIndex());
    }

    public void bindToLayer(int index) {
        current = find(index);
        fCoal.setSelected(current != null);
        if (current == null) {
            status.setText("当前层未设为开挖煤层：勾选上方“设为开挖煤层”后可编辑");
            setFormEnabled(false);
            return;
        }
        status.setText("第 " + (index + 1) + " 层 · 编号 " + current.name);
        setFormEnabled(true);
        fPreset.setSelectedItem(current.preset);
        fCut.setText(fmt(current.cutWidth));
        fHeight.setText(fmt(current.overallHeight));
        fSteps.setText(String.valueOf(current.steps));
        fDist.setText(fmt(current.stepDist));
        fDir.setSelectedItem(current.direction);
        fCentered.setSelected(current.centered);
        fX.setText(fmt(current.originX));
        fY.setText(fmt(current.originY));
        updateCenteredMode();
    }

    private String excavationName(LayerData layer, int index) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)").matcher(layer.name);
        if (m.find()) return m.group(1);
        return String.valueOf(index + 1);
    }

    private void toggleCoal() {
        int index = canvas.getSelectedIndex();
        if (index < 0 || index >= ParameterList.layerCount()) return;
        LayerData layer = ParameterList.layers.get(index);
        boolean mark = fCoal.isSelected();
        ExcavationLayer existing = find(index);
        if (mark && existing == null) {
            if (layer.savedColor == null) layer.savedColor = layer.color;
            layer.color = Color.BLACK;
            ExcavationLayer e = new ExcavationLayer(index, excavationName(layer, index));
            e.preset = ParameterList.excavationPreset;
            e.cutWidth = ParameterList.cutWidth;
            e.overallHeight = ParameterList.excavationHeight;
            e.steps = ParameterList.step;
            e.stepDist = ParameterList.stepDist;
            e.direction = ParameterList.direction;
            e.centered = ParameterList.excavationCentered;
            e.originX = ParameterList.excavationOriginX;
            e.originY = ParameterList.excavationOriginY;
            ParameterList.excavationLayers.add(e);
            current = e;
        } else if (!mark && existing != null) {
            ParameterList.excavationLayers.remove(existing);
            if (layer.savedColor != null) {
                layer.color = layer.savedColor;
                layer.savedColor = null;
            }
            current = null;
        }
        ParameterList.syncFromLayers();
        bindToLayer(index);
        repaintCanvas();
    }

    private ExcavationLayer find(int index) {
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            if (e.layerIndex == index) return e;
        }
        return null;
    }

    private void setFormEnabled(boolean enabled) {
        fPreset.setEnabled(enabled);
        fCut.setEnabled(enabled);
        fHeight.setEnabled(enabled);
        fSteps.setEnabled(enabled);
        fDist.setEnabled(enabled);
        fDir.setEnabled(enabled);
        fCentered.setEnabled(enabled);
        fX.setEnabled(enabled);
        fY.setEnabled(enabled);
    }

    private void updateCenteredMode() {
        if (current == null) return;
        boolean centered = fCentered.isSelected();
        current.centered = centered;
        fX.setEnabled(!centered);
        fY.setEnabled(!centered);
        fX.setText(centered ? "自动" : fmt(current.originX));
        fY.setText(centered ? "自动" : fmt(current.originY));
        repaintCanvas();
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridy = row;
        c.gridx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        if (field instanceof JTextField) {
            field.setPreferredSize(new Dimension(95, 22));
        } else if (field instanceof JComboBox) {
            field.setPreferredSize(new Dimension(170, 24));
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
                setter.accept(Double.parseDouble(text));
                repaintCanvas();
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void bindOptionalDouble(JTextField field, DoubleConsumer setter) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { saveOptional(field, setter); }
        });
        field.addActionListener(e -> saveOptional(field, setter));
    }

    private void saveOptional(JTextField field, DoubleConsumer setter) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            setter.accept(Double.NaN);
            repaintCanvas();
            return;
        }
        try {
            setter.accept(Double.parseDouble(text));
            repaintCanvas();
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
                setter.accept(Integer.parseInt(text));
                repaintCanvas();
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void repaintCanvas() {
        if (canvas != null) canvas.repaint();
    }

    private String fmt(double v) {
        if (Double.isNaN(v)) return "";
        if (v == Math.rint(v)) return String.format("%.0f", v);
        return String.format("%.2f", v);
    }
}
