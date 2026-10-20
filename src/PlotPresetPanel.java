import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class PlotPresetPanel extends JPanel {
    public PlotPresetPanel() {
        super(new BorderLayout());

        JPanel plots = new JPanel(new GridBagLayout());
        plots.setBorder(BorderFactory.createTitledBorder("绘图预设（默认前三种，可继续勾选叠加）"));
        GridBagConstraints pc = grid();
        for (int i = 0; i < ParameterList.plotPresets.size(); i++) {
            PlotPreset preset = ParameterList.plotPresets.get(i);
            JCheckBox check = new JCheckBox(preset.label, preset.selected);
            JTextField title = new JTextField(preset.title, 18);
            addPlotRow(plots, pc, i, check, title);
            check.addActionListener(e -> preset.selected = check.isSelected());
            bindString(title, v -> preset.title = v);
        }

        GridBagConstraints filler = grid();
        filler.gridx = 0;
        filler.gridy = ParameterList.plotPresets.size();
        filler.gridwidth = 4;
        filler.weightx = 1.0;
        filler.weighty = 1.0;
        filler.fill = GridBagConstraints.BOTH;
        plots.add(Box.createGlue(), filler);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(plots, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(wrap);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }

    private GridBagConstraints grid() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 4, 2, 4);
        return c;
    }

    private void addPlotRow(JPanel panel, GridBagConstraints c, int row, JCheckBox check, JTextField title) {
        c.gridy = row;
        c.gridx = 0;
        panel.add(check, c);
        c.gridx = 1;
        panel.add(new JLabel("图头"), c);
        c.gridx = 2;
        title.setPreferredSize(new Dimension(220, 22));
        panel.add(title, c);
        c.gridx = 3;
        c.weightx = 1.0;
        panel.add(new JLabel(""), c);
        c.weightx = 0;
    }

    private void bindString(JTextField field, Consumer<String> setter) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { save(field, setter); }
        });
        field.addActionListener(e -> save(field, setter));
    }

    private void save(JTextField field, Consumer<String> setter) {
        String text = field.getText().trim();
        if (!text.isEmpty()) setter.accept(text);
    }
}
