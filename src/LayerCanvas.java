import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

public class LayerCanvas extends JPanel {
    private final PropertyPanel propertyPanel;
    private JLabel statusLabel;
    private int selectedIndex = 0;
    private double scale = 1.0;
    private double originX = 60;
    private double originY = 40;
    private int lastX, lastY;
    private Double snapX, snapY;
    private double mouseWorldX, mouseWorldY;
    private int mouseScreenX = -1, mouseScreenY = -1;
    private final JCheckBox snapToggle = new JCheckBox("吸附", true);
    private final JCheckBox gridToggle = new JCheckBox("网格", false);
    private final JCheckBox measureToggle = new JCheckBox("测量", false);
    private final JCheckBox boundaryToggle = new JCheckBox("边界", false);
    private final JCheckBox excavationToggle = new JCheckBox("开挖", false);
    private final JCheckBox boltToggle = new JCheckBox("锚杆", false);
    private final JCheckBox plineToggle = new JCheckBox("测线", false);
    private Double measureX1, measureY1, measureX2, measureY2;
    private Double boundX1, boundY1;
    private Supplier<BoundaryCondition> boundaryFactory;
    private Consumer<BoundaryCondition> boundaryCreated;
    private IntConsumer boundarySelectionListener;
    private int selectedBoundary = -1;
    private Double excStartX, excStartY;
    private double excEndX, excEndY;
    private Supplier<ExcavationLayer> excavationFactory;
    private Consumer<ExcavationLayer> excavationCreated;
    private IntConsumer excavationSelectionListener;
    private Consumer<Boolean> excavationModeListener;
    private Runnable excavationEdited;
    private int selectedExcavation = -1;
    private int resizeIndex = -1;
    private int resizeCorner = -1;
    private double resizeFixedX, resizeFixedY;
    private List<List<double[]>> jointCache;
    private long jointSignature;
    private Double boltX1, boltY1;
    private Double plineX1, plineY1;
    private Supplier<BoltGroup> boltFactory;
    private Consumer<BoltGroup> boltCreated;
    private Supplier<Pline> plineFactory;
    private Consumer<Pline> plineCreated;
    private int selectedPline = -1;
    private int plineResizeIndex = -1;
    private int plineResizeEnd = -1;
    private int selectedBoltGroup = -1;
    private int[] selectedBoltIndices = new int[0];
    private int activeStage = 0;
    private String stage4Group;
    private boolean dragging;

    private static final Color BG = new Color(42, 47, 56);
    private static final Color GRID = new Color(68, 74, 84);
    private static final Color AXIS = new Color(190, 200, 210);
    private static final Color SELECT = new Color(255, 70, 70);
    private static final Font FONT_11 = new Font("微软雅黑", Font.PLAIN, 11);
    private static final Font FONT_BOLD_12 = new Font("微软雅黑", Font.BOLD, 12);
    private static final Font FONT_GRID = new Font("Arial", Font.PLAIN, 10);
    private final java.util.List<java.util.function.IntConsumer> selectionListeners = new java.util.ArrayList<>();

    public LayerCanvas(PropertyPanel propertyPanel) {
        this.propertyPanel = propertyPanel;
        setBackground(BG);
        setFocusable(true);
        setLayout(null);
        snapToggle.setOpaque(false);
        snapToggle.setForeground(Color.WHITE);
        snapToggle.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        snapToggle.setFocusable(false);
        snapToggle.setToolTipText("勾选时吸附到层界面、裂隙与模型边界；取消后自由移动");
        snapToggle.addActionListener(e -> refreshSnap());
        add(snapToggle);
        gridToggle.setOpaque(false);
        gridToggle.setForeground(Color.WHITE);
        gridToggle.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        gridToggle.setFocusable(false);
        gridToggle.setToolTipText("显示或隐藏坐标网格");
        gridToggle.addActionListener(e -> repaint());
        add(gridToggle);
        measureToggle.setOpaque(false);
        measureToggle.setForeground(Color.WHITE);
        measureToggle.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        measureToggle.setFocusable(false);
        measureToggle.setToolTipText("左键依次点两个点测距；右键清除");
        measureToggle.addActionListener(e -> {
            if (measureToggle.isSelected()) {
                boundaryToggle.setSelected(false);
                excavationToggle.setSelected(false);
                boltToggle.setSelected(false);
                plineToggle.setSelected(false);
                boundX1 = null;
                boundY1 = null;
                excStartX = null;
                excStartY = null;
                boltX1 = null;
                plineX1 = null;
            } else {
                clearMeasure();
            }
            repaint();
        });
        add(measureToggle);
        boundaryToggle.setOpaque(false);
        boundaryToggle.setForeground(Color.WHITE);
        boundaryToggle.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        boundaryToggle.setFocusable(false);
        boundaryToggle.setToolTipText("左键依次点两个点新建边界；右键取消当前点");
        boundaryToggle.addActionListener(e -> {
            if (boundaryToggle.isSelected()) {
                measureToggle.setSelected(false);
                excavationToggle.setSelected(false);
                boltToggle.setSelected(false);
                plineToggle.setSelected(false);
                clearMeasure();
            } else {
                boundX1 = null;
                boundY1 = null;
            }
            repaint();
        });
        add(boundaryToggle);
        excavationToggle.setOpaque(false);
        excavationToggle.setForeground(Color.WHITE);
        excavationToggle.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        excavationToggle.setFocusable(false);
        excavationToggle.setToolTipText("在画布上按住左键拖动，框选开挖区域；单击可选择已有区域");
        excavationToggle.addActionListener(e -> {
            if (excavationToggle.isSelected()) {
                measureToggle.setSelected(false);
                boundaryToggle.setSelected(false);
                boltToggle.setSelected(false);
                plineToggle.setSelected(false);
                clearMeasure();
                boundX1 = null;
                boundY1 = null;
                boltX1 = null;
                plineX1 = null;
            } else {
                excStartX = null;
                excStartY = null;
            }
            notifyExcavationMode(excavationToggle.isSelected());
            repaint();
        });
        add(excavationToggle);
        boltToggle.setOpaque(false);
        boltToggle.setForeground(Color.WHITE);
        boltToggle.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        boltToggle.setFocusable(false);
        boltToggle.setToolTipText("左键依次点两个点定义锚杆安装线");
        boltToggle.addActionListener(e -> {
            if (boltToggle.isSelected()) {
                measureToggle.setSelected(false);
                boundaryToggle.setSelected(false);
                excavationToggle.setSelected(false);
                plineToggle.setSelected(false);
                clearMeasure();
                boundX1 = null; boundY1 = null; excStartX = null; excStartY = null;
                plineX1 = null; plineY1 = null;
            } else {
                boltX1 = null; boltY1 = null;
            }
            repaint();
        });
        add(boltToggle);
        plineToggle.setOpaque(false);
        plineToggle.setForeground(Color.WHITE);
        plineToggle.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        plineToggle.setFocusable(false);
        plineToggle.setToolTipText("左键依次点两个点定义测线");
        plineToggle.addActionListener(e -> {
            if (plineToggle.isSelected()) {
                measureToggle.setSelected(false);
                boundaryToggle.setSelected(false);
                excavationToggle.setSelected(false);
                boltToggle.setSelected(false);
                clearMeasure();
                boundX1 = null; boundY1 = null; excStartX = null; excStartY = null;
                boltX1 = null; boltY1 = null;
            } else {
                plineX1 = null; plineY1 = null;
            }
            repaint();
        });
        add(plineToggle);

        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                updateMouse(e.getX(), e.getY());
                lastX = e.getX(); lastY = e.getY();
                dragging = false;
                double wx = snapX != null ? snapX : mouseWorldX;
                double wy = snapY != null ? snapY : mouseWorldY;
                if (activeStage == 3) {
                    int[] ph = hitPlineHandle(wx, wy);
                    if (ph != null) {
                        plineResizeIndex = ph[0];
                        plineResizeEnd = ph[1];
                        selectedPline = ph[0];
                        UndoManager.push();
                        repaint();
                        return;
                    }
                }
                int[] hit = activeStage == 0 ? ExcavationOverlay.handleHit(
                        ParameterList.excavationLayers, selectedExcavation,
                        wx, wy, 6.0 / scale) : null;
                if (hit != null) {
                    resizeIndex = hit[0];
                    resizeCorner = hit[1];
                    setSelectedExcavation(resizeIndex);
                    if (excavationSelectionListener != null) {
                        excavationSelectionListener.accept(resizeIndex);
                    }
                    ExcavationLayer region = ParameterList.excavationLayers.get(resizeIndex);
                    double x1 = Math.min(region.x1, region.x2);
                    double x2 = Math.max(region.x1, region.x2);
                    double y1 = Math.min(region.y1, region.y2);
                    double y2 = Math.max(region.y1, region.y2);
                    if (resizeCorner == 0) { resizeFixedX = x2; resizeFixedY = y2; }
                    else if (resizeCorner == 1) { resizeFixedX = x1; resizeFixedY = y2; }
                    else if (resizeCorner == 2) { resizeFixedX = x1; resizeFixedY = y1; }
                    else { resizeFixedX = x2; resizeFixedY = y1; }
                    UndoManager.push();
                    return;
                }
                if (excavationToggle.isSelected()) {
                    excStartX = snapX != null ? snapX : mouseWorldX;
                    excStartY = snapY != null ? snapY : mouseWorldY;
                    excEndX = excStartX;
                    excEndY = excStartY;
                }
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (measureToggle.isSelected()) clearMeasure();
                    if (boundaryToggle.isSelected()) {
                        boundX1 = null;
                        boundY1 = null;
                        repaint();
                    }
                    if (excavationToggle.isSelected()) {
                        excStartX = null;
                        excStartY = null;
                        resizeIndex = -1;
                        resizeCorner = -1;
                        repaint();
                    }
                    if (boltToggle.isSelected()) {
                        boltX1 = null; boltY1 = null; repaint();
                    }
                    if (plineToggle.isSelected()) {
                        plineX1 = null; plineY1 = null; repaint();
                    }
                    return;
                }
                if (plineResizeIndex >= 0) {
                    plineResizeIndex = -1;
                    plineResizeEnd = -1;
                    repaint();
                    return;
                }
                if (activeStage == 3 && boltToggle.isSelected()) {
                    addBoltPoint(e.getX(), e.getY());
                    return;
                }
                if (activeStage == 3 && plineToggle.isSelected()) {
                    addPlinePoint(e.getX(), e.getY());
                    return;
                }
                if (resizeIndex >= 0) {
                    resizeIndex = -1;
                    resizeCorner = -1;
                    if (excavationEdited != null) excavationEdited.run();
                    repaint();
                    return;
                }
                if (excavationToggle.isSelected() && excStartX != null) {
                    updateMouse(e.getX(), e.getY());
                    excEndX = snapX != null ? snapX : mouseWorldX;
                    excEndY = snapY != null ? snapY : mouseWorldY;
                    double w = Math.abs(excEndX - excStartX);
                    double h = Math.abs(excEndY - excStartY);
                    if (w < 0.05 || h < 0.05) {
                        int hit = hitExcavation(excStartX, excStartY);
                        if (hit >= 0) {
                            setSelectedExcavation(hit);
                            if (excavationSelectionListener != null) {
                                excavationSelectionListener.accept(hit);
                            }
                        }
                    } else if (excavationFactory != null) {
                        ExcavationLayer region = excavationFactory.get();
                        if (region != null) {
                            region.x1 = Math.min(excStartX, excEndX);
                            region.x2 = Math.max(excStartX, excEndX);
                            region.y1 = Math.min(excStartY, excEndY);
                            region.y2 = Math.max(excStartY, excEndY);
                            region.autoGeometry = false;
                            if (excavationCreated != null) excavationCreated.accept(region);
                        }
                    }
                    excStartX = null;
                    excStartY = null;
                    repaint();
                    return;
                }
                if (!dragging && Math.abs(e.getX() - lastX) < 4 && Math.abs(e.getY() - lastY) < 4) {
                    if (activeStage == 2 && boundaryToggle.isSelected()) {
                        addBoundaryPoint(e.getX(), e.getY());
                        return;
                    }
                    if (measureToggle.isSelected()) {
                        addMeasurePoint(e.getX(), e.getY());
                        return;
                    }
                    if (activeStage == 2) {
                        int hit = hitBoundary(toWorldX(e.getX()), toWorldY(e.getY()));
                        if (hit >= 0) {
                            setSelectedBoundary(hit);
                            if (boundarySelectionListener != null) boundarySelectionListener.accept(hit);
                            return;
                        }
                    }
                    if (activeStage <= 1) {
                        selectLayerAt(toWorldY(e.getY()));
                    }
                }
            }
            @Override public void mouseClicked(MouseEvent e) {
                if (!measureToggle.isSelected() && !boundaryToggle.isSelected()
                        && !excavationToggle.isSelected()
                        && !boltToggle.isSelected() && !plineToggle.isSelected()
                        && e.getClickCount() == 2) fitToScreen();
            }
        };
        addMouseListener(mouse);

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (activeStage == 3 && plineResizeIndex >= 0
                        && plineResizeIndex < ParameterList.plines.size()) {
                    updateMouse(e.getX(), e.getY());
                    double wx = snapX != null ? snapX : mouseWorldX;
                    double wy = snapY != null ? snapY : mouseWorldY;
                    Pline p = ParameterList.plines.get(plineResizeIndex);
                    if (plineResizeEnd == 0) {
                        p.x1 = wx; p.y1 = wy;
                    } else {
                        p.x2 = wx; p.y2 = wy;
                    }
                    repaint();
                    return;
                }
                if (activeStage == 3 && (boltToggle.isSelected() || plineToggle.isSelected())) return;
                if (activeStage == 0 && resizeIndex >= 0
                        && resizeIndex < ParameterList.excavationLayers.size()) {
                    updateMouse(e.getX(), e.getY());
                    double wx = snapX != null ? snapX : mouseWorldX;
                    double wy = snapY != null ? snapY : mouseWorldY;
                    ExcavationLayer region = ParameterList.excavationLayers.get(resizeIndex);
                    region.x1 = Math.min(resizeFixedX, wx);
                    region.x2 = Math.max(resizeFixedX, wx);
                    region.y1 = Math.min(resizeFixedY, wy);
                    region.y2 = Math.max(resizeFixedY, wy);
                    region.autoGeometry = false;
                    repaint();
                    return;
                }
                if (excavationToggle.isSelected() && excStartX != null) {
                    updateMouse(e.getX(), e.getY());
                    excEndX = snapX != null ? snapX : mouseWorldX;
                    excEndY = snapY != null ? snapY : mouseWorldY;
                    dragging = true;
                    repaint();
                    return;
                }
                dragging = true;
                originX += e.getX() - lastX;
                originY -= e.getY() - lastY;
                lastX = e.getX(); lastY = e.getY();
                repaint();
            }
            @Override public void mouseMoved(MouseEvent e) {
                updateMouse(e.getX(), e.getY());
            }
        });

        addMouseWheelListener(e -> {
            double wx = toWorldX(e.getX());
            double wy = toWorldY(e.getY());
            double factor = e.getWheelRotation() < 0 ? 1.12 : 1.0 / 1.12;
            scale = Math.max(0.05, Math.min(80.0, scale * factor));
            originX = e.getX() - wx * scale;
            originY = getHeight() - e.getY() - wy * scale;
            updateMouse(e.getX(), e.getY());
            repaint();
        });
    }

    public void setStatusLabel(JLabel label) { this.statusLabel = label; }
    public int getSelectedIndex() { return selectedIndex; }
    public double getScale() { return scale; }
    public double getOriginX() { return originX; }
    public double getOriginY() { return originY; }
    public boolean isSnapEnabled() { return snapToggle.isSelected(); }
    public boolean isGridEnabled() { return gridToggle.isSelected(); }

    public void setView(double scale, double originX, double originY) {
        this.scale = scale;
        this.originX = originX;
        this.originY = originY;
        repaint();
    }

    public void setSnapEnabled(boolean enabled) { snapToggle.setSelected(enabled); }
    public void setGridEnabled(boolean enabled) { gridToggle.setSelected(enabled); }

    public void setBoundaryMode(boolean enabled) {
        if (enabled && activeStage != 2) return;
        boundaryToggle.setSelected(enabled);
        if (enabled) {
            measureToggle.setSelected(false);
            clearMeasure();
        }
        if (!enabled) {
            boundX1 = null;
            boundY1 = null;
        }
        if (mouseScreenX >= 0 && mouseScreenY >= 0) {
            updateMouse(mouseScreenX, mouseScreenY);
        }
        repaint();
    }

    public boolean isBoundaryMode() {
        return boundaryToggle.isSelected();
    }

    public void setBoundaryFactory(Supplier<BoundaryCondition> factory) {
        boundaryFactory = factory;
    }

    public void setBoundaryCreated(Consumer<BoundaryCondition> listener) {
        boundaryCreated = listener;
    }

    public void setBoundarySelectionListener(IntConsumer listener) {
        boundarySelectionListener = listener;
    }

    public void setSelectedBoundary(int index) {
        selectedBoundary = index;
        repaint();
    }

    public int getSelectedBoundary() {
        return selectedBoundary;
    }

    public void setExcavationMode(boolean enabled) {
        if (enabled && activeStage != 0) return;
        excavationToggle.setSelected(enabled);
        if (enabled) {
            measureToggle.setSelected(false);
            boundaryToggle.setSelected(false);
            clearMeasure();
            boundX1 = null;
            boundY1 = null;
        } else {
            excStartX = null;
            excStartY = null;
        }
        notifyExcavationMode(enabled);
        repaint();
    }

    public boolean isExcavationMode() {
        return excavationToggle.isSelected();
    }

    public void setExcavationFactory(Supplier<ExcavationLayer> factory) {
        excavationFactory = factory;
    }

    public void setExcavationCreated(Consumer<ExcavationLayer> listener) {
        excavationCreated = listener;
    }

    public void setExcavationSelectionListener(IntConsumer listener) {
        excavationSelectionListener = listener;
    }

    public void setExcavationModeListener(Consumer<Boolean> listener) {
        excavationModeListener = listener;
    }

    public void setExcavationEdited(Runnable listener) {
        excavationEdited = listener;
    }

    private void notifyExcavationMode(boolean enabled) {
        if (excavationModeListener != null) excavationModeListener.accept(enabled);
    }

    public void setSelectedExcavation(int index) {
        selectedExcavation = index;
        repaint();
    }

    public int getSelectedExcavation() {
        return selectedExcavation;
    }

    public void setBoltMode(boolean enabled) {
        if (enabled && activeStage != 3) return;
        boltToggle.setSelected(enabled);
        if (enabled) {
            plineToggle.setSelected(false);
            excavationToggle.setSelected(false);
            boundaryToggle.setSelected(false);
            measureToggle.setSelected(false);
        } else {
            boltX1 = null;
            boltY1 = null;
        }
        repaint();
    }

    public void setBoltFactory(Supplier<BoltGroup> factory) {
        boltFactory = factory;
    }

    public void setBoltCreated(Consumer<BoltGroup> listener) {
        boltCreated = listener;
    }

    public void setPlineMode(boolean enabled) {
        if (enabled && activeStage != 3) return;
        plineToggle.setSelected(enabled);
        if (enabled) {
            boltToggle.setSelected(false);
            excavationToggle.setSelected(false);
            boundaryToggle.setSelected(false);
            measureToggle.setSelected(false);
        } else {
            plineX1 = null;
            plineY1 = null;
        }
        repaint();
    }

    public void setPlineFactory(Supplier<Pline> factory) {
        plineFactory = factory;
    }

    public void setPlineCreated(Consumer<Pline> listener) {
        plineCreated = listener;
    }

    public void setSelectedPline(int index) {
        selectedPline = index;
        repaint();
    }

    public void setBoltSelection(int group, int[] indices) {
        selectedBoltGroup = group;
        selectedBoltIndices = indices == null ? new int[0] : indices.clone();
        repaint();
    }

    public void setActiveStage(int stage) {
        activeStage = stage;
        if (stage != 3) {
            stage4Group = null;
            boltToggle.setSelected(false);
            plineToggle.setSelected(false);
            boltX1 = null;
            plineX1 = null;
        }
        if (stage != 2) {
            boundaryToggle.setSelected(false);
            boundX1 = null;
        }
        if (stage != 0) {
            excavationToggle.setSelected(false);
            excStartX = null;
        }
        repaint();
    }

    public void setStage4Group(String group) {
        stage4Group = group;
        repaint();
    }

    @Override
    public void doLayout() {
        Dimension d = snapToggle.getPreferredSize();
        snapToggle.setBounds(8, 6, d.width, d.height);
        Dimension g = gridToggle.getPreferredSize();
        gridToggle.setBounds(8 + d.width + 8, 6, g.width, g.height);
        Dimension m = measureToggle.getPreferredSize();
        measureToggle.setBounds(8 + d.width + 8 + g.width + 8, 6, m.width, m.height);
        Dimension b = boundaryToggle.getPreferredSize();
        boundaryToggle.setBounds(8 + d.width + 8 + g.width + 8 + m.width + 8, 6,
                b.width, b.height);
        Dimension x = excavationToggle.getPreferredSize();
        excavationToggle.setBounds(8 + d.width + 8 + g.width + 8 + m.width + 8 + b.width + 8,
                6, x.width, x.height);
        Dimension bo = boltToggle.getPreferredSize();
        boltToggle.setBounds(8 + d.width + 8 + g.width + 8 + m.width + 8 + b.width + 8
                + x.width + 8, 6, bo.width, bo.height);
        Dimension pl = plineToggle.getPreferredSize();
        plineToggle.setBounds(8 + d.width + 8 + g.width + 8 + m.width + 8 + b.width + 8
                + x.width + 8 + bo.width + 8, 6, pl.width, pl.height);
    }

    public void addSelectionListener(java.util.function.IntConsumer listener) {
        selectionListeners.add(listener);
    }

    private void notifySelection(int index) {
        for (java.util.function.IntConsumer listener : selectionListeners) {
            listener.accept(index);
        }
    }

    public void selectLayer(int index) {
        if (index < 0 || index >= ParameterList.layerCount()) return;
        selectedIndex = index;
        propertyPanel.bindData(index);
        notifySelection(index);
        repaint();
    }

    public void resetSelectedIndex() {
        int n = ParameterList.layerCount();
        selectedIndex = Math.max(0, Math.min(selectedIndex, n - 1));
        repaint();
    }

    public void fitToScreen() {
        if (getWidth() <= 0 || getHeight() <= 0) return;
        List<Double> hs = ParameterList.heights();
        double maxH = hs.get(0);
        double len = Math.max(1.0, ParameterList.modelLength);
        scale = Math.min((getWidth() - 140.0) / len, (getHeight() - 110.0) / maxH) * 0.92;
        scale = Math.max(0.05, scale);
        originX = 70;
        originY = 45;
        repaint();
    }

    private void selectLayerAt(double worldY) {
        if (activeStage > 1) return;
        double y = worldY;
        List<Double> hs = ParameterList.heights();
        for (int i = 0; i < hs.size() - 1; i++) {
            if (y <= hs.get(i) + 1e-9 && y >= hs.get(i + 1) - 1e-9) {
                selectedIndex = i;
                propertyPanel.bindData(i);
                notifySelection(i);
                repaint();
                return;
            }
        }
    }

    private void updateMouse(int sx, int sy) {
        mouseScreenX = sx;
        mouseScreenY = sy;
        mouseWorldX = toWorldX(sx);
        mouseWorldY = toWorldY(sy);
        snapX = null;
        snapY = null;
        double tol = 12.0 / scale;
        if (snapToggle.isSelected()) {
            snapY = SnapTargets.nearest(mouseWorldY, SnapTargets.yTargets(mouseWorldX, tol), tol);
            snapX = SnapTargets.nearest(mouseWorldX, SnapTargets.xTargets(mouseWorldY, tol), tol);
        }
        if (statusLabel != null) {
            double sxShow = snapX != null ? snapX : mouseWorldX;
            double syShow = snapY != null ? snapY : mouseWorldY;
            statusLabel.setText(String.format("x = %.2f m%s   y = %.2f m%s   当前第 %d 层%s%s",
                    sxShow, snapX != null ? " [吸附]" : "",
                    syShow, snapY != null ? " [吸附]" : "",
                    selectedIndex + 1, snapToggle.isSelected() ? "" : "   吸附关",
                    measureHint() + boundaryHint() + excavationHint()
                            + boltHint() + plineHint()));
        }
        repaint();
    }

    private void refreshSnap() {
        if (mouseScreenX >= 0 && mouseScreenY >= 0) {
            updateMouse(mouseScreenX, mouseScreenY);
        } else {
            snapX = null;
            snapY = null;
            repaint();
        }
    }

    private double toWorldX(int sx) { return (sx - originX) / scale; }
    private double toWorldY(int sy) { return (getHeight() - originY - sy) / scale; }
    private int toScreenX(double wx) { return (int) Math.round(originX + wx * scale); }
    private int toScreenY(double wy) { return (int) Math.round(getHeight() - originY - wy * scale); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(BG);
        g2.fillRect(0, 0, getWidth(), getHeight());

        double[] hs = heights();
        ensureJointCache();
        int left = toScreenX(0);
        int right = toScreenX(ParameterList.modelLength);

        for (int i = 0; i < ParameterList.layerCount(); i++) {
            LayerData layer = ParameterList.layers.get(i);
            double top = hs[i];
            double bottom = hs[i + 1];
            int yTop = toScreenY(top);
            int yBottom = toScreenY(bottom);

            g2.setColor(layer.color);
            g2.fillRect(left, yTop, right - left, Math.max(1, yBottom - yTop));

            Shape oldClip = g2.getClip();
            g2.clipRect(left, yTop, right - left, Math.max(1, yBottom - yTop));
            drawJoints(g2, i);
            g2.setClip(oldClip);

            if (i == selectedIndex) {
                g2.setColor(SELECT);
                g2.setStroke(new BasicStroke(2.2f));
                g2.drawRect(left, yTop, right - left, yBottom - yTop);
                g2.setStroke(new BasicStroke(1f));
            }

            int visTop = Math.max(yTop, 0);
            int visBottom = Math.min(yBottom, getHeight());
            if (right > 8 && left < getWidth() - 8 && visBottom - visTop >= 12) {
                String label = String.format("%s  %.2f m", layer.name, layer.thickness);
                g2.setFont(FONT_11);
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(label);
                int th = fm.getHeight();
                int lx = left + 8;
                if (lx + tw + 8 > right) lx = right - tw - 8;
                lx = Math.max(8, Math.min(lx, getWidth() - tw - 8));
                int ly = (visTop + visBottom) / 2 - th / 2;
                ly = Math.max(2, Math.min(ly, getHeight() - th - 2));
                g2.setColor(new Color(0, 0, 0, 140));
                g2.fillRoundRect(lx - 4, ly, tw + 8, th + 2, 8, 8);
                g2.setColor(i == selectedIndex ? Color.YELLOW : new Color(245, 245, 245));
                g2.drawString(label, lx, ly + fm.getAscent());
            }
        }

        drawCracks(g2, hs);
        drawBoundaries(g2);
        drawExcavations(g2);
        if (activeStage == 3 && stage4Group != null) {
            Stage4Overlay.draw(g2, stage4Group, this::toScreenX, this::toScreenY, getWidth());
        }
        drawBolts(g2);
        drawPlines(g2);
        if (gridToggle.isSelected()) drawGrid(g2);
        if (snapY != null) drawSnapLine(g2, 0, toScreenY(snapY), getWidth(), toScreenY(snapY));
        if (snapX != null) drawSnapLine(g2, toScreenX(snapX), 0, toScreenX(snapX), getHeight());
        if (snapX != null && snapY != null) {
            int cx = toScreenX(snapX);
            int cy = toScreenY(snapY);
            g2.setColor(new Color(255, 205, 0));
            g2.fillOval(cx - 4, cy - 4, 8, 8);
        }
        drawMeasurement(g2);
    }

    private void addMeasurePoint(int sx, int sy) {
        updateMouse(sx, sy);
        double wx = snapX != null ? snapX : mouseWorldX;
        double wy = snapY != null ? snapY : mouseWorldY;
        if (measureX1 == null || measureX2 != null) {
            measureX1 = wx;
            measureY1 = wy;
            measureX2 = null;
            measureY2 = null;
        } else {
            measureX2 = wx;
            measureY2 = wy;
        }
        updateMouse(sx, sy);
    }

    private void clearMeasure() {
        measureX1 = null;
        measureY1 = null;
        measureX2 = null;
        measureY2 = null;
        repaint();
    }

    private double measureDistance() {
        if (measureX1 == null || measureX2 == null) return Double.NaN;
        return Math.hypot(measureX2 - measureX1, measureY2 - measureY1);
    }

    private String measureHint() {
        if (!measureToggle.isSelected()) return "";
        if (measureX1 == null) return "   测量：选第 1 点";
        if (measureX2 == null) return "   测量：选第 2 点";
        return String.format("   测量：%.2f m", measureDistance());
    }

    private void drawMeasurement(Graphics2D g2) {
        if (measureX1 == null) return;
        int x1 = toScreenX(measureX1);
        int y1 = toScreenY(measureY1);
        g2.setColor(new Color(0, 220, 255, 230));
        g2.setStroke(new BasicStroke(1.8f));
        g2.fillOval(x1 - 4, y1 - 4, 8, 8);
        if (measureX2 == null) {
            g2.setStroke(new BasicStroke(1f));
            return;
        }
        int x2 = toScreenX(measureX2);
        int y2 = toScreenY(measureY2);
        g2.fillOval(x2 - 4, y2 - 4, 8, 8);
        g2.drawLine(x1, y1, x2, y2);
        g2.setStroke(new BasicStroke(1f));

        String label = String.format("%.2f m", measureDistance());
        g2.setFont(FONT_BOLD_12);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(label);
        int th = fm.getHeight();
        int lx = (x1 + x2) / 2 - tw / 2;
        int ly = (y1 + y2) / 2 - th / 2;
        lx = Math.max(4, Math.min(lx, getWidth() - tw - 8));
        ly = Math.max(4, Math.min(ly, getHeight() - th - 4));
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRoundRect(lx - 4, ly, tw + 8, th + 2, 8, 8);
        g2.setColor(new Color(180, 245, 255));
        g2.drawString(label, lx, ly + fm.getAscent());
    }

    private void addBoundaryPoint(int sx, int sy) {
        updateMouse(sx, sy);
        double wx = snapX != null ? snapX : mouseWorldX;
        double wy = snapY != null ? snapY : mouseWorldY;
        if (boundX1 == null) {
            boundX1 = wx;
            boundY1 = wy;
        } else {
            if (boundaryFactory != null) {
                BoundaryCondition b = boundaryFactory.get();
                if (b != null) {
                    b.sx = boundX1;
                    b.sy = boundY1;
                    b.ex = wx;
                    b.ey = wy;
                    if (b.width <= 0) b.width = ParameterList.boundaryWidth;
                    boundX1 = null;
                    boundY1 = null;
                    if (boundaryCreated != null) boundaryCreated.accept(b);
                }
            }
        }
        updateMouse(sx, sy);
    }

    private int hitBoundary(double wx, double wy) {
        return BoundaryOverlay.hit(ParameterList.boundaries, wx, wy, 4.0 / scale);
    }

    private String boundaryHint() {
        if (!boundaryToggle.isSelected()) return "";
        if (boundX1 == null) return "   边界：选第 1 点";
        return "   边界：选第 2 点";
    }

    private void drawBoundaries(Graphics2D g2) {
        BoundaryOverlay.draw(g2, ParameterList.boundaries, selectedBoundary,
                this::toScreenX, this::toScreenY, boundX1, boundY1,
                snapX != null ? snapX : mouseWorldX,
                snapY != null ? snapY : mouseWorldY);
    }

    private int hitExcavation(double wx, double wy) {
        return ExcavationOverlay.hit(ParameterList.excavationLayers, wx, wy);
    }

    private String excavationHint() {
        if (!excavationToggle.isSelected()) return "";
        if (excStartX == null) return "   开挖：按住左键拖动框选";
        return "   开挖：正在框选";
    }

    private void drawExcavations(Graphics2D g2) {
        ExcavationOverlay.draw(g2, ParameterList.excavationLayers, selectedExcavation,
                this::toScreenX, this::toScreenY,
                excStartX, excStartY, excEndX, excEndY);
    }

    private void addBoltPoint(int sx, int sy) {
        updateMouse(sx, sy);
        double wx = snapX != null ? snapX : mouseWorldX;
        double wy = snapY != null ? snapY : mouseWorldY;
        if (boltX1 == null) {
            boltX1 = wx;
            boltY1 = wy;
        } else if (boltFactory != null) {
            BoltGroup g = boltFactory.get();
            if (g != null) {
                g.x1 = boltX1; g.y1 = boltY1;
                g.x2 = wx; g.y2 = wy;
                boltX1 = null; boltY1 = null;
                if (boltCreated != null) boltCreated.accept(g);
            }
        }
        updateMouse(sx, sy);
        repaint();
    }

    private void addPlinePoint(int sx, int sy) {
        updateMouse(sx, sy);
        double wx = snapX != null ? snapX : mouseWorldX;
        double wy = snapY != null ? snapY : mouseWorldY;
        if (plineX1 == null) {
            plineX1 = wx;
            plineY1 = wy;
        } else if (plineFactory != null) {
            Pline p = plineFactory.get();
            if (p != null) {
                p.x1 = plineX1; p.y1 = plineY1;
                p.x2 = wx; p.y2 = wy;
                plineX1 = null; plineY1 = null;
                if (plineCreated != null) plineCreated.accept(p);
            }
        }
        updateMouse(sx, sy);
        repaint();
    }

    private int[] hitPlineHandle(double wx, double wy) {
        double tol = 6.0 / scale;
        for (int i = ParameterList.plines.size() - 1; i >= 0; i--) {
            Pline p = ParameterList.plines.get(i);
            if (Math.abs(wx - p.x1) <= tol && Math.abs(wy - p.y1) <= tol) return new int[]{i, 0};
            if (Math.abs(wx - p.x2) <= tol && Math.abs(wy - p.y2) <= tol) return new int[]{i, 1};
        }
        return null;
    }

    private String boltHint() {
        if (!boltToggle.isSelected()) return "";
        return boltX1 == null ? "   锚杆：选安装线第 1 点" : "   锚杆：选安装线第 2 点";
    }

    private String plineHint() {
        if (!plineToggle.isSelected()) return "";
        return plineX1 == null ? "   测线：选第 1 点" : "   测线：选第 2 点";
    }

    private void drawBolts(Graphics2D g2) {
        if (!hasAction(Stage4Action.Type.BOLT) && !boltToggle.isSelected()) return;
        g2.setFont(FONT_11);
        for (int gi = 0; gi < ParameterList.boltGroups.size(); gi++) {
            BoltGroup group = ParameterList.boltGroups.get(gi);
            boolean groupSelected = gi == selectedBoltGroup;
            g2.setColor(groupSelected ? new Color(255, 150, 50)
                    : new Color(255, 210, 120, 150));
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10f, new float[]{5f, 4f}, 0f));
            g2.drawLine(toScreenX(group.x1), toScreenY(group.y1),
                    toScreenX(group.x2), toScreenY(group.y2));
            for (int bi = 0; bi < group.bolts.size(); bi++) {
                Bolt b = group.bolts.get(bi);
                boolean individual = groupSelected && contains(selectedBoltIndices, bi);
                if (individual) {
                    g2.setColor(Color.YELLOW);
                    g2.setStroke(new BasicStroke(3f));
                } else if (groupSelected) {
                    g2.setColor(new Color(255, 200, 80));
                    g2.setStroke(new BasicStroke(2.2f));
                } else {
                    g2.setColor(new Color(255, 105, 90));
                    g2.setStroke(new BasicStroke(1.6f));
                }
                int sx = toScreenX(b.sx), sy = toScreenY(b.sy);
                g2.drawLine(sx, sy, toScreenX(b.ex()), toScreenY(b.ey()));
                g2.fillOval(sx - 2, sy - 2, 4, 4);
            }
            g2.setStroke(new BasicStroke(1.6f));
            g2.setColor(Color.WHITE);
            g2.drawString(group.name + "  " + group.bolts.size() + "根",
                    toScreenX((group.x1 + group.x2) / 2) + 4,
                    toScreenY((group.y1 + group.y2) / 2) - 4);
        }
        if (boltX1 != null) {
            g2.setColor(new Color(255, 220, 80));
            g2.fillOval(toScreenX(boltX1) - 4, toScreenY(boltY1) - 4, 8, 8);
        }
    }

    private boolean contains(int[] values, int target) {
        for (int value : values) {
            if (value == target) return true;
        }
        return false;
    }

    private boolean hasAction(Stage4Action.Type type) {
        for (Stage4Action action : ParameterList.stage4Actions) {
            if (action.type == type) return true;
        }
        return false;
    }

    private void drawPlines(Graphics2D g2) {
        if (!hasAction(Stage4Action.Type.PLINE) && !plineToggle.isSelected()) return;
        g2.setFont(FONT_11);
        for (int i = 0; i < ParameterList.plines.size(); i++) {
            Pline p = ParameterList.plines.get(i);
            int x1 = toScreenX(p.x1), y1 = toScreenY(p.y1);
            int x2 = toScreenX(p.x2), y2 = toScreenY(p.y2);
            g2.setColor(i == selectedPline
                    ? new Color(255, 150, 60) : new Color(80, 220, 255));
            g2.setStroke(new BasicStroke(i == selectedPline ? 2.2f : 1.6f));
            g2.drawLine(x1, y1, x2, y2);
            g2.fillRect(x1 - 4, y1 - 4, 8, 8);
            g2.fillRect(x2 - 4, y2 - 4, 8, 8);
            g2.drawString("#" + (i + 1), (x1 + x2) / 2, (y1 + y2) / 2 - 4);
        }
        if (plineX1 != null) {
            g2.setColor(new Color(255, 220, 80));
            g2.fillOval(toScreenX(plineX1) - 4, toScreenY(plineY1) - 4, 8, 8);
        }
    }

    private double[] heights() {
        if (ParameterList.heightList == null
                || ParameterList.heightList.length != ParameterList.layerCount() + 1) {
            ParameterList.syncFromLayers();
        }
        return ParameterList.heightList;
    }

    private void ensureJointCache() {
        long signature = jointSignature();
        if (jointCache != null && signature == jointSignature) return;
        jointCache = JointGeometry.build(scale);
        jointSignature = signature;
    }

    private long jointSignature() {
        long h = 1125899906842597L;
        h = h * 31 + Double.doubleToLongBits(ParameterList.modelLength);
        h = h * 31 + ParameterList.layers.size();
        for (LayerData layer : ParameterList.layers) {
            h = h * 31 + Double.doubleToLongBits(layer.thickness);
            h = h * 31 + layer.joints.size();
            for (JointSet js : layer.joints) {
                h = h * 31 + Double.doubleToLongBits(js.angle);
                h = h * 31 + Double.doubleToLongBits(js.angleStdDev);
                h = h * 31 + Double.doubleToLongBits(js.bridgeLength);
                h = h * 31 + Double.doubleToLongBits(js.bridgeStdDev);
                h = h * 31 + Double.doubleToLongBits(js.traceLength);
                h = h * 31 + Double.doubleToLongBits(js.traceStdDev);
                h = h * 31 + Double.doubleToLongBits(js.spacing);
                h = h * 31 + Double.doubleToLongBits(js.spacingStdDev);
                h = h * 31 + Double.doubleToLongBits(js.startX);
                h = h * 31 + Double.doubleToLongBits(js.startY);
                h = h * 31 + (js.baseGroup ? 1 : 0);
                h = h * 31 + (js.linkSpacing ? 1 : 0);
            }
        }
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            h = h * 31 + e.layerIndex;
            h = h * 31 + Double.doubleToLongBits(e.cutWidth);
            h = h * 31 + Double.doubleToLongBits(e.stepDist);
            h = h * 31 + e.steps;
            h = h * 31 + e.direction.ordinal();
            h = h * 31 + Double.doubleToLongBits(e.x1);
            h = h * 31 + Double.doubleToLongBits(e.y1);
            h = h * 31 + Double.doubleToLongBits(e.x2);
            h = h * 31 + Double.doubleToLongBits(e.y2);
            h = h * 31 + (e.autoGeometry ? 1 : 0);
        }
        h = h * 31 + (ParameterList.debugMode ? 1 : 0);
        h = h * 31 + Double.doubleToLongBits(ParameterList.debugScale);
        h = h * 31 + Math.round(scale * 100);
        return h;
    }

    private void drawJoints(Graphics2D g2, int layerIndex) {
        if (jointCache == null || layerIndex < 0 || layerIndex >= jointCache.size()) return;
        List<double[]> lines = jointCache.get(layerIndex);
        if (lines.isEmpty()) return;
        g2.setColor(new Color(250, 250, 250, 80));
        g2.setStroke(new BasicStroke(0.65f));
        int w = getWidth();
        int h = getHeight();
        for (double[] line : lines) {
            int x1 = toScreenX(line[0]);
            int y1 = toScreenY(line[1]);
            int x2 = toScreenX(line[2]);
            int y2 = toScreenY(line[3]);
            if ((x1 < -8 && x2 < -8) || (x1 > w + 8 && x2 > w + 8)) continue;
            if ((y1 < -8 && y2 < -8) || (y1 > h + 8 && y2 > h + 8)) continue;
            g2.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawGrid(Graphics2D g2) {
        double step = niceStep(60.0 / scale);
        g2.setFont(FONT_GRID);
        g2.setStroke(new BasicStroke(0.6f));

        for (double x = 0; x <= ParameterList.modelLength + 1e-9; x += step) {
            int sx = toScreenX(x);
            g2.setColor(GRID);
            g2.drawLine(sx, 0, sx, getHeight());
            g2.setColor(AXIS);
            g2.drawString(format(x), sx + 2, getHeight() - 4);
        }
        double maxH = ParameterList.heights().get(0);
        for (double y = 0; y <= maxH + 1e-9; y += step) {
            int sy = toScreenY(y);
            g2.setColor(GRID);
            g2.drawLine(0, sy, getWidth(), sy);
            g2.setColor(AXIS);
            g2.drawString(format(y), 4, sy - 2);
        }
    }

    private void drawSnapLine(Graphics2D g2, int x1, int y1, int x2, int y2) {
        g2.setColor(new Color(255, 205, 0, 210));
        g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10f, new float[]{8f, 5f}, 0f));
        g2.drawLine(x1, y1, x2, y2);
        g2.setStroke(new BasicStroke(1f));
    }

    private double niceStep(double raw) {
        double p = Math.pow(10, Math.floor(Math.log10(Math.max(raw, 1e-6))));
        double v = raw / p;
        if (v <= 1) return p;
        if (v <= 2) return 2 * p;
        if (v <= 5) return 5 * p;
        return 10 * p;
    }

    private String format(double v) {
        if (scale > 4) return String.format("%.2f", v);
        if (scale > 1.2) return String.format("%.1f", v);
        return String.format("%.0f", v);
    }

    private void drawCracks(Graphics2D g2, double[] hs) {
        g2.setColor(new Color(255, 150, 40, 220));
        g2.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10f, new float[]{5f, 4f}, 0f));
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            drawCrackLayer(g2, e, hs);
        }
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawCrackLayer(Graphics2D g2, ExcavationLayer e, double[] hs) {
        double bottom = Util.excavationBottom(e, 0);
        double height = Util.excavationHeight(e, 0);
        if (height <= 1e-9) return;
        int y1 = toScreenY(bottom);
        int y2 = toScreenY(bottom + height);
        List<Double> cracks = Util.calculateExcavationXList(e);
        double xMin = Double.MAX_VALUE;
        double xMax = -Double.MAX_VALUE;
        for (double x : cracks) {
            xMin = Math.min(xMin, x);
            xMax = Math.max(xMax, x);
        }
        if (!isBoundaryY(bottom, hs)) {
            g2.drawLine(toScreenX(xMin), y1, toScreenX(xMax), y1);
        }
        if (!isBoundaryY(bottom + height, hs)) {
            g2.drawLine(toScreenX(xMin), y2, toScreenX(xMax), y2);
        }
        for (double x : cracks) {
            int sx = toScreenX(x);
            g2.drawLine(sx, y1, sx, y2);
        }
    }

    private boolean isBoundaryY(double y, double[] hs) {
        for (double h : hs) {
            if (Math.abs(y - h) < 1e-6) return true;
        }
        return false;
    }


}
