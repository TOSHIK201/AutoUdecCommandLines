import java.util.ArrayList;
import java.util.List;

public class Util {

    public enum Direction {
        LEFT_TO_RIGHT,  // 从左向右
        RIGHT_TO_LEFT   // 从右向左
    }

    /**
     * 计算居中开挖的各边界 X 坐标
     * @param steps     开挖步数
     * @param stepDist  每次开挖的距离
     * @param cutWidth  开切眼宽度
     * @param direction 开挖方向
     * @return 坐标集合，例如 [47.0, 55.0, 67.0, 79.0, 91.0, 103.0]
     */
    public static List<Double> calculateExcavationXList(int steps, double stepDist, double cutWidth, Direction direction) {
        return calculateExcavationXList(steps, stepDist, cutWidth, direction,
                ParameterList.excavationCentered, ParameterList.excavationOriginX);
    }

    public static List<Double> calculateExcavationXList(int steps, double stepDist, double cutWidth,
                                                        Direction direction, boolean centered, double originX) {
        double totalWidth = cutWidth + steps * stepDist;
        double left;
        if (centered) {
            left = (ParameterList.modelLength - totalWidth) / 2.0;
        } else if (Double.isNaN(originX)) {
            left = direction == Direction.LEFT_TO_RIGHT ? 0.0 : ParameterList.modelLength - totalWidth;
        } else {
            left = originX;
        }
        List<Double> xList = new ArrayList<>();

        if (direction == Direction.LEFT_TO_RIGHT) {
            xList.add(left);
            double cursor = left + cutWidth;
            xList.add(cursor);
            for (int i = 0; i < steps; i++) {
                cursor += stepDist;
                xList.add(cursor);
            }
        } else {
            double right = left + totalWidth;
            xList.add(right);
            double cursor = right - cutWidth;
            xList.add(cursor);
            for (int i = 0; i < steps; i++) {
                cursor -= stepDist;
                xList.add(cursor);
            }
        }
        return xList;
    }

    public static List<Double> calculateExcavationXList(ExcavationLayer e) {
        List<Double> xs = new ArrayList<>();
        double left = Math.min(e.x1, e.x2);
        double right = Math.max(e.x1, e.x2);
        double width = right - left;
        if (width <= 1e-9) return xs;
        double cut = Math.min(Math.max(e.cutWidth, 0), width);
        double step = e.stepDist > 1e-9 ? e.stepDist : 0;

        if (e.direction == Direction.LEFT_TO_RIGHT) {
            double cursor = left;
            addIfNew(xs, cursor);
            cursor = Math.min(cursor + cut, right);
            addIfNew(xs, cursor);
            while (step > 0 && cursor + step < right - 1e-9) {
                cursor += step;
                addIfNew(xs, cursor);
            }
            addIfNew(xs, right);
        } else {
            double cursor = right;
            addIfNew(xs, cursor);
            cursor = Math.max(cursor - cut, left);
            addIfNew(xs, cursor);
            while (step > 0 && cursor - step > left + 1e-9) {
                cursor -= step;
                addIfNew(xs, cursor);
            }
            addIfNew(xs, left);
        }
        return xs;
    }

    public static double excavationBottom(ExcavationLayer e, int layerIndex) {
        return Math.min(e.y1, e.y2);
    }

    public static double excavationHeight(ExcavationLayer e, int layerIndex) {
        return Math.abs(e.y2 - e.y1);
    }

    private static void addIfNew(List<Double> xs, double value) {
        if (xs.isEmpty() || Math.abs(xs.get(xs.size() - 1) - value) > 1e-9) {
            xs.add(value);
        }
    }

    public static String generateVerticalCracks(List<Double> xList, double yBottom, double yTop) {
        StringBuilder sb = new StringBuilder();
        for (Double x : xList) {
            sb.append(String.format("crack %.2f %.2f %.2f %.2f\n", x, yBottom, x, yTop));
        }
        return sb.toString();
    }

    public static String generateHorizontalCracks(double[] heightList, double modelLength) {
        StringBuilder sb = new StringBuilder();
        // 从索引 1 开始，到倒数第二个结束，跳过最顶层和最底层
        for (int i = 1; i < heightList.length - 1; i++) {
            double height = heightList[i];
            sb.append(String.format("crack 0 %.2f %.2f %.2f\n", height, modelLength, height));
        }
        return sb.toString();
    }

    public static String generateHorizontalCrack(double x1, double x2, double y) {
        return String.format("crack %.2f %.2f %.2f %.2f\n",
                Math.min(x1, x2), y, Math.max(x1, x2), y);
    }


    public static String generateGroupCommands(List<Double> xList, double yBottom, double yTop, String layerName) {
        StringBuilder sb = new StringBuilder();
        double ya = Math.min(yBottom, yTop);
        double yb = Math.max(yBottom, yTop);

        double xa = Math.min(xList.get(0), xList.get(1));
        double xb = Math.max(xList.get(0), xList.get(1));
        sb.append(String.format("group openOffCut_%s range %.2f %.2f %.2f %.2f\n",
                layerName, xa, xb, ya, yb));

        for (int i = 1; i < xList.size() - 1; i++) {
            double leftX = Math.min(xList.get(i), xList.get(i + 1));
            double rightX = Math.max(xList.get(i), xList.get(i + 1));
            sb.append(String.format("group state_%d_%s range %.2f %.2f %.2f %.2f\n",
                    i, layerName, leftX, rightX, ya, yb));
        }
        return sb.toString();
    }

    /**
     * 根据高度数组生成 jregion 命令（从底到顶，id 从大到小）
     * @param heightList 高度数组（从高到低）
     * @param modelLength 模型总长
     * @return jregion 命令字符串
     */
    public static String generateJregions(double[] heightList, double modelLength) {
        StringBuilder sb = new StringBuilder();

        // 界面的数量 - 1
        int id = heightList.length - 1;

        // 倒序遍历，从最底层往上
        for (int i = heightList.length - 2; i >= 0; i--) {
            double bottom = heightList[i + 1];
            double top = heightList[i];
            sb.append(String.format("jregion id %d 0 %.2f 0 %.2f %.2f %.2f %.2f %.2f\n",
                    id, bottom, top, modelLength, top, modelLength, bottom));
            id--;
        }
        return sb.toString();
    }

    /**
     * 生成网格划分（gen edge）命令。边长取矩形中心到顶点的距离（半对角线）。
     * @param heightList 高度数组（从高到低）
     * @param spacingList 节理间距数组（从顶到底）
     * @param modelLength 模型总长
     * @return gen edge 命令字符串
     */
    public static String generateMeshCommands(double[] heightList, double[] spacingList, double modelLength) {
        StringBuilder sb = new StringBuilder();

        // 顺序遍历，从最顶层开始往下
        for (int i = 0; i < spacingList.length; i++) {
            double top = heightList[i];
            double bottom = heightList[i + 1];
            double width = layerWidthSpacing(i, spacingList[i]);
            double height = layerHeightSpacing(i, spacingList[i]);
            double edge = Math.hypot(width / 2.0, height / 2.0);

            //gen edge 1.4142 range 0 150.00 59.60 62.30
            sb.append(String.format("gen edge %.4f range 0 %.2f %.2f %.2f\n",
                    edge, modelLength, bottom, top));
        }
        return sb.toString();
    }

    /**
     * 生成岩体材质分配与参数赋值
     */
    public static String generateMatCommands(double[] heightList, double modelLength,
                                             double[] rawE, double[] rawPoisson, double[] rawDensity,
                                             double[] rawFriction, double[] rawCohesion, double[] rawTension) {
        StringBuilder sb = new StringBuilder();
        int layerCount = heightList.length - 1;

        sb.append("change cons 3\n\n");
        for (int i = 0; i < layerCount; i++) {
            int matId = i + 1;
            double top = heightList[i];
            double bottom = heightList[i + 1];
            sb.append(String.format("change mat %d range 0 %.2f %.2f %.2f\n", matId, modelLength, bottom, top));
        }

        sb.append("\n");

        for (int i = 0; i < layerCount; i++) {
            int matId = i + 1;
            double E_Pa = rawE[i] * 1e9;
            double v = rawPoisson[i];
            double density_kg = rawDensity[i] * 1000;
            double cohesion_Pa = rawCohesion[i] * 1e6;
            double tension_Pa = rawTension[i] * 1e6;
            double friction = rawFriction[i];

            double K = E_Pa / (3 * (1 - 2 * v));
            double G = E_Pa / (2 * (1 + v));

            sb.append(String.format("prop mat %d d %.0f sh %.3e bulk %.3e fric %.2f coh %.3e ten %.3e\n",
                    matId, density_kg, G, K, friction, cohesion_Pa, tension_Pa));
        }

        return sb.toString();
    }

    /**
     * 生成节理材料分配与参数赋值命令（change jmat 和 prop jmat）
     *
     * @param heightList  高度数组（用于 range 范围）
     * @param jmatKn      法向刚度数组
     * @param jmatKs      切向刚度数组
     * @param jmatFric    节理摩擦角数组
     * @param jmatCoh     节理内聚力数组
     * @param jmatTen     节理抗拉强度数组
     * @param modelLength 模型总长
     * @return change jmat 与 prop jmat 命令字符串
     */
    public static String generatePropJmatCommands(double[] heightList, double[] jmatKn, double[] jmatKs,
                                                  double[] jmatFric, double[] jmatCoh, double[] jmatTen, double modelLength) {
        StringBuilder sb = new StringBuilder();

        //设定节理本构模型（库仑滑移 jcons=2）
        sb.append("change jcons 2\n\n");

        int layerCount = heightList.length - 1;

        //从上到下分配 jmat 编号
        for (int i = 0; i < layerCount; i++) {
            int jmatId = i + 1;
            double top = heightList[i];
            double bottom = heightList[i + 1];

            sb.append(String.format("change jmat %d range 0 %.2f %.2f %.2f\n",
                    jmatId, modelLength, bottom, top));
        }
        sb.append("\n");

        //
        for (int i = 0; i < layerCount; i++) {
            int jmatId = i + 1;
            sb.append(String.format("prop jmat %d jkn %.1e jks %.1e jfric %.0f jcoh %.1f jten %.1e\n",
                    jmatId, jmatKn[i], jmatKs[i], jmatFric[i], jmatCoh[i], jmatTen[i]));
        }

        return sb.toString();
    }

    /**
     * 生成 UDEC 出图并保存的命令
     * @param title    图片标题
     * @param plotCmd  绘图命令
     * @param fileName 输出的文件名
     * @return 出图命令字符串
     */
    public static String generatePlotCommands(String title, String plotCmd, String fileName) {
        return "title\n" +
                title + "\n" +
                plotCmd + "\n" +
                "set output " + ParameterList.normalizedOutputPath() + "/output/" + fileName + "\n" +
                "set plot jpg size " + ParameterList.outputWidth + " " + ParameterList.outputHeight + "\n" +
                "copy\n\n";
    }

    /**
     * 按每一层的节理组生成 jset 命令。输入间距只作期望值，
     * 0° 用高度间距、90° 用宽度间距；网格用 gen edge，边长取矩形半对角线。
     */
    public static String generateJsetsFromLayers(double[] heightList, java.util.List<java.util.List<JointSet>> layerJoints,
                                                 double modelLength) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < layerJoints.size(); i++) {
            double top = heightList[i];
            double bottom = heightList[i + 1];
            double layerHeight = top - bottom;
            for (JointSet js : layerJoints.get(i)) {
                double angle = js.angle;
                boolean vertical = Math.abs(angle - 90.0) < 1e-6;
                double widthSpacing = layerWidthSpacing(i, js.spacing);
                double heightSpacing = layerHeightSpacing(i, js.spacing);
                if (vertical && js.baseGroup) {
                    appendBrickJsets(sb, i, js, bottom, widthSpacing);
                    continue;
                }
                double spacingMean = vertical ? widthSpacing : heightSpacing;
                double startX = js.startX;
                double startY = js.startY;
                if (js.baseGroup) startY = excavationStartY(i);
                double angleStd = js.angleStdDev;
                double trace = js.traceLength;
                double traceStd = js.traceStdDev;
                double gap = js.bridgeLength;
                double gapStd = js.bridgeStdDev;
                if (Math.abs(angle) < 1e-6) {
                    if (js.baseGroup) trace = modelLength;
                } else if (vertical) {
                    if (js.baseGroup) trace = layerHeight;
                }

                sb.append("jset ").append(fAngle(angle)).append(' ').append(f2z(angleStd)).append(' ')
                        .append(f2a(trace)).append(' ').append(f2z(traceStd)).append(' ')
                        .append(f2z(gap)).append(' ').append(f2z(gapStd)).append(' ')
                        .append(f4a(spacingMean)).append(' ')
                        .append(f4z(js.spacingStdDev)).append(' ')
                        .append(f4z(startX)).append(' ').append(f4a(startY))
                        .append(" range jregion ").append(i + 1).append('\n');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private static void appendBrickJsets(StringBuilder sb, int layerIndex, JointSet js,
                                         double bottom, double widthSpacing) {
        double target = layerHeightSpacing(layerIndex, js.spacing);
        double rowHeight = target;
        double startY = excavationStartY(layerIndex);
        double phase = excavationPhase(layerIndex, widthSpacing);
        appendVerticalSet(sb, layerIndex, js, phase, startY, rowHeight);
        appendVerticalSet(sb, layerIndex, js, phase + widthSpacing / 2.0,
                startY + rowHeight, rowHeight);
    }

    private static void appendVerticalSet(StringBuilder sb, int layerIndex, JointSet js,
                                          double startX, double startY, double rowHeight) {
        sb.append("jset 90 ").append(f2z(js.angleStdDev)).append(' ')
                .append(f4a(rowHeight)).append(' ').append(f2z(js.traceStdDev)).append(' ')
                .append(f4a(rowHeight)).append(' ').append(f2z(js.bridgeStdDev)).append(' ')
                .append(f4a(widthSpacingFor(layerIndex, js))).append(' ')
                .append(f4z(js.spacingStdDev)).append(' ')
                .append(f4z(startX)).append(' ').append(f4a(startY))
                .append(" range jregion ").append(layerIndex + 1).append('\n');
    }

    private static double widthSpacingFor(int layerIndex, JointSet js) {
        return layerWidthSpacing(layerIndex, js.spacing);
    }

    public static double layerWidthSpacing(int layerIndex, double inputSpacing) {
        double target = scaledTarget(inputSpacing);
        ExcavationLayer region = primaryRegion(layerIndex);
        if (region != null) {
            double height = layerHeightSpacing(layerIndex, inputSpacing);
            double width = Math.abs(region.x2 - region.x1);
            int base = (int) Math.max(1,
                    Math.round(ParameterList.modelLength / height + 1e-9));
            double best = ParameterList.modelLength / base;
            double bestScore = Double.MAX_VALUE;
            for (int k = -3; k <= 3; k++) {
                int columns = base + k;
                if (columns < 1) continue;
                double candidate = ParameterList.modelLength / columns;
                double remainder = width % candidate;
                double sliver = Math.max(0, candidate * 0.5 - remainder) / candidate;
                double aspect = Math.abs(candidate / height - 1.0);
                double score = aspect + sliver * 0.5;
                if (score < bestScore) {
                    bestScore = score;
                    best = candidate;
                }
            }
            return best;
        }
        double range = Math.max(0.05, target * 0.50);
        int steps = (int) Math.round(range / 0.01);
        double best = -1;
        double bestScore = Double.MAX_VALUE;
        for (int i = -steps; i <= steps; i++) {
            double candidate = round2(target + i * 0.01);
            if (candidate < 0.05) continue;
            if (!multiple(ParameterList.modelLength, candidate)) continue;
            if (!alignsWithExcavation(layerIndex, candidate)) continue;
            double score = Math.abs(candidate - target);
            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        if (best <= 0) {
            for (ExcavationLayer e : ParameterList.excavationLayers) {
                if (overlapHeight(e, layerIndex) <= 1e-6) continue;
                double width = Math.abs(e.x2 - e.x1);
                if (width <= 1e-9) continue;
                int parts = (int) Math.max(1, Math.round(width / target + 1e-9));
                double candidate = round2(width / parts);
                if (candidate >= 0.05 && multiple(ParameterList.modelLength, candidate)) {
                    best = candidate;
                    break;
                }
            }
        }
        return best > 0 ? best : target;
    }

    public static double layerHeightSpacing(int layerIndex, double inputSpacing) {
        double top = ParameterList.heightList[layerIndex];
        double bottom = ParameterList.heightList[layerIndex + 1];
        double target = scaledTarget(inputSpacing);
        ExcavationLayer region = primaryRegion(layerIndex);
        if (region != null) {
            double height = overlapHeight(region, layerIndex);
            int rows = (int) Math.max(1, Math.round(height / target + 1e-9));
            if (ParameterList.debugMode) rows = Math.max(2, rows);
            return height / rows;
        }
        int parts = (int) Math.max(1, Math.round((top - bottom) / target + 1e-9));
        if (ParameterList.debugMode) parts = Math.max(2, parts);
        return (top - bottom) / parts;
    }

    private static double scaledTarget(double inputSpacing) {
        double target = round2(Math.max(inputSpacing, 0.05));
        if (ParameterList.debugMode) target = round2(target * Math.max(1.0, ParameterList.debugScale));
        return target;
    }

    public static double excavationPhase(int layerIndex, double spacing) {
        ExcavationLayer region = primaryRegion(layerIndex);
        if (region == null || spacing <= 1e-9) return 0.0;
        return Math.min(region.x1, region.x2) % spacing;
    }

    public static double excavationStartY(int layerIndex) {
        ExcavationLayer region = primaryRegion(layerIndex);
        if (region != null) {
            return Math.max(ParameterList.heightList[layerIndex + 1],
                    Math.min(region.y1, region.y2));
        }
        if (ParameterList.heightList != null && layerIndex >= 0
                && layerIndex + 1 < ParameterList.heightList.length) {
            return ParameterList.heightList[layerIndex + 1];
        }
        return 0.0;
    }

    static ExcavationLayer primaryRegion(int layerIndex) {
        if (ParameterList.heightList == null || layerIndex < 0
                || layerIndex + 1 >= ParameterList.heightList.length) return null;
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            if (Math.abs(e.x2 - e.x1) < 1e-9 || Math.abs(e.y2 - e.y1) < 1e-9) continue;
            if (overlapHeight(e, layerIndex) <= 1e-6) continue;
            return e;
        }
        return null;
    }

    static double overlapHeight(ExcavationLayer e, int layerIndex) {
        if (ParameterList.heightList == null || layerIndex < 0
                || layerIndex + 1 >= ParameterList.heightList.length) return 0.0;
        double top = ParameterList.heightList[layerIndex];
        double bottom = ParameterList.heightList[layerIndex + 1];
        double y1 = Math.min(e.y1, e.y2);
        double y2 = Math.max(e.y1, e.y2);
        return Math.min(top, y2) - Math.max(bottom, y1);
    }

    // 开挖区域覆盖到的层都要参与节理适配，而不是只适配被标记的煤层。
    public static boolean excavationAffectsLayer(ExcavationLayer e, int layerIndex) {
        if (ParameterList.heightList == null || layerIndex < 0
                || layerIndex + 1 >= ParameterList.heightList.length) return false;
        double top = ParameterList.heightList[layerIndex];
        double bottom = ParameterList.heightList[layerIndex + 1];
        double y1 = Math.min(e.y1, e.y2);
        double y2 = Math.max(e.y1, e.y2);
        return y2 >= bottom - 1e-6 && y1 <= top + 1e-6;
    }

    private static double remainder(double length, double spacing) {
        double parts = Math.floor(length / spacing + 1e-9);
        return length - parts * spacing;
    }

    private static boolean alignsWithExcavation(int layerIndex, double spacing) {
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            if (overlapHeight(e, layerIndex) <= 1e-6) continue;
            double width = Math.abs(e.x2 - e.x1);
            if (!multiple(width, spacing)) return false;
            if (!multiple(e.cutWidth, spacing) || !multiple(e.stepDist, spacing)) return false;
        }
        return true;
    }

    private static boolean multiple(double value, double spacing) {
        double r = remainder(value, spacing);
        return r < 1e-6 || spacing - r < 1e-6;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static String fAngle(double v) {
        if (Math.abs(v - Math.rint(v)) < 1e-9) return String.valueOf((int) Math.rint(v));
        return String.format("%.2f", v);
    }

    private static String f2z(double v) {
        if (Math.abs(v) < 1e-12) return "0";
        return String.format("%.2f", v);
    }

    private static String f2a(double v) {
        return String.format("%.2f", v);
    }

    private static String f4z(double v) {
        if (Math.abs(v) < 1e-12) return "0";
        return String.format("%.4f", v);
    }

    private static String f4a(double v) {
        return String.format("%.4f", v);
    }

}
