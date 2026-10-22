import java.awt.*;
import java.util.List;
import java.util.function.DoubleToIntFunction;

class BoundaryOverlay {
    static int hit(List<BoundaryCondition> list, double wx, double wy, double tol) {
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).hit(wx, wy, tol)) return i;
        }
        return -1;
    }

    static void draw(Graphics2D g2, List<BoundaryCondition> list, int selected,
                     DoubleToIntFunction toX, DoubleToIntFunction toY,
                     Double pendingX, Double pendingY, double mouseX, double mouseY) {
        g2.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        for (int i = 0; i < list.size(); i++) {
            BoundaryCondition b = list.get(i);
            double[] r = b.range();
            int x1 = toX.applyAsInt(r[0]);
            int x2 = toX.applyAsInt(r[1]);
            int yTop = toY.applyAsInt(r[3]);
            int yBottom = toY.applyAsInt(r[2]);
            int w = Math.max(1, x2 - x1);
            int h = Math.max(1, yBottom - yTop);
            boolean sel = i == selected;
            Color fill = b.verticalLine() ? new Color(70, 160, 255, 55)
                    : new Color(70, 220, 140, 55);
            if (b.type.displacement()) fill = new Color(255, 200, 60, 60);
            if (b.type.stress()) fill = new Color(255, 140, 60, 60);
            if (sel) fill = new Color(255, 70, 70, 85);
            g2.setColor(fill);
            g2.fillRect(x1, yTop, w, h);
            g2.setColor(sel ? new Color(255, 120, 120) : new Color(160, 205, 255, 170));
            g2.drawRect(x1, yTop, w, h);

            g2.setColor(new Color(225, 240, 255, 210));
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10f, new float[]{6f, 4f}, 0f));
            g2.drawLine(toX.applyAsInt(b.sx), toY.applyAsInt(b.sy),
                    toX.applyAsInt(b.ex), toY.applyAsInt(b.ey));
            g2.setStroke(new BasicStroke(1f));

            String label = (i + 1) + " " + b.type.label;
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(label);
            int th = fm.getHeight();
            int lx = (x1 + x2) / 2 - tw / 2;
            int ly = (yTop + yBottom) / 2 - th / 2;
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRoundRect(lx - 4, ly, tw + 8, th, 8, 8);
            g2.setColor(Color.WHITE);
            g2.drawString(label, lx, ly + fm.getAscent());
        }

        if (pendingX != null) {
            int px = toX.applyAsInt(pendingX);
            int py = toY.applyAsInt(pendingY);
            g2.setColor(new Color(255, 220, 80));
            g2.fillOval(px - 4, py - 4, 8, 8);
            int cx = toX.applyAsInt(mouseX);
            int cy = toY.applyAsInt(mouseY);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10f, new float[]{5f, 4f}, 0f));
            g2.drawLine(px, py, cx, cy);
            g2.setStroke(new BasicStroke(1f));
        }
    }
}
