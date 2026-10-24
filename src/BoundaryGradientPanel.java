import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public class BoundaryGradientPanel extends JPanel {
    private static final String[] PRESETS = {
            "无梯度", "X方向梯度", "Y方向梯度", "双向梯度"
    };
    private final JComboBox<String> preset = new JComboBox<>(PRESETS);
    private final JTextField sxxx = new JTextField(7);
    private final JTextField sxyx = new JTextField(7);
    private final JTextField syyx = new JTextField(7);
    private final JTextField sxxy = new JTextField(7);
    private final JTextField sxyy = new JTextField(7);
    private final JTextField syyy = new JTextField(7);
    private BoundaryCondition current;
    private Runnable onChange;
    private boolean binding;

    public BoundaryGradientPanel() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("应力梯度"));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(1, 3, 1, 3);

        c.gridy = 0; c.gridx = 0;
        add(new JLabel("梯度预设"), c);
        c.gridx = 1; c.gridwidth = 3;
        preset.setPreferredSize(new Dimension(140, 22));
        add(preset, c);
        c.gridwidth = 1;

        c.gridy = 1; c.gridx = 0;
        add(new JLabel("xgrad"), c);
        addField(c, 1, sxxx, "σxx 沿 X");
        addField(c, 2, sxyx, "σxy 沿 X");
        addField(c, 3, syyx, "σyy 沿 X");

        c.gridy = 2; c.gridx = 0;
        add(new JLabel("ygrad"), c);
        addField(c, 1, sxxy, "σxx 沿 Y");
        addField(c, 2, sxyy, "σxy 沿 Y");
        addField(c, 3, syyy, "σyy 沿 Y");

        preset.setToolTipText("按手册：xgrad sxxx sxyx syyx；ygrad sxxy sxyy syyy");
        preset.addActionListener(e -> applyPreset());
        bind(sxxx, "sxxx");
        bind(sxyx, "sxyx");
        bind(syyx, "syyx");
        bind(sxxy, "sxxy");
        bind(sxyy, "sxyy");
        bind(syyy, "syyy");
        clear();
    }

    public void setOnChanged(Runnable listener) { this.onChange = listener; }

    public void setStress(boolean stress) {
        preset.setEnabled(stress);
        updateEnabled();
    }

    public void bind(BoundaryCondition boundary) {
        current = boundary;
        binding = true;
        if (boundary != null) {
            if (boundary.useXGrad && boundary.useYGrad) preset.setSelectedIndex(3);
            else if (boundary.useXGrad) preset.setSelectedIndex(1);
            else if (boundary.useYGrad) preset.setSelectedIndex(2);
            else preset.setSelectedIndex(0);
            sxxx.setText(sci(boundary.sxxx));
            sxyx.setText(sci(boundary.sxyx));
            syyx.setText(sci(boundary.syyx));
            sxxy.setText(sci(boundary.sxxy));
            sxyy.setText(sci(boundary.sxyy));
            syyy.setText(sci(boundary.syyy));
        }
        binding = false;
        updateEnabled();
    }

    public void clear() {
        current = null;
        binding = true;
        preset.setSelectedIndex(0);
        for (JTextField field : new JTextField[]{sxxx, sxyx, syyx, sxxy, sxyy, syyy}) {
            field.setText("0");
        }
        binding = false;
        preset.setEnabled(false);
        updateEnabled();
    }

    public void applyTo(BoundaryCondition boundary) {
        int index = preset.getSelectedIndex();
        boundary.useXGrad = index == 1 || index == 3;
        boundary.useYGrad = index == 2 || index == 3;
        boundary.sxxx = parse(sxxx.getText());
        boundary.sxyx = parse(sxyx.getText());
        boundary.syyx = parse(syyx.getText());
        boundary.sxxy = parse(sxxy.getText());
        boundary.sxyy = parse(sxyy.getText());
        boundary.syyy = parse(syyy.getText());
    }

    private void applyPreset() {
        if (binding || current == null) return;
        UndoManager.push();
        int index = preset.getSelectedIndex();
        current.useXGrad = index == 1 || index == 3;
        current.useYGrad = index == 2 || index == 3;
        updateEnabled();
        if (onChange != null) onChange.run();
    }

    private void bind(JTextField field, String key) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { save(field, key); }
        });
        field.addActionListener(e -> save(field, key));
    }

    private void save(JTextField field, String key) {
        if (binding || current == null) return;
        UndoManager.push();
        double value = parse(field.getText());
        switch (key) {
            case "sxxx": current.sxxx = value; break;
            case "sxyx": current.sxyx = value; break;
            case "syyx": current.syyx = value; break;
            case "sxxy": current.sxxy = value; break;
            case "sxyy": current.sxyy = value; break;
            case "syyy": current.syyy = value; break;
        }
        if (onChange != null) onChange.run();
    }

    private void updateEnabled() {
        int index = preset.getSelectedIndex();
        boolean stress = current != null && current.type.stress();
        preset.setEnabled(stress);
        sxxx.setEnabled(stress && (index == 1 || index == 3));
        sxyx.setEnabled(stress && (index == 1 || index == 3));
        syyx.setEnabled(stress && (index == 1 || index == 3));
        sxxy.setEnabled(stress && (index == 2 || index == 3));
        sxyy.setEnabled(stress && (index == 2 || index == 3));
        syyy.setEnabled(stress && (index == 2 || index == 3));
    }

    private void addField(GridBagConstraints c, int x, JTextField field, String tip) {
        c.gridx = x;
        field.setPreferredSize(new Dimension(72, 22));
        field.setToolTipText(tip);
        add(field, c);
    }

    private double parse(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private String sci(double value) {
        if (Math.abs(value) < 1e-12) return "0";
        return String.format(Locale.ROOT, "%.4e", value);
    }
}
