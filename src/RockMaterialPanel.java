import javax.swing.*;
import java.awt.*;

public class RockMaterialPanel extends JPanel {
    private final JTextField e = new JTextField();
    private final JTextField poisson = new JTextField();
    private final JTextField density = new JTextField();
    private final JTextField friction = new JTextField();
    private final JTextField cohesion = new JTextField();
    private final JTextField tension = new JTextField();
    private final JTextField mesh = new JTextField();
    private int index = -1;
    private LayerCanvas canvas;
    private Runnable onMeshChanged;

    public RockMaterialPanel() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        GridBagConstraints c = grid();
        addRow(c, 0, "弹性模量 E(GPa)", e, "E");
        addRow(c, 1, "泊松比", poisson, "poisson");
        addRow(c, 2, "密度(g/cm3)", density, "density");
        addRow(c, 3, "摩擦角(°)", friction, "friction");
        addRow(c, 4, "内聚力(MPa)", cohesion, "cohesion");
        addRow(c, 5, "抗拉强度(MPa)", tension, "tension");
        addRow(c, 6, "网格尺寸(m)", mesh, "mesh");
        c.gridy = 7; c.gridx = 0; c.gridwidth = 2; c.weighty = 1;
        add(new JPanel(), c);
    }

    public void setCanvas(LayerCanvas canvas) { this.canvas = canvas; }
    public void setOnMeshChanged(Runnable listener) { this.onMeshChanged = listener; }

    public void bindData(int layerIndex) {
        index = layerIndex;
        if (index < 0 || index >= ParameterList.layerCount()) return;
        LayerData layer = ParameterList.layers.get(index);
        e.setText(fmt(layer.E));
        poisson.setText(fmt(layer.poisson));
        density.setText(fmt(layer.density));
        friction.setText(fmt(layer.friction));
        cohesion.setText(fmt(layer.cohesion));
        tension.setText(fmt(layer.tension));
        mesh.setText(fmt(layer.meshSize));
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
        try {
            double value = Double.parseDouble(text);
            LayerData layer = ParameterList.layers.get(index);
            UndoManager.push();
            switch (type) {
                case "E": layer.E = value; break;
                case "poisson": layer.poisson = value; break;
                case "density": layer.density = value; break;
                case "friction": layer.friction = value; break;
                case "cohesion": layer.cohesion = value; break;
                case "tension": layer.tension = value; break;
                case "mesh": {
                    double old = layer.meshSize;
                    layer.meshSize = value;
                    for (JointSet joint : layer.joints) {
                        if (Math.abs(joint.spacing - old) < 1e-9) joint.spacing = value;
                    }
                    break;
                }
            }
            ParameterList.syncFromLayers();
            if ("mesh".equals(type) && onMeshChanged != null) onMeshChanged.run();
            if (canvas != null) canvas.repaint();
        } catch (NumberFormatException ignored) {
        }
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
