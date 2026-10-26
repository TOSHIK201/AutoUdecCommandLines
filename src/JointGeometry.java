import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 节理预览的世界坐标线段缓存：视图平移/缩放时不再重复做间距搜索。
class JointGeometry {
    private static final double MIN_PIXELS = 3.0;

    static List<List<double[]>> build(double scale) {
        List<List<double[]>> byLayer = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        double[] hs = ParameterList.heightList;
        double modelLength = ParameterList.modelLength;
        for (int i = 0; i < ParameterList.layerCount(); i++) {
            LayerData layer = ParameterList.layers.get(i);
            double top = hs[i];
            double bottom = hs[i + 1];
            double thickness = top - bottom;
            List<double[]> lines = new ArrayList<>();
            if (thickness > 0) {
                double anchorY = Util.excavationStartY(i);
                for (JointSet js : layer.joints) {
                    boolean vertical = Math.abs(js.angle - 90.0) < 1e-6;
                    double width = Util.layerWidthSpacing(i, js.spacing);
                    double height = Util.layerHeightSpacing(i, js.spacing);
                    double stepX = width * Math.max(1.0,
                            Math.ceil(MIN_PIXELS / Math.max(1e-9, scale * width)));
                    double stepY = height * Math.max(1.0,
                            Math.ceil(MIN_PIXELS / Math.max(1e-9, scale * height)));

                    if (Math.abs(js.angle) < 1e-6) {
                        addHorizontal(lines, seen, bottom, top, stepY, modelLength,
                                js.baseGroup ? anchorY : bottom);
                    } else if (vertical) {
                        if (js.baseGroup) {
                            double rowHeight = height;
                            double rowStep = rowHeight * Math.max(1.0,
                                    Math.ceil(MIN_PIXELS / Math.max(1e-9, scale * rowHeight)));
                            double phase = Util.excavationPhase(i, width);
                            if (rowStep >= thickness) {
                                addVertical(lines, seen, phase, modelLength, stepX, top, bottom);
                            } else {
                                double startA = firstAtOrBelow(bottom, anchorY, rowStep * 2.0);
                                addBrickRows(lines, seen, phase, startA, top, rowStep, stepX, modelLength);
                                addBrickRows(lines, seen, phase + stepX / 2.0, startA + rowStep,
                                        top, rowStep, stepX, modelLength);
                            }
                        } else {
                            addVertical(lines, seen, 0, modelLength, stepX, top, bottom);
                        }
                    } else {
                        addAngled(lines, seen, js, modelLength, thickness, stepY);
                    }
                }
            }
            byLayer.add(lines);
        }
        return byLayer;
    }

    private static void addHorizontal(List<double[]> lines, Set<String> seen,
                                      double bottom, double top, double spacing,
                                      double modelLength, double anchorY) {
        if (spacing <= 1e-9) return;
        double start = anchorY + Math.ceil((bottom - anchorY) / spacing) * spacing;
        for (double y = start; y <= top + 1e-9; y += spacing) {
            add(lines, seen, 0, y, modelLength, y);
        }
    }

    private static double firstAtOrBelow(double bottom, double anchor, double period) {
        if (period <= 1e-9) return bottom;
        return anchor + Math.floor((bottom - anchor) / period) * period;
    }

    private static void addBrickRows(List<double[]> lines, Set<String> seen, double startX,
                                     double fromY, double top, double rowHeight,
                                     double width, double modelLength) {
        if (width <= 1e-9 || rowHeight <= 1e-9) return;
        for (double y = fromY; y < top - 1e-9; y += rowHeight * 2.0) {
            double yEnd = Math.min(y + rowHeight, top);
            for (double x = startX; x <= modelLength + 1e-9; x += width) {
                add(lines, seen, x, y, x, yEnd);
            }
        }
    }

    private static void addVertical(List<double[]> lines, Set<String> seen, double fromX,
                                    double toX, double spacing, double top, double bottom) {
        if (spacing <= 1e-9) return;
        for (double x = fromX; x <= toX + 1e-9; x += spacing) {
            add(lines, seen, x, top, x, bottom);
        }
    }

    private static void addAngled(List<double[]> lines, Set<String> seen, JointSet js,
                                  double modelLength, double thickness, double spacing) {
        double rad = Math.toRadians(js.angle);
        double ux = Math.cos(rad);
        double uy = Math.sin(rad);
        double px = -uy;
        double py = ux;
        double span = modelLength + thickness * 2.0;
        if (spacing <= 1e-9) return;
        for (double d = -span; d <= span; d += spacing) {
            double cx = js.startX + px * d;
            double cy = js.startY + py * d;
            add(lines, seen, cx - ux * span, cy - uy * span,
                    cx + ux * span, cy + uy * span);
        }
    }

    private static void add(List<double[]> lines, Set<String> seen,
                            double x1, double y1, double x2, double y2) {
        if (x1 > x2 || (Math.abs(x1 - x2) < 1e-9 && y1 > y2)) {
            double tx = x1, ty = y1;
            x1 = x2; y1 = y2;
            x2 = tx; y2 = ty;
        }
        String key = Math.round(x1 * 1e4) + "," + Math.round(y1 * 1e4) + ","
                + Math.round(x2 * 1e4) + "," + Math.round(y2 * 1e4);
        if (seen.add(key)) lines.add(new double[]{x1, y1, x2, y2});
    }
}
