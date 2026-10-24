import javax.swing.*;
import java.awt.*;

public class JointMaterialPanel extends JPanel {
    private final JTextField jkn = new JTextField();
    private final JTextField jks = new JTextField();
    private final JTextField jfric = new JTextField();
    private final JTextField jcoh = new JTextField();
    private final JTextField jten = new JTextField();
    private int index = -1;
    private LayerCanvas canvas;

    public JointMaterialPanel() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        GridBagConstraints c = grid();
        addRow(c, 0, "jkn (Pa/m)", jkn, "jkn");
        addRow(c, 1, "jks (Pa/m)", jks, "jks");
        addRow(c, 2, "jfric (°)", jfric, "jfric");
        addRow(c, 3, "jcoh (Pa)", jcoh, "jcoh");
        addRow(c, 4, "jten (Pa)", jten, "jten");
        c.gridy = 5; c.gridx = 0; c.gridwidth = 2; c.weighty = 1;
        add(new JPanel(), c);
    }

    public void setCanvas(LayerCanvas canvas) { this.canvas = canvas; }

    public void bindData(int layerIndex) {
        index = layerIndex;
        if (index < 0 || index >= ParameterList.layerCount()) return;
        LayerData layer = ParameterList.layers.get(index);
        jkn.setText(sci(layer.jkn));
        jks.setText(sci(layer.jks));
        jfric.setText(fmt(layer.jfric));
        jcoh.setText(sci(layer.jcoh));
        jten.setText(sci(layer.jten));
    }

    private void addRow(GridBagConstraints c, int row, String label, JTextField field, String type) {
        c.gridy = row; c.gridx = 0;
        add(new JLabel(label), c);
        c.gridx = 1;
        field.setPreferredSize(new Dimension(120, 22));
        field.setToolTipText("支持科学计数法，如 1e9、2.5e10");
        add(field, c);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { save(field, type); }
        });
        field.addActionListener(e -> save(field, type));
    }

    private void save(JTextField field, String type) {
        if (index < 0 || index >= ParameterList.layerCount()) return;
        try {
            double value = Double.parseDouble(field.getText().trim());
            LayerData layer = ParameterList.layers.get(index);
            UndoManager.push();
            switch (type) {
                case "jkn": layer.jkn = value; break;
                case "jks": layer.jks = value; break;
                case "jfric": layer.jfric = value; break;
                case "jcoh": layer.jcoh = value; break;
                case "jten": layer.jten = value; break;
            }
            ParameterList.syncFromLayers();
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

    private String sci(double value) {
        if (Math.abs(value) < 1e-12) return "0";
        String s = String.format("%.3e", value);
        String[] parts = s.split("e");
        String mantissa = parts[0].replaceAll("0+$", "").replaceAll("\\.$", "");
        return mantissa + "e" + Integer.parseInt(parts[1]);
    }

    private String fmt(double value) {
        if (value == Math.rint(value)) return String.format("%.0f", value);
        return String.format("%.4f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
