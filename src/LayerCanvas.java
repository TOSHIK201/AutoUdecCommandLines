import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

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

    private static final Color BG = new Color(42, 47, 56);
    private static final Color GRID = new Color(68, 74, 84);
    private static final Color AXIS = new Color(190, 200, 210);
    private static final Color SELECT = new Color(255, 70, 70);
    private final java.util.List<java.util.function.IntConsumer> selectionListeners = new java.util.ArrayList<>();

    public LayerCanvas(PropertyPanel propertyPanel) {
        this.propertyPanel = propertyPanel;
        setBackground(BG);
        setFocusable(true);

        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                updateMouse(e.getX(), e.getY());
                lastX = e.getX(); lastY = e.getY();
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (Math.abs(e.getX() - lastX) < 4 && Math.abs(e.getY() - lastY) < 4) {
                    selectLayerAt(toWorldY(e.getY()));
                }
            }
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) fitToScreen();
            }
        };
        addMouseListener(mouse);

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
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
        mouseWorldX = toWorldX(sx);
        mouseWorldY = toWorldY(sy);
        snapX = null;
        snapY = null;
        double tol = 12.0 / scale;
        for (double h : ParameterList.heights()) {
            if (Math.abs(mouseWorldY - h) <= tol) { snapY = h; break; }
        }
        List<Double> xTargets = new java.util.ArrayList<>();
        xTargets.add(0.0);
        xTargets.add(ParameterList.modelLength);
        xTargets.addAll(Util.calculateExcavationXList(ParameterList.step, ParameterList.stepDist,
                ParameterList.cutWidth, ParameterList.direction));
        for (double x : xTargets) {
            if (Math.abs(mouseWorldX - x) <= tol) { snapX = x; break; }
        }
        if (statusLabel != null) {
            double sxShow = snapX != null ? snapX : mouseWorldX;
            double syShow = snapY != null ? snapY : mouseWorldY;
            statusLabel.setText(String.format("x = %.2f m%s   y = %.2f m%s   当前第 %d 层",
                    sxShow, snapX != null ? " [吸附]" : "",
                    syShow, snapY != null ? " [吸附]" : "",
                    selectedIndex + 1));
        }
        repaint();
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
            drawJoints(g2, layer, top, bottom);
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
        drawGrid(g2);
        if (snapY != null) drawSnapLine(g2, 0, toScreenY(snapY), getWidth(), toScreenY(snapY));
        if (snapX != null) drawSnapLine(g2, toScreenX(snapX), 0, toScreenX(snapX), getHeight());
        if (snapX != null && snapY != null) {
            int cx = toScreenX(snapX);
            int cy = toScreenY(snapY);
            g2.setColor(new Color(255, 205, 0));
            g2.fillOval(cx - 4, cy - 4, 8, 8);
        }
    }

    private void drawJoints(Graphics2D g2, LayerData layer, double top, double bottom) {
        double thickness = top - bottom;
        if (thickness <= 0) return;
        g2.setColor(new Color(250, 250, 250, 80));
        g2.setStroke(new BasicStroke(0.65f));

        for (JointSet js : layer.joints) {
            double target = Math.max(0.05, js.spacing);
            if (ParameterList.debugMode) target *= Math.max(1.0, ParameterList.debugScale);
            int blocksY = (int) Math.max(1, Math.round(thickness / target));
            double dy = thickness / blocksY;
            int blocksX = (int) Math.max(1, Math.round(ParameterList.modelLength / dy));
            double dx = ParameterList.modelLength / blocksX;

            if (Math.abs(js.angle) < 1e-6) {
                for (double y = bottom; y <= top + 1e-9; y += dy) {
                    int sy = toScreenY(y);
                    g2.drawLine(toScreenX(0), sy, toScreenX(ParameterList.modelLength), sy);
                }
            } else if (Math.abs(js.angle - 90.0) < 1e-6) {
                for (double x = 0; x <= ParameterList.modelLength + 1e-9; x += dx) {
                    int sx = toScreenX(x);
                    g2.drawLine(sx, toScreenY(top), sx, toScreenY(bottom));
                }
            } else {
                double rad = Math.toRadians(js.angle);
                double ux = Math.cos(rad), uy = Math.sin(rad);
                double px = -uy, py = ux;
                double span = ParameterList.modelLength + thickness * 2;
                for (double d = -span; d <= span; d += target) {
                    double cx = js.startX + px * d;
                    double cy = js.startY + py * d;
                    g2.drawLine(toScreenX(cx - ux * span), toScreenY(cy - uy * span),
                            toScreenX(cx + ux * span), toScreenY(cy + uy * span));
                }
            }
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
        int layerIndex = e.layerIndex;
        if (layerIndex < 0 || layerIndex + 1 >= hs.size()) return;
        double bottom = (e.centered || Double.isNaN(e.originY))
                ? hs.get(layerIndex + 1) : e.originY;
        double height = e.overallHeight > 0 ? e.overallHeight : ParameterList.layers.get(layerIndex).thickness;
        int y1 = toScreenY(bottom);
        int y2 = toScreenY(bottom + height);
        List<Double> cracks = Util.calculateExcavationXList(e);
        for (double x : cracks) {
            int sx = toScreenX(x);
            g2.drawLine(sx, y1, sx, y2);
        }
    }


}
