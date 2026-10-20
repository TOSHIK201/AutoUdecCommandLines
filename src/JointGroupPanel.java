import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public class JointGroupPanel extends JPanel {
    public JointGroupPanel(int index, JointSet js, LayerCanvas canvas, Runnable onDelete,
                           Consumer<JointSet> onLinkToggled, Consumer<JointSet> onSpacingChanged) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("节理组 " + (index + 1)));

        JPanel grid = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(1, 3, 1, 3);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;

        JCheckBox link = new JCheckBox("间距联动");
        link.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        link.setSelected(js.baseGroup || js.linkSpacing);
        link.setEnabled(!js.baseGroup);
        link.addActionListener(e -> {
            js.linkSpacing = link.isSelected();
            onLinkToggled.accept(js);
        });
        c.gridy = 0;
        c.gridx = 0;
        c.gridheight = 3;
        c.anchor = GridBagConstraints.WEST;
        grid.add(link, c);
        c.gridheight = 1;

        addCell(grid, c, 0, 1, "角度(°)", js.angle, v -> js.angle = v, canvas, !js.baseGroup);
        addCell(grid, c, 0, 3, "角度标准差", js.angleStdDev, v -> js.angleStdDev = v, canvas, !js.baseGroup);
        addCell(grid, c, 0, 5, "岩桥长度", js.bridgeLength, v -> js.bridgeLength = v, canvas, !js.baseGroup);
        addCell(grid, c, 0, 7, "岩桥标准差", js.bridgeStdDev, v -> js.bridgeStdDev = v, canvas, !js.baseGroup);
        addCell(grid, c, 1, 1, "迹线长度", js.traceLength, v -> js.traceLength = v, canvas, !js.baseGroup);
        addCell(grid, c, 1, 3, "迹线标准差", js.traceStdDev, v -> js.traceStdDev = v, canvas, !js.baseGroup);
        addCell(grid, c, 1, 5, "间距", js.spacing, v -> {
            js.spacing = v;
            onSpacingChanged.accept(js);
        }, canvas, true);
        addCell(grid, c, 1, 7, "间距标准差", js.spacingStdDev, v -> js.spacingStdDev = v, canvas, true);
        addCell(grid, c, 2, 1, "开始X", js.startX, v -> js.startX = v, canvas, !js.baseGroup);
        addCell(grid, c, 2, 3, "开始Y", js.startY, v -> js.startY = v, canvas, !js.baseGroup);

        add(grid, BorderLayout.CENTER);

        JButton del = new JButton("删除该组");
        del.setToolTipText("删除该组节理");
        del.setForeground(Color.RED);
        del.addActionListener(e -> onDelete.run());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        bottom.add(del);
        add(bottom, BorderLayout.SOUTH);
    }

    private void addCell(JPanel grid, GridBagConstraints c, int row, int col, String label,
                         double value, DoubleConsumer setter, LayerCanvas canvas, boolean editable) {
        c.gridy = row;
        c.gridx = col;
        JLabel l = new JLabel(label);
        l.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        grid.add(l, c);

        JTextField field = new JTextField(format(value), 5);
        field.setPreferredSize(new Dimension(48, 21));
        field.setMinimumSize(new Dimension(48, 21));
        field.setEnabled(editable);
        c.gridx = col + 1;
        grid.add(field, c);

        Runnable save = () -> {
            try {
                String text = field.getText().trim();
                if (text.isEmpty()) return;
                setter.accept(Double.parseDouble(text));
                if (canvas != null) canvas.repaint();
            } catch (NumberFormatException ignored) {
            }
        };
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { save.run(); }
        });
        field.addActionListener(e -> save.run());
    }

    private String format(double v) {
        if (v == Math.rint(v)) return String.format("%.0f", v);
        return String.format("%.3f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
