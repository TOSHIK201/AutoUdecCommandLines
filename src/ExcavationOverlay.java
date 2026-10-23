import java.awt.*;
import java.util.List;
import java.util.function.DoubleToIntFunction;

class ExcavationOverlay {
    private static final Font FONT = new Font("微软雅黑", Font.PLAIN, 11);

    static int hit(List<ExcavationLayer> list, double wx, double wy) {
        for (int i = list.size() - 1; i >= 0; i--) {
            ExcavationLayer e = list.get(i);
            if (wx >= Math.min(e.x1, e.x2) && wx <= Math.max(e.x1, e.x2)
                    && wy >= Math.min(e.y1, e.y2) && wy <= Math.max(e.y1, e.y2)) {
                return i;
            }
        }
        return -1;
    }

    static void draw(Graphics2D g2, List<ExcavationLayer> list, int selected,
                     DoubleToIntFunction toX, DoubleToIntFunction toY,
                     Double startX, Double startY, double endX, double endY) {
        g2.setFont(FONT);
        for (int i = 0; i < list.size(); i++) {
            ExcavationLayer e = list.get(i);
            drawRect(g2, toX, toY, e.x1, e.y1, e.x2, e.y2,
                    i == selected, e.name + "  " + e.stepCount() + " 步");
        }
        if (startX != null) {
            drawRect(g2, toX, toY, startX, startY, endX, endY, true, "新开挖区域");
        }
    }

    // 返回 {区域下标, 角点序号}；角点 0=左下 1=右下 2=右上 3=左上
    static int[] handleHit(List<ExcavationLayer> list, int selected,
                           double wx, double wy, double tol) {
        if (selected >= 0 && selected < list.size()) {
            int corner = cornerHit(list.get(selected), wx, wy, tol);
            if (corner >= 0) return new int[]{selected, corner};
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            if (i == selected) continue;
            int corner = cornerHit(list.get(i), wx, wy, tol);
            if (corner >= 0) return new int[]{i, corner};
        }
        return null;
    }

    private static int cornerHit(ExcavationLayer e, double wx, double wy, double tol) {
        double x1 = Math.min(e.x1, e.x2), x2 = Math.max(e.x1, e.x2);
        double y1 = Math.min(e.y1, e.y2), y2 = Math.max(e.y1, e.y2);
        double[][] corners = {{x1, y1}, {x2, y1}, {x2, y2}, {x1, y2}};
        for (int i = 0; i < corners.length; i++) {
            if (Math.abs(wx - corners[i][0]) <= tol && Math.abs(wy - corners[i][1]) <= tol) {
                return i;
            }
        }
        return -1;
    }

    private static void drawRect(Graphics2D g2, DoubleToIntFunction toX,
                                 DoubleToIntFunction toY, double x1, double y1,
                                 double x2, double y2, boolean selected, String label) {
        int sx = toX.applyAsInt(Math.min(x1, x2));
        int ex = toX.applyAsInt(Math.max(x1, x2));
        int sy = toY.applyAsInt(Math.max(y1, y2));
        int ey = toY.applyAsInt(Math.min(y1, y2));
        int w = Math.max(1, ex - sx);
        int h = Math.max(1, ey - sy);
        g2.setColor(selected ? new Color(255, 120, 40, 80) : new Color(255, 190, 60, 45));
        g2.fillRect(sx, sy, w, h);
        g2.setColor(selected ? new Color(255, 160, 60) : new Color(255, 210, 110, 190));
        g2.drawRect(sx, sy, w, h);
        drawHandles(g2, toX, toY, Math.min(x1, x2), Math.min(y1, y2),
                Math.max(x1, x2), Math.max(y1, y2), selected);

        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(label);
        int th = fm.getHeight();
        int lx = (sx + ex) / 2 - tw / 2;
        int ly = (sy + ey) / 2 - th / 2;
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(lx - 4, ly, tw + 8, th, 8, 8);
        g2.setColor(Color.WHITE);
        g2.drawString(label, lx, ly + fm.getAscent());
    }

    private static void drawHandles(Graphics2D g2, DoubleToIntFunction toX,
                                    DoubleToIntFunction toY, double x1, double y1,
                                    double x2, double y2, boolean selected) {
        int size = selected ? 8 : 5;
        int[][] points = {
                {toX.applyAsInt(x1), toY.applyAsInt(y1)},
                {toX.applyAsInt(x2), toY.applyAsInt(y1)},
                {toX.applyAsInt(x2), toY.applyAsInt(y2)},
                {toX.applyAsInt(x1), toY.applyAsInt(y2)}
        };
        for (int[] p : points) {
            g2.setColor(selected ? new Color(255, 150, 50) : new Color(255, 230, 170));
            g2.fillRect(p[0] - size / 2, p[1] - size / 2, size, size);
            g2.setColor(new Color(60, 60, 60, 200));
            g2.drawRect(p[0] - size / 2, p[1] - size / 2, size, size);
        }
    }
}
