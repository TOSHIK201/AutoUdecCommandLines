import javax.swing.*;
import java.awt.*;

public class PlinePanel extends JPanel {
    private final LayerCanvas canvas;
    private final DefaultListModel<Pline> model = new DefaultListModel<>();
    private final JList<Pline> list = new JList<>(model);
    private final JTextField x1 = new JTextField(6), y1 = new JTextField(6);
    private final JTextField x2 = new JTextField(6), y2 = new JTextField(6);
    private final JTextField points = new JTextField(5);
    private boolean binding;

    public PlinePanel(LayerCanvas canvas) {
        super(new BorderLayout());
        this.canvas = canvas;

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        JButton draw = new JButton("画测线");
        draw.addActionListener(e -> canvas.setPlineMode(true));
        JButton apply = new JButton("应用");
        apply.addActionListener(e -> apply());
        JButton del = new JButton("删除");
        del.addActionListener(e -> delete());
        tools.add(draw);
        tools.add(apply);
        tools.add(del);
        add(tools, BorderLayout.NORTH);

        list.setVisibleRowCount(6);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object value,
                    int index, boolean selected, boolean focused) {
                super.getListCellRendererComponent(l, value, index, selected, focused);
                setText("#" + (index + 1) + "  " + value);
                return this;
            }
        });
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) bind();
        });
        add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("测线参数"));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(1, 3, 1, 3);
        row(form, c, 0, "起点 X/Y", x1, y1);
        row(form, c, 1, "终点 X/Y", x2, y2);
        c.gridy = 2; c.gridx = 0;
        form.add(new JLabel("节点数"), c);
        c.gridx = 1;
        points.setPreferredSize(new Dimension(70, 22));
        form.add(points, c);
        add(form, BorderLayout.SOUTH);

        canvas.setPlineFactory(this::newFromForm);
        canvas.setPlineCreated(this::added);
        reload();
    }

    public void reload() {
        binding = true;
        model.clear();
        for (Pline p : ParameterList.plines) model.addElement(p);
        binding = false;
        if (model.size() > 0) list.setSelectedIndex(0);
        else clearForm();
    }

    private Pline newFromForm() {
        Pline p = new Pline();
        p.points = (int) parse(points.getText(), ParameterList.plinePoints);
        return p;
    }

    private void added(Pline p) {
        UndoManager.push();
        ParameterList.plines.add(p);
        model.addElement(p);
        list.setSelectedIndex(model.size() - 1);
        canvas.repaint();
    }

    private void apply() {
        int index = list.getSelectedIndex();
        if (index < 0) return;
        Pline p = model.get(index);
        UndoManager.push();
        p.x1 = parse(x1.getText(), p.x1);
        p.y1 = parse(y1.getText(), p.y1);
        p.x2 = parse(x2.getText(), p.x2);
        p.y2 = parse(y2.getText(), p.y2);
        p.points = (int) parse(points.getText(), p.points);
        model.set(index, p);
        canvas.repaint();
    }

    private void delete() {
        int index = list.getSelectedIndex();
        if (index < 0) return;
        UndoManager.push();
        ParameterList.plines.remove(index);
        model.remove(index);
        if (model.size() > 0) list.setSelectedIndex(Math.min(index, model.size() - 1));
        else clearForm();
        canvas.repaint();
    }

    private void bind() {
        if (binding) return;
        Pline p = list.getSelectedValue();
        if (p == null) {
            canvas.setSelectedPline(-1);
            clearForm();
            return;
        }
        canvas.setSelectedPline(list.getSelectedIndex());
        binding = true;
        x1.setText(fmt(p.x1)); y1.setText(fmt(p.y1));
        x2.setText(fmt(p.x2)); y2.setText(fmt(p.y2));
        points.setText(String.valueOf(p.points));
        binding = false;
    }

    private void clearForm() {
        binding = true;
        x1.setText(""); y1.setText(""); x2.setText(""); y2.setText("");
        points.setText(String.valueOf(ParameterList.plinePoints));
        binding = false;
    }

    private static double parse(String text, double fallback) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private String fmt(double v) {
        if (v == Math.rint(v)) return String.format("%.0f", v);
        return String.format("%.2f", v);
    }

    private void row(JPanel panel, GridBagConstraints c, int row, String label,
                     JTextField f1, JTextField f2) {
        c.gridy = row; c.gridx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1; f1.setPreferredSize(new Dimension(90, 22)); panel.add(f1, c);
        c.gridx = 2; f2.setPreferredSize(new Dimension(90, 22)); panel.add(f2, c);
        c.gridx = 3;
    }
}
