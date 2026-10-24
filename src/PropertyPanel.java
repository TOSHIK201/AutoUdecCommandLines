import javax.swing.*;
import java.awt.*;

public class PropertyPanel extends JPanel {
    private final LayerGeometryPanel layerPanel = new LayerGeometryPanel();
    private final RockMaterialPanel materialPanel = new RockMaterialPanel();
    private final JointGeometryPanel jointGeometryPanel = new JointGeometryPanel();
    private final JointMaterialPanel jointMaterialPanel = new JointMaterialPanel();

    public PropertyPanel() {
        super(new BorderLayout());
    }

    public void setCanvas(LayerCanvas canvas) {
        layerPanel.setCanvas(canvas);
        materialPanel.setCanvas(canvas);
        jointGeometryPanel.setCanvas(canvas);
        jointMaterialPanel.setCanvas(canvas);
        layerPanel.setOnGeometryChanged(jointGeometryPanel::refresh);
        materialPanel.setOnMeshChanged(jointGeometryPanel::refresh);
    }

    public JPanel getLayerPanel() { return layerPanel; }
    public JPanel getRockMaterialPanel() { return materialPanel; }
    public JPanel getJointGeometryPanel() { return jointGeometryPanel; }
    public JPanel getJointMaterialPanel() { return jointMaterialPanel; }

    public void bindData(int index) {
        UndoManager.suspend(() -> {
            layerPanel.bindData(index);
            materialPanel.bindData(index);
            jointGeometryPanel.bindData(index);
            jointMaterialPanel.bindData(index);
        });
    }
}
