import javax.swing.*;
import java.awt.*;

public class ExcavationPropertyPanel extends JPanel {
    private final LayerCanvas canvas;
    private final DefaultListModel<ExcavationLayer> model = new DefaultListModel<>();
    private final JList<ExcavationLayer> list = new JList<>(model);
    private final ExcavationFormPanel form;
    private final JCheckBox coalShortcut = new JCheckBox("设为开挖煤层（快捷）");
    private final JToggleButton mode = new JToggleButton("框选开挖");
    private final JLabel status = new JLabel();
    private boolean binding;

    public ExcavationPropertyPanel(LayerCanvas canvas) {
        super(new BorderLayout());
        this.canvas = canvas;
        form = new ExcavationFormPanel(canvas, list, model);

        JPanel top = new JPanel(new BorderLayout());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        mode.setToolTipText("按下后在画布上拖动框选；再次点击结束框选");
        mode.addActionListener(e -> {
            if (!mode.isSelected()) {
                canvas.setExcavationMode(false);
                return;
            }
            String preset = (String) JOptionPane.showInputDialog(this, "选择开挖形式", "新建开挖",
                    JOptionPane.PLAIN_MESSAGE, null, ExcavationFormPanel.PRESETS,
                    ExcavationFormPanel.PRESETS[0]);
            if (preset == null) {
                mode.setSelected(false);
                return;
            }
            form.setPreset(preset);
            canvas.setExcavationMode(true);
        });
        JButton delete = new JButton("删除选中");
        delete.addActionListener(e -> deleteSelected());
        coalShortcut.setToolTipText("以当前选中岩层的中部快速建立一个居中开挖区域，并把该层标黑");
        coalShortcut.addActionListener(e -> toggleCoal());
        buttons.add(mode);
        buttons.add(delete);
        buttons.add(coalShortcut);
        top.add(buttons, BorderLayout.NORTH);
        status.setBorder(BorderFactory.createEmptyBorder(2, 6, 4, 6));
        top.add(status, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        list.setVisibleRowCount(5);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) bindSelected();
        });
        list.getInputMap().put(KeyStroke.getKeyStroke("DELETE"), "deleteExcavation");
        list.getActionMap().put("deleteExcavation", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                deleteSelected();
            }
        });

        JPanel body = new JPanel(new BorderLayout());
        body.setBorder(BorderFactory.createTitledBorder("开挖区域列表"));
        body.add(new JScrollPane(list), BorderLayout.CENTER);
        body.add(form, BorderLayout.SOUTH);
        add(body, BorderLayout.CENTER);

        canvas.setExcavationFactory(form::newFromForm);
        canvas.setExcavationCreated(this::added);
        canvas.setExcavationSelectionListener(this::selectIndex);
        canvas.setExcavationEdited(this::bindSelected);
        canvas.setExcavationModeListener(on -> {
            mode.setSelected(on);
            mode.setText(on ? "结束框选" : "框选开挖");
        });
        canvas.addSelectionListener(this::onLayerSelected);
        reload();
    }

    public void reload() {
        int keep = list.getSelectedIndex();
        binding = true;
        model.clear();
        for (ExcavationLayer e : ParameterList.excavationLayers) model.addElement(e);
        binding = false;
        if (model.size() > 0) list.setSelectedIndex(Math.max(0, Math.min(keep, model.size() - 1)));
        else form.clearForm();
        onLayerSelected(canvas.getSelectedIndex());
    }

    private void added(ExcavationLayer e) {
        UndoManager.push();
        ParameterList.excavationLayers.add(e);
        model.addElement(e);
        list.setSelectedIndex(model.size() - 1);
        canvas.repaint();
    }

    private void deleteSelected() {
        int index = list.getSelectedIndex();
        if (index < 0) return;
        ExcavationLayer e = model.get(index);
        UndoManager.push();
        ParameterList.excavationLayers.remove(e);
        model.remove(index);
        restoreColorIfUnlinked(e);
        if (model.size() > 0) list.setSelectedIndex(Math.min(index, model.size() - 1));
        else {
            form.clearForm();
            canvas.setSelectedExcavation(-1);
        }
        canvas.repaint();
    }

    private void selectIndex(int index) {
        if (index >= 0 && index < model.size()) list.setSelectedIndex(index);
    }

    private void bindSelected() {
        if (binding) return;
        ExcavationLayer e = list.getSelectedValue();
        canvas.setSelectedExcavation(list.getSelectedIndex());
        form.bindSelected();
        if (e == null) {
            status.setText("未选择开挖区域");
        } else {
            status.setText("已选 " + e.name + " · " + e.stepCount() + " 步");
        }
    }

    private void onLayerSelected(int index) {
        if (binding) return;
        boolean linked = find(index) != null;
        binding = true;
        coalShortcut.setSelected(linked);
        binding = false;
    }

    private void toggleCoal() {
        if (binding) return;
        int index = canvas.getSelectedIndex();
        if (index < 0 || index >= ParameterList.layerCount()) return;
        LayerData layer = ParameterList.layers.get(index);
        ExcavationLayer existing = find(index);
        UndoManager.push();
        if (coalShortcut.isSelected() && existing == null) {
            if (layer.savedColor == null) layer.savedColor = layer.color;
            layer.color = Color.BLACK;
            ExcavationLayer e = new ExcavationLayer(index, excavationName(layer, index));
            ParameterList.excavationLayers.add(e);
            ParameterList.syncFromLayers();
            reload();
            list.setSelectedIndex(model.size() - 1);
        } else if (!coalShortcut.isSelected() && existing != null) {
            ParameterList.excavationLayers.remove(existing);
            restoreColorIfUnlinked(existing);
            ParameterList.syncFromLayers();
            reload();
        }
        canvas.repaint();
    }

    private ExcavationLayer find(int layerIndex) {
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            if (e.layerIndex == layerIndex) return e;
        }
        return null;
    }

    private void restoreColorIfUnlinked(ExcavationLayer e) {
        if (e.layerIndex < 0 || find(e.layerIndex) != null) return;
        if (e.layerIndex >= ParameterList.layers.size()) return;
        LayerData layer = ParameterList.layers.get(e.layerIndex);
        if (layer.savedColor != null) {
            layer.color = layer.savedColor;
            layer.savedColor = null;
        }
    }

    private String excavationName(LayerData layer, int index) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)").matcher(layer.name);
        if (m.find()) return m.group(1);
        return String.valueOf(index + 1);
    }
}
