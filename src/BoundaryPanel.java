import javax.swing.*;
import java.awt.*;
import java.util.function.DoubleConsumer;

public class BoundaryPanel extends JPanel {
    private final LayerCanvas canvas;
    private final DefaultListModel<BoundaryCondition> model = new DefaultListModel<>();
    private final JList<BoundaryCondition> list = new JList<>(model);
    private final BoundaryFormPanel form;
    private final JTextField gravityField = new JTextField(5);
    private boolean binding;

    public BoundaryPanel(LayerCanvas canvas) {
        super(new BorderLayout());
        this.canvas = canvas;
        form = new BoundaryFormPanel(canvas, list, model);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        top.add(new JLabel("重力加速度"));
        gravityField.setPreferredSize(new Dimension(70, 22));
        gravityField.setText(BoundaryCondition.fmt(ParameterList.gravity));
        bindDouble(gravityField, v -> ParameterList.gravity = v);
        top.add(gravityField);
        top.add(new JLabel("默认边界：左右与底部速度约束，顶部应力边界（分量手动输入）"));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        JButton draw = new JButton("绘制边界");
        draw.setToolTipText("两点定义输入线；UDEC 按矩形 range 生效，斜线取较长方向条带近似");
        draw.addActionListener(e -> canvas.setBoundaryMode(true));
        JButton delete = new JButton("删除选中");
        delete.addActionListener(e -> deleteSelected());
        toolbar.add(draw);
        toolbar.add(delete);

        list.setVisibleRowCount(4);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) bindSelected();
        });
        list.getInputMap().put(KeyStroke.getKeyStroke("DELETE"), "deleteBoundary");
        list.getActionMap().put("deleteBoundary", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                deleteSelected();
            }
        });

        JPanel boundary = new JPanel(new BorderLayout());
        boundary.setBorder(BorderFactory.createTitledBorder("边界条件列表"));
        boundary.add(toolbar, BorderLayout.NORTH);
        boundary.add(new JScrollPane(list), BorderLayout.CENTER);
        boundary.add(form, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(boundary, BorderLayout.CENTER);

        canvas.setBoundaryFactory(form::newFromForm);
        canvas.setBoundaryCreated(this::added);
        canvas.setBoundarySelectionListener(this::selectIndex);
        reload();
    }

    public void reload() {
        binding = true;
        model.clear();
        for (BoundaryCondition b : ParameterList.boundaries) model.addElement(b);
        binding = false;
        if (model.size() > 0) list.setSelectedIndex(0);
        else form.clearForm();
    }

    private void added(BoundaryCondition b) {
        UndoManager.push();
        ParameterList.boundaries.add(b);
        model.addElement(b);
        list.setSelectedIndex(model.size() - 1);
        canvas.repaint();
    }

    private void deleteSelected() {
        int index = list.getSelectedIndex();
        if (index < 0) return;
        UndoManager.push();
        ParameterList.boundaries.remove(index);
        model.remove(index);
        if (model.size() > 0) list.setSelectedIndex(Math.min(index, model.size() - 1));
        else {
            form.clearForm();
            canvas.setSelectedBoundary(-1);
        }
        canvas.repaint();
    }

    private void selectIndex(int index) {
        if (index >= 0 && index < model.size()) list.setSelectedIndex(index);
    }

    private void bindSelected() {
        if (binding) return;
        canvas.setSelectedBoundary(list.getSelectedIndex());
        form.bindSelected();
    }

    private void bindDouble(JTextField field, DoubleConsumer setter) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                saveDouble(field, setter);
            }
        });
        field.addActionListener(e -> saveDouble(field, setter));
    }

    private void saveDouble(JTextField field, DoubleConsumer setter) {
        if (binding) return;
        String text = field.getText().trim();
        if (text.isEmpty()) return;
        try {
            double value = Double.parseDouble(text);
            UndoManager.push();
            setter.accept(value);
        } catch (NumberFormatException ignored) {
        }
    }
}
