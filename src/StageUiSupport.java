import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

class StageUiSupport {
    static GridBagConstraints grid() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 4, 2, 4);
        return c;
    }

    static void addRow(JPanel panel, GridBagConstraints c, int row, String label,
                       JComponent field, int width) {
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 0;
        if (field instanceof JTextField) {
            field.setPreferredSize(new Dimension(width, 22));
        }
        panel.add(field, c);
        c.gridx = 2;
        c.weightx = 1.0;
        panel.add(new JLabel(""), c);
        c.weightx = 0;
    }

    static void bindDouble(JTextField field, DoubleConsumer setter, LayerCanvas canvas) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                saveDouble(field, setter, canvas);
            }
        });
        field.addActionListener(e -> saveDouble(field, setter, canvas));
    }

    static void bindString(JTextField field, Consumer<String> setter) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                saveString(field, setter);
            }
        });
        field.addActionListener(e -> saveString(field, setter));
    }

    private static void saveDouble(JTextField field, DoubleConsumer setter, LayerCanvas canvas) {
        try {
            String text = field.getText().trim();
            if (text.isEmpty()) return;
            UndoManager.push();
            setter.accept(Double.parseDouble(text));
            if (canvas != null) canvas.repaint();
        } catch (NumberFormatException ignored) {
        }
    }

    private static void saveString(JTextField field, Consumer<String> setter) {
        String text = field.getText().trim();
        if (text.isEmpty()) return;
        UndoManager.push();
        setter.accept(text);
    }
}
