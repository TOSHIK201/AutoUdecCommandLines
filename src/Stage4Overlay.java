import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.function.DoubleToIntFunction;

// 阶段4 选中开挖动作时，在画布上标出该分组的范围与标签
class Stage4Overlay {
    private static final Font FONT = new Font("微软雅黑", Font.BOLD, 12);

    static void draw(Graphics2D g2, String group,
                     DoubleToIntFunction toX, DoubleToIntFunction toY,
                     int canvasWidth) {
        double[] r = Stage4Geometry.range(group);
        if (r == null) return;
        int x1 = toX.applyAsInt(r[0]);
        int x2 = toX.applyAsInt(r[1]);
        int yTop = toY.applyAsInt(r[3]);
        int yBottom = toY.applyAsInt(r[2]);
        int w = Math.max(1, x2 - x1);
        int h = Math.max(1, yBottom - yTop);

        g2.setColor(new Color(255, 120, 40, 70));
        g2.fillRect(x1, yTop, w, h);
        g2.setColor(new Color(255, 170, 60));
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10f, new float[]{7f, 4f}, 0f));
        g2.drawRect(x1, yTop, w, h);
        g2.setStroke(new BasicStroke(1f));

        String label = Stage4Geometry.label(group);
        g2.setFont(FONT);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(label);
        int th = fm.getHeight();
        int lx = x1 + (w - tw) / 2;
        int ly = yTop - th - 2;
        if (ly < 2) ly = yTop + 2;
        lx = Math.max(4, Math.min(lx, Math.max(4, canvasWidth - tw - 8)));
        g2.setColor(new Color(0, 0, 0, 185));
        g2.fillRoundRect(lx - 5, ly, tw + 10, th + 2, 8, 8);
        g2.setColor(new Color(255, 220, 150));
        g2.drawString(label, lx, ly + fm.getAscent());
    }
}
