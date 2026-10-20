import javax.swing.*;
import java.awt.*;

public class ExcavationPanel extends JPanel {
    private final LayerCanvas canvas;
    private final JPanel rows = new JPanel();

    public ExcavationPanel(LayerCanvas canvas) {
        this.canvas = canvas;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("开挖煤层"));
        setPreferredSize(new Dimension(0, 145));

        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(rows);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        add(scroll, BorderLayout.CENTER);

        JButton add = new JButton("+ 新增开挖层");
        add.addActionListener(e -> addLayer());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        bottom.add(add);
        add(bottom, BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        rows.removeAll();
        for (int i = 0; i < ParameterList.excavationLayers.size(); i++) {
            rows.add(buildRow(i));
        }
        rows.revalidate();
        rows.repaint();
    }

    private JPanel buildRow(int index) {
        ExcavationLayer layer = ParameterList.excavationLayers.get(index);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));

        row.add(new JLabel("层号"));
        JTextField idx = new JTextField(String.valueOf(layer.layerIndex + 1), 4);
        idx.setPreferredSize(new Dimension(42, 22));
        row.add(idx);

        row.add(new JLabel("编号"));
        JTextField name = new JTextField(layer.name, 5);
        name.setPreferredSize(new Dimension(55, 22));
        row.add(name);

        JButton del = new JButton("删除");
        del.setForeground(Color.RED);
        del.addActionListener(e -> {
            if (ParameterList.excavationLayers.size() <= 1) {
                JOptionPane.showMessageDialog(this, "至少保留一个开挖层");
                return;
            }
            ParameterList.excavationLayers.remove(layer);
            ParameterList.syncFromLayers();
            refresh();
            if (canvas != null) canvas.repaint();
        });
        row.add(del);

        Runnable saveIndex = () -> {
            try {
                int v = Integer.parseInt(idx.getText().trim());
                v = Math.max(1, Math.min(v, ParameterList.layerCount()));
                layer.layerIndex = v - 1;
                idx.setText(String.valueOf(v));
                ParameterList.syncFromLayers();
                if (canvas != null) canvas.repaint();
            } catch (NumberFormatException ignored) {
            }
        };
        idx.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { saveIndex.run(); }
        });
        idx.addActionListener(e -> saveIndex.run());

        Runnable saveName = () -> {
            String text = name.getText().trim();
            if (!text.isEmpty()) {
                layer.name = text;
                ParameterList.syncFromLayers();
                if (canvas != null) canvas.repaint();
            }
        };
        name.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { saveName.run(); }
        });
        name.addActionListener(e -> saveName.run());

        return row;
    }

    private void addLayer() {
        int idx = canvas == null ? 0 : Math.max(0, canvas.getSelectedIndex());
        ParameterList.excavationLayers.add(new ExcavationLayer(idx, "新层"));
        ParameterList.syncFromLayers();
        refresh();
        if (canvas != null) canvas.repaint();
    }
}
