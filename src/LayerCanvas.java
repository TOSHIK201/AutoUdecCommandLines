import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private boolean dragging;

    private static final Color BG = new Color(42, 47, 56);
    private static final Color GRID = new Color(68, 74, 84);
    private static final Color AXIS = new Color(190, 200, 210);
    private static final Color SELECT = new Color(255, 70, 70);
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
                boundX1 = null;
                boundY1 = null;
                excStartX = null;
                excStartY = null;
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
                clearMeasure();
                boundX1 = null;
                boundY1 = null;
            } else {
                excStartX = null;
                excStartY = null;
            }
            notifyExcavationMode(excavationToggle.isSelected());
            repaint();
        });
        add(excavationToggle);

        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                updateMouse(e.getX(), e.getY());
                lastX = e.getX(); lastY = e.getY();
                dragging = false;
                double wx = snapX != null ? snapX : mouseWorldX;
                double wy = snapY != null ? snapY : mouseWorldY;
                int[] hit = ExcavationOverlay.handleHit(
                        ParameterList.excavationLayers, selectedExcavation,
                        wx, wy, 6.0 / scale);
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
                    if (boundaryToggle.isSelected()) {
                        addBoundaryPoint(e.getX(), e.getY());
                        return;
                    }
                    if (measureToggle.isSelected()) {
                        addMeasurePoint(e.getX(), e.getY());
                        return;
                    }
                    int hit = hitBoundary(toWorldX(e.getX()), toWorldY(e.getY()));
                    if (hit >= 0) {
                        setSelectedBoundary(hit);
                        if (boundarySelectionListener != null) boundarySelectionListener.accept(hit);
                        return;
                    }
                    selectLayerAt(toWorldY(e.getY()));
                }
            }
            @Override public void mouseClicked(MouseEvent e) {
                if (!measureToggle.isSelected() && !boundaryToggle.isSelected()
                        && !excavationToggle.isSelected()
                        && e.getClickCount() == 2) fitToScreen();
            }
        };
        addMouseListener(mouse);

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (resizeIndex >= 0 && resizeIndex < ParameterList.excavationLayers.size()) {
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
                    measureHint() + boundaryHint() + excavationHint()));
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

        List<Double> hs = ParameterList.heights();
        int left = toScreenX(0);
        int right = toScreenX(ParameterList.modelLength);
        Set<String> drawnJointLines = new HashSet<>();

        for (int i = 0; i < ParameterList.layerCount(); i++) {
            LayerData layer = ParameterList.layers.get(i);
            double top = hs.get(i);
            double bottom = hs.get(i + 1);
            int yTop = toScreenY(top);
            int yBottom = toScreenY(bottom);

            g2.setColor(layer.color);
            g2.fillRect(left, yTop, right - left, Math.max(1, yBottom - yTop));

            Shape oldClip = g2.getClip();
            g2.clipRect(left, yTop, right - left, Math.max(1, yBottom - yTop));
            drawJoints(g2, layer, i, top, bottom, drawnJointLines);
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
                g2.setFont(new Font("微软雅黑", Font.PLAIN, 11));
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
        g2.setFont(new Font("微软雅黑", Font.BOLD, 12));
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

    private void drawJoints(Graphics2D g2, LayerData layer, int layerIndex, double top, double bottom,
                            Set<String> drawn) {
        double thickness = top - bottom;
        if (thickness <= 0) return;
        g2.setColor(new Color(250, 250, 250, 80));
        g2.setStroke(new BasicStroke(0.65f));

        for (JointSet js : layer.joints) {
            boolean vertical = Math.abs(js.angle - 90.0) < 1e-6;
            double width = Util.layerWidthSpacing(layerIndex, js.spacing);
            double height = Util.layerHeightSpacing(layerIndex, js.spacing);
            double spacing = vertical ? width : height;

            if (Math.abs(js.angle) < 1e-6) {
                double drawSpacing = js.baseGroup
                        ? thickness / Math.max(1, Math.round(thickness / height + 1e-9))
                        : spacing;
                for (double y = bottom; y <= top + 1e-9; y += drawSpacing) {
                    int sy = toScreenY(y);
                    drawJointLine(g2, drawn, toScreenX(0), sy, toScreenX(ParameterList.modelLength), sy);
                }
            } else if (Math.abs(js.angle - 90.0) < 1e-6) {
                if (js.baseGroup) {
                    double rowHeight = thickness / Math.max(1, Math.round(thickness / height + 1e-9));
                    double phase = Util.excavationPhase(layerIndex, width);
                    for (double y = bottom; y < top - 1e-9; y += rowHeight * 2.0) {
                        drawVerticalSegment(g2, drawn, phase, y,
                                Math.min(y + rowHeight, top), width);
                    }
                    for (double y = bottom + rowHeight; y < top - 1e-9; y += rowHeight * 2.0) {
                        drawVerticalSegment(g2, drawn, phase + width / 2.0, y,
                                Math.min(y + rowHeight, top), width);
                    }
                } else {
                    for (double x = 0; x <= ParameterList.modelLength + 1e-9; x += spacing) {
                        int sx = toScreenX(x);
                        drawJointLine(g2, drawn, sx, toScreenY(top), sx, toScreenY(bottom));
                    }
                }
            } else {
                double rad = Math.toRadians(js.angle);
                double ux = Math.cos(rad), uy = Math.sin(rad);
                double px = -uy, py = ux;
                double span = ParameterList.modelLength + thickness * 2;
                for (double d = -span; d <= span; d += spacing) {
                    double cx = js.startX + px * d;
                    double cy = js.startY + py * d;
                    drawJointLine(g2, drawn, toScreenX(cx - ux * span), toScreenY(cy - uy * span),
                            toScreenX(cx + ux * span), toScreenY(cy + uy * span));
                }
            }
        }
    }

    private void drawVerticalSegment(Graphics2D g2, Set<String> drawn, double startX,
                                     double y0, double y1, double spacing) {
        for (double x = startX; x <= ParameterList.modelLength + 1e-9; x += spacing) {
            int sx = toScreenX(x);
            drawJointLine(g2, drawn, sx, toScreenY(y1), sx, toScreenY(y0));
        }
    }

    private void drawJointLine(Graphics2D g2, Set<String> drawn, int x1, int y1, int x2, int y2) {
        int ax = x1, ay = y1, bx = x2, by = y2;
        if (ax > bx || (ax == bx && ay > by)) {
            ax = x2; ay = y2; bx = x1; by = y1;
        }
        String key = ax + "," + ay + "," + bx + "," + by;
        if (drawn.add(key)) {
            g2.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawGrid(Graphics2D g2) {
        double step = niceStep(60.0 / scale);
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
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

    private void drawCracks(Graphics2D g2, List<Double> hs) {
        g2.setColor(new Color(255, 150, 40, 220));
        g2.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10f, new float[]{5f, 4f}, 0f));
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            drawCrackLayer(g2, e, hs);
        }
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawCrackLayer(Graphics2D g2, ExcavationLayer e, List<Double> hs) {
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

    private boolean isBoundaryY(double y, List<Double> hs) {
        for (double h : hs) {
            if (Math.abs(y - h) < 1e-6) return true;
        }
        return false;
    }


}
