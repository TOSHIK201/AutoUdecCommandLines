import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public class ExcavationFormPanel extends JPanel {
    public static final String[] PRESETS = {"开切眼 + 工作面推进"};

    private final LayerCanvas canvas;
    private final JList<ExcavationLayer> list;
    private final DefaultListModel<ExcavationLayer> model;
    private final JTextField name = new JTextField(10);
    private final JComboBox<String> preset = new JComboBox<>(PRESETS);
    private final JTextField cutWidth = new JTextField(6);
    private final JTextField stepDist = new JTextField(6);
    private final JComboBox<Util.Direction> direction = new JComboBox<>(Util.Direction.values());
    private final JTextField x1 = new JTextField(6);
    private final JTextField y1 = new JTextField(6);
    private final JTextField x2 = new JTextField(6);
    private final JTextField y2 = new JTextField(6);
    private final JTextField steps = new JTextField(6);
    private final JLabel stepsHint = new JLabel();
    private final JButton coal = new JButton("开挖煤层");
    private Runnable coalAction;
    private boolean binding;

    public ExcavationFormPanel(LayerCanvas canvas, JList<ExcavationLayer> list,
                               DefaultListModel<ExcavationLayer> model) {
        super(new GridBagLayout());
        this.canvas = canvas;
        this.list = list;
        this.model = model;
        setBorder(BorderFactory.createTitledBorder("选中开挖区域"));

        GridBagConstraints c = grid();
        put(c, 0, 0, new JLabel("名称"));
        c.gridwidth = 3;
        put(c, 1, 0, name, 150);
        c.gridwidth = 1;
        put(c, 0, 1, new JLabel("开挖预设"));
        c.gridwidth = 3;
        put(c, 1, 1, preset);
        c.gridwidth = 1;
        put(c, 0, 2, new JLabel("开切眼宽度(m)"));
        put(c, 1, 2, cutWidth, 80);
        put(c, 2, 2, new JLabel("开挖步距(m)"));
        put(c, 3, 2, stepDist, 80);
        put(c, 0, 3, new JLabel("开挖方向"));
        c.gridwidth = 3;
        put(c, 1, 3, direction);
        c.gridwidth = 1;
        put(c, 0, 4, new JLabel("开挖步数"));
        put(c, 1, 4, steps, 80);
        c.gridwidth = 2;
        put(c, 2, 4, stepsHint);
        c.gridwidth = 1;
        put(c, 0, 5, new JLabel("X1 / Y1"));
        put(c, 1, 5, x1, 75);
        put(c, 2, 5, y1, 75);
        put(c, 0, 6, new JLabel("X2 / Y2"));
        put(c, 1, 6, x2, 75);
        put(c, 2, 6, y2, 75);
        c.gridy = 7; c.gridx = 0; c.gridwidth = 4;
        c.fill = GridBagConstraints.HORIZONTAL;
        coal.setToolTipText("按当前切眼宽度、步距与步数居中，并开采全厚煤层（只应用一次）");
        coal.addActionListener(e -> {
            if (coalAction != null) coalAction.run();
        });
        add(coal, c);
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;

        bindName();
        preset.addActionListener(e -> applyType());
        direction.addActionListener(e -> applyType());
        bindDouble(cutWidth, v -> edit(item -> item.cutWidth = v, true));
        bindDouble(stepDist, v -> edit(item -> item.stepDist = v, true));
        bindInt(steps, this::setSteps);
        bindDouble(x1, v -> edit(item -> item.x1 = v, false));
        bindDouble(y1, v -> edit(item -> item.y1 = v, false));
        bindDouble(x2, v -> edit(item -> item.x2 = v, false));
        bindDouble(y2, v -> edit(item -> item.y2 = v, false));
    }

    public ExcavationLayer newFromForm() {
        ExcavationLayer e = new ExcavationLayer(nextName(), 0, 0, 0, 0);
        e.preset = String.valueOf(preset.getSelectedItem());
        e.cutWidth = parse(cutWidth.getText(), ParameterList.cutWidth);
        e.stepDist = parse(stepDist.getText(), ParameterList.stepDist);
        e.steps = Math.max(0, (int) parse(steps.getText(), ParameterList.step));
        e.direction = (Util.Direction) direction.getSelectedItem();
        return e;
    }

    public void setCoalAction(Runnable action) {
        coalAction = action;
    }

    public void applyParamsTo(ExcavationLayer e) {
        e.preset = String.valueOf(preset.getSelectedItem());
        e.cutWidth = parse(cutWidth.getText(), e.cutWidth);
        e.stepDist = parse(stepDist.getText(), e.stepDist);
        e.steps = Math.max(0, (int) parse(steps.getText(), e.steps));
        e.direction = (Util.Direction) direction.getSelectedItem();
    }

    private String nextName() {
        int n = ParameterList.excavationLayers.size() + 1;
        while (hasName("EX" + n)) n++;
        return "EX" + n;
    }

    private boolean hasName(String name) {
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            if (name.equals(e.name)) return true;
        }
        return false;
    }

    public void setPreset(String value) {
        if (value != null) preset.setSelectedItem(value);
    }

    private void bindName() {
        name.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                saveName();
            }
        });
        name.addActionListener(e -> saveName());
    }

    private void saveName() {
        if (binding) return;
        ExcavationLayer e = list.getSelectedValue();
        if (e == null) return;
        String text = name.getText().trim().replaceAll("\\s+", "_");
        if (text.isEmpty()) return;
        UndoManager.push();
        e.name = uniqueName(text, e);
        name.setText(e.name);
        refreshItem();
    }

    private String uniqueName(String base, ExcavationLayer self) {
        String result = base;
        int n = 2;
        while (hasName(result, self)) result = base + "_" + n++;
        return result;
    }

    private boolean hasName(String value, ExcavationLayer self) {
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            if (e != self && value.equals(e.name)) return true;
        }
        return false;
    }

    public void bindSelected() {
        ExcavationLayer e = list.getSelectedValue();
        if (e == null) {
            clearForm();
            return;
        }
        binding = true;
        name.setText(e.name);
        preset.setSelectedItem(e.preset);
        cutWidth.setText(fmt(e.cutWidth));
        stepDist.setText(fmt(e.stepDist));
        direction.setSelectedItem(e.direction);
        x1.setText(fmt(Math.min(e.x1, e.x2)));
        y1.setText(fmt(Math.min(e.y1, e.y2)));
        x2.setText(fmt(Math.max(e.x1, e.x2)));
        y2.setText(fmt(Math.max(e.y1, e.y2)));
        steps.setText(String.valueOf(e.steps));
        stepsHint.setText("区域实际 " + e.stepCount() + " 步");
        binding = false;
    }

    public void clearForm() {
        binding = true;
        name.setText("");
        cutWidth.setText(fmt(ParameterList.cutWidth));
        stepDist.setText(fmt(ParameterList.stepDist));
        steps.setText(String.valueOf(ParameterList.step));
        stepsHint.setText("");
        x1.setText("");
        y1.setText("");
        x2.setText("");
        y2.setText("");
        binding = false;
    }

    private void applyType() {
        if (binding) return;
        ExcavationLayer e = list.getSelectedValue();
        if (e == null) return;
        UndoManager.push();
        e.preset = String.valueOf(preset.getSelectedItem());
        e.direction = (Util.Direction) direction.getSelectedItem();
        ParameterList.refreshBoltBindings();
        refreshItem();
    }

    private void setSteps(int value) {
        ExcavationLayer e = list.getSelectedValue();
        if (e == null) return;
        e.steps = Math.max(0, value);
        if (e.autoGeometry) ParameterList.syncExcavationGeometry();
        refreshItem();
    }

    private void edit(Consumer<ExcavationLayer> change, boolean geometry) {
        if (binding) return;
        ExcavationLayer e = list.getSelectedValue();
        if (e == null) return;
        change.accept(e);
        if (!geometry) {
            e.autoGeometry = false;
            double left = Math.min(e.x1, e.x2);
            double right = Math.max(e.x1, e.x2);
            double bottom = Math.min(e.y1, e.y2);
            double top = Math.max(e.y1, e.y2);
            e.x1 = left;
            e.x2 = right;
            e.y1 = bottom;
            e.y2 = top;
        } else if (e.autoGeometry) {
            ParameterList.syncExcavationGeometry();
        }
        ParameterList.refreshBoltBindings();
        refreshItem();
    }

    private void refreshItem() {
        int index = list.getSelectedIndex();
        if (index < 0) return;
        ExcavationLayer e = model.get(index);
        steps.setText(String.valueOf(e.steps));
        stepsHint.setText("区域实际 " + e.stepCount() + " 步");
        model.set(index, e);
        canvas.setSelectedExcavation(index);
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

    private void bindInt(JTextField field, java.util.function.IntConsumer setter) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                saveInt(field, setter);
            }
        });
        field.addActionListener(e -> saveInt(field, setter));
    }

    private void saveInt(JTextField field, java.util.function.IntConsumer setter) {
        if (binding) return;
        String text = field.getText().trim();
        if (text.isEmpty()) return;
        try {
            int value = Integer.parseInt(text);
            UndoManager.push();
            setter.accept(value);
        } catch (NumberFormatException ignored) {
        }
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

    private String fmt(double v) {
        if (v == Math.rint(v)) return String.format("%.0f", v);
        return String.format("%.2f", v);
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
