import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StagePlotPanel extends JPanel {
    private final List<PlotPreset> presets;
    private final JComboBox<PlotPreset> combo = new JComboBox<>();
    private final JPanel rows = new JPanel();
    private final JLabel count = new JLabel();
    private final Runnable onChange;
    private final boolean allowHist;

    public StagePlotPanel(String title, List<PlotPreset> presets, boolean allowHist,
                          Runnable onChange) {
        super(new BorderLayout());
        this.presets = presets;
        this.onChange = onChange;
        this.allowHist = allowHist;
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean selected, boolean focused) {
                super.getListCellRendererComponent(list, value, index, selected, focused);
                if (value instanceof PlotPreset) {
                    PlotPreset preset = (PlotPreset) value;
                    boolean blocked = !allowHist && preset.histCommand != null;
                    setText((preset.selected ? "✓ " : "   ") + preset.label
                            + (blocked ? "（仅阶段3）" : ""));
                    setEnabled(!blocked);
                }
                return this;
            }
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        top.add(new JLabel(title));
        JButton add = new JButton("添加");
        add.setToolTipText("把下拉菜单中选中的绘图项加入本阶段");
        add.addActionListener(e -> addSelected());
        top.add(combo);
        top.add(add);
        top.add(count);
        if (allowHist) top.add(new JLabel("控制台显示限 1 项"));
        add(top, BorderLayout.NORTH);

        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(rows);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setPreferredSize(new Dimension(420, 92));
        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    private void addSelected() {
        PlotPreset preset = (PlotPreset) combo.getSelectedItem();
        if (preset == null) return;
        if (preset.selected) return;
        if (!allowHist && preset.histCommand != null) {
            JOptionPane.showMessageDialog(this, "该绘图项需要 solve 后的 history 记录，只能在阶段3使用");
            return;
        }
        UndoManager.push();
        preset.order = nextOrder();
        preset.selected = true;
        refresh();
    }

    private void remove(PlotPreset preset) {
        UndoManager.push();
        preset.selected = false;
        preset.order = 0;
        refresh();
    }

    private int nextOrder() {
        int max = 0;
        for (PlotPreset preset : presets) {
            if (preset.selected) max = Math.max(max, preset.order);
        }
        return max + 1;
    }

    private void refresh() {
        if (onChange != null) onChange.run();
        combo.removeAllItems();
        int selected = 0;
        for (PlotPreset preset : presets) {
            if (preset.selected) selected++;
            combo.addItem(preset);
        }
        count.setText("已选 " + selected + " 项");
        combo.setEnabled(combo.getItemCount() > 0);

        rows.removeAll();
        for (PlotPreset preset : presets) {
            if (preset.selected) rows.add(buildRow(preset));
        }
        rows.revalidate();
        rows.repaint();
    }

    private JPanel buildRow(PlotPreset preset) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 1));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        JLabel label = new JLabel(preset.label);
        label.setPreferredSize(new Dimension(100, 20));
        row.add(label);
        row.add(new JLabel("图头"));
        JTextField title = new JTextField(preset.title, 12);
        title.setPreferredSize(new Dimension(130, 22));
        bindTitle(title, preset);
        row.add(title);
        if (preset.histCommand != null) {
            JCheckBox console = new JCheckBox("控制台", preset.console);
            console.setToolTipText("循环时在控制台实时显示该历史量（一次只能一项）");
            console.addActionListener(e -> {
                UndoManager.push();
                if (console.isSelected()) {
                    for (PlotPreset p : presets) p.console = false;
                }
                preset.console = console.isSelected();
                refresh();
            });
            row.add(console);
        }
        JButton remove = new JButton("移除");
        remove.addActionListener(e -> remove(preset));
        row.add(remove);
        return row;
    }

    private void bindTitle(JTextField field, PlotPreset preset) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                saveTitle(field, preset);
            }
        });
        field.addActionListener(e -> saveTitle(field, preset));
    }

    private void saveTitle(JTextField field, PlotPreset preset) {
        String text = field.getText().trim();
        if (!text.isEmpty() && !text.equals(preset.title)) {
            UndoManager.push();
            preset.title = text;
        }
    }
}
