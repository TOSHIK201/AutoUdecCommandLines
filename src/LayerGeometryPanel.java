import javax.swing.*;
import java.awt.*;

public class LayerGeometryPanel extends JPanel {
    private final JTextField name = new JTextField();
    private final JTextField thickness = new JTextField();
    private final JButton colorButton = new JButton("选择");
    private int index = -1;
    private LayerCanvas canvas;
    private Runnable onGeometryChanged;

    public LayerGeometryPanel() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        GridBagConstraints c = grid();
        addRow(c, 0, "岩层名称", name, "name");
        addRow(c, 1, "本层厚度(m)", thickness, "thickness");
        c.gridy = 2; c.gridx = 0;
        add(new JLabel("显示颜色"), c);
        c.gridx = 1;
        colorButton.setToolTipText("选择岩层显示颜色");
        colorButton.setPreferredSize(new Dimension(90, 22));
        colorButton.addActionListener(e -> chooseColor());
        add(colorButton, c);
        c.gridy = 3; c.gridx = 0; c.gridwidth = 2; c.weighty = 1;
        add(new JPanel(), c);
    }

    public void setCanvas(LayerCanvas canvas) { this.canvas = canvas; }
    public void setOnGeometryChanged(Runnable listener) { this.onGeometryChanged = listener; }

    public void bindData(int layerIndex) {
        index = layerIndex;
        if (index < 0 || index >= ParameterList.layerCount()) return;
        LayerData layer = ParameterList.layers.get(index);
        name.setText(layer.name);
        thickness.setText(fmt(layer.thickness));
        colorButton.setBackground(layer.color);
        colorButton.setEnabled(!isExcavationLayer(index));
    }

    private void addRow(GridBagConstraints c, int row, String label, JTextField field, String type) {
        c.gridy = row; c.gridx = 0;
        add(new JLabel(label), c);
        c.gridx = 1;
        field.setPreferredSize(new Dimension(110, 22));
        add(field, c);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { save(field, type); }
        });
        field.addActionListener(e -> save(field, type));
    }

    private void save(JTextField field, String type) {
        if (index < 0 || index >= ParameterList.layerCount()) return;
        String text = field.getText().trim();
        if (text.isEmpty()) return;
        LayerData layer = ParameterList.layers.get(index);
        try {
            UndoManager.push();
            if ("name".equals(type)) {
                layer.name = text;
            } else {
                double value = Double.parseDouble(text);
                if (value <= 0.01) return;
                layer.thickness = value;
            }
            ParameterList.syncFromLayers();
            if (onGeometryChanged != null) onGeometryChanged.run();
            if (canvas != null) canvas.repaint();
        } catch (NumberFormatException ignored) {
        }
    }

    private void chooseColor() {
        if (index < 0 || index >= ParameterList.layerCount() || isExcavationLayer(index)) return;
        LayerData layer = ParameterList.layers.get(index);
        Color color = JColorChooser.showDialog(this, "选择岩层颜色", layer.color);
        if (color == null) return;
        UndoManager.push();
        layer.color = color;
        colorButton.setBackground(color);
        if (canvas != null) canvas.repaint();
    }

    private boolean isExcavationLayer(int layerIndex) {
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            if (e.layerIndex == layerIndex) return true;
        }
        return false;
    }

    private GridBagConstraints grid() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 3, 2, 3);
        return c;
    }

    private String fmt(double value) {
        if (value == Math.rint(value)) return String.format("%.0f", value);
        return String.format("%.4f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
