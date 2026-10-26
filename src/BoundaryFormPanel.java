import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public class BoundaryFormPanel extends JPanel {
    private final LayerCanvas canvas;
    private final JList<BoundaryCondition> list;
    private final DefaultListModel<BoundaryCondition> model;
    private final JComboBox<BoundaryCondition.Type> typeBox =
            new JComboBox<>(BoundaryCondition.uiTypes());
    private final JTextField valueField = new JTextField(6);
    private final JTextField widthField = new JTextField(5);
    private final JTextField sxField = new JTextField(6);
    private final JTextField syField = new JTextField(6);
    private final JTextField exField = new JTextField(6);
    private final JTextField eyField = new JTextField(6);
    private final JTextField sxxField = new JTextField(7);
    private final JTextField sxyField = new JTextField(7);
    private final JTextField syyField = new JTextField(7);
    private final BoundaryGradientPanel gradientPanel = new BoundaryGradientPanel();
    private boolean binding;

    public BoundaryFormPanel(LayerCanvas canvas, JList<BoundaryCondition> list,
                             DefaultListModel<BoundaryCondition> model) {
        super(new GridBagLayout());
        this.canvas = canvas;
        this.list = list;
        this.model = model;
        setBorder(BorderFactory.createTitledBorder("选中边界"));

        GridBagConstraints c = grid();
        valueField.setToolTipText("速度边界填速度值（与模型单位一致，通常 m/s）");
        put(c, 0, 0, new JLabel("类型"));
        c.gridwidth = 3;
        put(c, 1, 0, typeBox);
        c.gridwidth = 1;
        put(c, 0, 1, new JLabel("数值"));
        put(c, 1, 1, valueField, 80);
        put(c, 2, 1, new JLabel("宽度(m)"));
        put(c, 3, 1, widthField, 80);
        put(c, 0, 2, new JLabel("起点 X"));
        put(c, 1, 2, sxField, 80);
        put(c, 2, 2, new JLabel("起点 Y"));
        put(c, 3, 2, syField, 80);
        put(c, 0, 3, new JLabel("终点 X"));
        put(c, 1, 3, exField, 80);
        put(c, 2, 3, new JLabel("终点 Y"));
        put(c, 3, 3, eyField, 80);
        put(c, 0, 4, new JLabel("σxx (Pa)"));
        put(c, 1, 4, sxxField, 80);
        put(c, 2, 4, new JLabel("σxy (Pa)"));
        put(c, 3, 4, sxyField, 80);
        put(c, 0, 5, new JLabel("σyy (Pa)"));
        put(c, 1, 5, syyField, 80);
        c.gridy = 6; c.gridx = 0; c.gridwidth = 4;
        add(gradientPanel, c);
        c.gridwidth = 1;
        c.gridy = 7; c.gridx = 0; c.gridwidth = 4;
        JLabel rangeHint = new JLabel("UDEC 边界按矩形 range 生效；斜线取较长方向条带近似");
        rangeHint.setFont(rangeHint.getFont().deriveFont(11f));
        rangeHint.setForeground(new Color(95, 95, 95));
        add(rangeHint, c);
        c.gridwidth = 1;

        typeBox.addActionListener(e -> applyType());
        bindDouble(valueField, v -> edit(item -> item.value = v));
        bindDouble(widthField, v -> edit(item -> item.width = v));
        bindDouble(sxField, v -> edit(item -> { item.sx = v; item.edge = BoundaryCondition.Edge.NONE; }));
        bindDouble(syField, v -> edit(item -> { item.sy = v; item.edge = BoundaryCondition.Edge.NONE; }));
        bindDouble(exField, v -> edit(item -> { item.ex = v; item.edge = BoundaryCondition.Edge.NONE; }));
        bindDouble(eyField, v -> edit(item -> { item.ey = v; item.edge = BoundaryCondition.Edge.NONE; }));
        bindDouble(sxxField, v -> edit(item -> item.sxx = v));
        bindDouble(sxyField, v -> edit(item -> item.sxy = v));
        bindDouble(syyField, v -> edit(item -> item.syy = v));
        gradientPanel.setOnChanged(this::refreshItem);
        updateEnabled(BoundaryCondition.Type.VELOCITY_X);
    }

    public BoundaryCondition newFromForm() {
        BoundaryCondition b = new BoundaryCondition();
        b.type = (BoundaryCondition.Type) typeBox.getSelectedItem();
        b.value = parse(valueField.getText(), 0);
        b.width = Math.max(0.01, parse(widthField.getText(), ParameterList.boundaryWidth));
        b.sxx = parse(sxxField.getText(), 0);
        b.sxy = parse(sxyField.getText(), 0);
        b.syy = parse(syyField.getText(), 0);
        gradientPanel.applyTo(b);
        return b;
    }

    public void bindSelected() {
        BoundaryCondition b = list.getSelectedValue();
        if (b == null) {
            clearForm();
            return;
        }
        binding = true;
        typeBox.setSelectedItem(b.type);
        valueField.setText(BoundaryCondition.fmt(b.value));
        widthField.setText(BoundaryCondition.fmt(b.width));
        sxField.setText(BoundaryCondition.fmt(b.sx));
        syField.setText(BoundaryCondition.fmt(b.sy));
        exField.setText(BoundaryCondition.fmt(b.ex));
        eyField.setText(BoundaryCondition.fmt(b.ey));
        sxxField.setText(String.format(Locale.ROOT, "%.4e", b.sxx));
        sxyField.setText(String.format(Locale.ROOT, "%.4e", b.sxy));
        syyField.setText(String.format(Locale.ROOT, "%.4e", b.syy));
        gradientPanel.bind(b);
        binding = false;
        updateEnabled(b.type);
    }

    public void clearForm() {
        binding = true;
        valueField.setText("");
        widthField.setText(BoundaryCondition.fmt(ParameterList.boundaryWidth));
        sxField.setText("");
        syField.setText("");
        exField.setText("");
        eyField.setText("");
        sxxField.setText("0.0000e+00");
        sxyField.setText("0.0000e+00");
        syyField.setText("0.0000e+00");
        gradientPanel.clear();
        binding = false;
        updateEnabled(BoundaryCondition.Type.VELOCITY_X);
    }

    private void applyType() {
        if (binding) return;
        BoundaryCondition b = list.getSelectedValue();
        if (b == null) return;
        UndoManager.push();
        b.type = (BoundaryCondition.Type) typeBox.getSelectedItem();
        updateEnabled(b.type);
        refreshItem();
    }

    private void updateEnabled(BoundaryCondition.Type type) {
        valueField.setEnabled(!type.displacement() && !type.stress());
        sxxField.setEnabled(type.stress());
        sxyField.setEnabled(type.stress());
        syyField.setEnabled(type.stress());
        gradientPanel.setStress(type.stress());
    }

    private void edit(Consumer<BoundaryCondition> change) {
        if (binding) return;
        BoundaryCondition b = list.getSelectedValue();
        if (b == null) return;
        change.accept(b);
        refreshItem();
    }

    private void refreshItem() {
        int index = list.getSelectedIndex();
        if (index < 0) return;
        model.set(index, model.get(index));
        canvas.setSelectedBoundary(index);
        canvas.repaint();
    }

    private static double parse(String text, double fallback) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private void bindDouble(JTextField field, DoubleConsumer setter) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                saveDouble(field, setter);
            }
        });
        field.addActionListener(e -> saveDouble(field, setter));
    }

    private void saveDouble(JTextField field, DoubleConsumer setter) {
        if (binding) return;
        String text = field.getText().trim();
        if (text.isEmpty()) return;
        try {
            double value = Double.parseDouble(text);
            UndoManager.push();
            setter.accept(value);
        } catch (NumberFormatException ignored) {
        }
    }

    private GridBagConstraints grid() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 4, 2, 4);
        return c;
    }

    private void put(GridBagConstraints c, int x, int y, JComponent field) {
        put(c, x, y, field, 0);
    }

    private void put(GridBagConstraints c, int x, int y, JComponent field, int width) {
        c.gridx = x;
        c.gridy = y;
        if (width > 0 && field instanceof JTextField) {
            field.setPreferredSize(new Dimension(width, 22));
        }
        add(field, c);
    }
}
