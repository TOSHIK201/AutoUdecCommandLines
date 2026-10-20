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
        return calculateExcavationXList(e.steps, e.stepDist, e.cutWidth, e.direction, e.centered, e.originX);
    }

    public static double excavationBottom(ExcavationLayer e, int layerIndex) {
        if (e.centered || Double.isNaN(e.originY)) {
            return ParameterList.heightList[layerIndex + 1];
        }
        return e.originY;
    }

    public static double excavationHeight(ExcavationLayer e, int layerIndex) {
        return e.overallHeight > 0 ? e.overallHeight : ParameterList.layers.get(layerIndex).thickness;
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
     * 生成 jset 命令
     *
     * @param heightList  高度数组（从高到低）
     * @param spacingList 期望的节理间距数组（从顶到底）
     * @param modelLength 模型总长
     * @return jset 命令字符串
     */
    public static String generateJsets(double[] heightList, double[] spacingList, double modelLength) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < spacingList.length; i++) {
            // 当前层的顶部、底部高度和厚度
            double top = heightList[i];
            double bottom = heightList[i + 1];
            double layerHeight = top - bottom;

            // 期望间距
            double targetSpacing = spacingList[i];

            //计算 Y 方向份数
            int numBlocksY = (int) Math.round(layerHeight / targetSpacing);
            if (numBlocksY < 1) numBlocksY = 1;
            double spacingY = layerHeight / numBlocksY;

            //计算 X 方向份数
            int numBlocksX = (int) Math.round(modelLength / spacingY);
            if (numBlocksX < 1) numBlocksX = 1;
            double spacingX = modelLength / numBlocksX;

            String range = "range jregion " + (i + 1);

            sb.append(String.format("jset 0 0 %.2f 0 0 0 %.4f 0 0 %.2f %s\n",
                    modelLength, spacingY, bottom, range));

            sb.append(String.format("jset 90 0 %.2f 0 0 0 %.4f 0 0 %.2f %s\n",
                    layerHeight, spacingX, bottom, range));

            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 生成网格划分（gen quad）命令
     * @param heightList 高度数组（从高到低）
     * @param spacingList 节理间距数组（从顶到底）
     * @param modelLength 模型总长
     * @return gen quad 命令字符串
     */
    public static String generateMeshCommands(double[] heightList, double[] spacingList, double modelLength) {
        StringBuilder sb = new StringBuilder();

        // 顺序遍历，从最顶层开始往下
        for (int i = 0; i < spacingList.length; i++) {
            double top = heightList[i];
            double bottom = heightList[i + 1];
            double size = spacingList[i];

            //gen quad 2.0 range 0 150.00 59.60 62.30
            sb.append(String.format("gen quad %.2f range 0 %.2f %.2f %.2f\n",
                    size, modelLength, bottom, top));
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
                "set output " + ParameterList.outputPath + "/output/" + fileName + "\n" +
                "set plot jpg size " + ParameterList.outputWidth + " " + ParameterList.outputHeight + "\n" +
                "copy\n\n";
    }

    /**
     * 按每一层的节理组生成 jset 命令。默认 0/90 两组时，
     * 先用目标间距把层厚分成整数份，再在 X 方向取同样的边长，保证岩块接近正方形。
     */
    public static String generateJsetsFromLayers(double[] heightList, java.util.List<java.util.List<JointSet>> layerJoints,
                                                 double modelLength) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < layerJoints.size(); i++) {
            double top = heightList[i];
            double bottom = heightList[i + 1];
            double layerHeight = top - bottom;
            for (JointSet js : layerJoints.get(i)) {
                double target = Math.max(js.spacing, 0.05);
                if (ParameterList.debugMode) target *= Math.max(1.0, ParameterList.debugScale);
                int numY = (int) Math.max(1, Math.round(layerHeight / target));
                double spacingY = layerHeight / numY;
                int numX = (int) Math.max(1, Math.round(modelLength / spacingY));
                double spacingX = modelLength / numX;

                double angle = js.angle;
                double angleStd = js.angleStdDev;
                double trace = js.traceLength;
                double traceStd = js.traceStdDev;
                double gap = js.bridgeLength;
                double gapStd = js.bridgeStdDev;
                double spacingMean;
                if (Math.abs(angle) < 1e-6) {
                    spacingMean = spacingY;
                    if (js.baseGroup) trace = modelLength;
                } else if (Math.abs(angle - 90.0) < 1e-6) {
                    spacingMean = spacingX;
                    if (js.baseGroup) trace = layerHeight;
                } else {
                    spacingMean = js.spacing;
                }

                sb.append("jset ").append(fAngle(angle)).append(' ').append(f2z(angleStd)).append(' ')
                        .append(f2a(trace)).append(' ').append(f2z(traceStd)).append(' ')
                        .append(f2z(gap)).append(' ').append(f2z(gapStd)).append(' ')
                        .append(f4a(spacingMean)).append(' ').append(f4z(js.spacingStdDev)).append(' ')
                        .append(f2z(js.startX)).append(' ').append(f2a(js.startY))
                        .append(" range jregion ").append(i + 1).append('\n');
            }
            sb.append('\n');
        }
        return sb.toString();
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
