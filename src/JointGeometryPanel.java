import javax.swing.*;
import java.awt.*;
import java.util.List;

public class JointGeometryPanel extends JPanel {
    private final JLabel layerLabel = new JLabel("当前层：1");
    private final JPanel groupContainer = new JPanel();
    private int index = -1;
    private LayerCanvas canvas;

    public JointGeometryPanel() {
        super(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        layerLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        layerLabel.setForeground(new Color(0, 102, 204));
        top.add(layerLabel);
        add(top, BorderLayout.NORTH);

        groupContainer.setLayout(new BoxLayout(groupContainer, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(groupContainer);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        JButton addGroup = new JButton("+ 新建节理组");
        addGroup.setToolTipText("为当前岩层新增一组节理");
        addGroup.addActionListener(e -> addJointGroup());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        bottom.add(addGroup);
        add(bottom, BorderLayout.SOUTH);
    }

    public void setCanvas(LayerCanvas canvas) { this.canvas = canvas; }

    public void bindData(int layerIndex) {
        index = layerIndex;
        if (index >= 0 && index < ParameterList.layerCount()) {
            layerLabel.setText("当前层：" + (index + 1) + "  "
                    + ParameterList.layers.get(index).name);
        }
        refresh();
    }

    public void refresh() {
        UndoManager.suspend(this::refreshInternal);
    }

    private void refreshInternal() {
        groupContainer.removeAll();
        if (index < 0 || index >= ParameterList.layerCount()) {
            groupContainer.revalidate();
            groupContainer.repaint();
            return;
        }
        List<JointSet> joints = ParameterList.layers.get(index).joints;
        for (int i = 0; i < joints.size(); i++) {
            final JointSet target = joints.get(i);
            JointGroupPanel panel = new JointGroupPanel(i, target, canvas, () -> {
                if (joints.size() <= 1) {
                    JOptionPane.showMessageDialog(this, "至少保留一组节理");
                    return;
                }
                UndoManager.push();
                joints.remove(target);
                refresh();
                if (canvas != null) canvas.repaint();
            }, this::onLinkToggled, this::onSpacingChanged);
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            int prefHeight = Math.max(150, panel.getPreferredSize().height);
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, prefHeight));
            groupContainer.add(panel);
            groupContainer.add(Box.createVerticalStrut(6));
        }
        groupContainer.revalidate();
        groupContainer.repaint();
    }

    private void addJointGroup() {
        if (index < 0 || index >= ParameterList.layerCount()) return;
        LayerData layer = ParameterList.layers.get(index);
        List<Double> heights = ParameterList.heights();
        double bottom = heights.get(index + 1);
        UndoManager.push();
        layer.joints.add(new JointSet(ParameterList.jointAngle,
                ParameterList.jointStartX, bottom));
        refresh();
        if (canvas != null) canvas.repaint();
    }

    private void onLinkToggled(JointSet source) {
        if (index < 0 || index >= ParameterList.layerCount()) return;
        if (source.linkSpacing) {
            for (JointSet other : ParameterList.layers.get(index).joints) {
                if (other != source && other.linkSpacing) {
                    source.spacing = other.spacing;
                    break;
                }
            }
        }
        refresh();
        if (canvas != null) canvas.repaint();
    }

    private void onSpacingChanged(JointSet source) {
        if (index < 0 || index >= ParameterList.layerCount()) return;
        if (source.linkSpacing) {
            for (JointSet other : ParameterList.layers.get(index).joints) {
                if (other != source && other.linkSpacing) other.spacing = source.spacing;
            }
            refresh();
        }
        if (canvas != null) canvas.repaint();
    }
}
