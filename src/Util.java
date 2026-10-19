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
        double totalLength = ParameterList.modelLength;
        double centerX = totalLength / 2.0;

        double totalExcavationWidth = cutWidth + (steps * stepDist);
        double startX = centerX - (totalExcavationWidth / 2.0);

        List<Double> xList = new ArrayList<>();
        xList.add(startX);

        double currentX = startX + cutWidth;
        xList.add(currentX);

        for (int i = 0; i < steps; i++) {
            currentX += stepDist;
            xList.add(currentX);
        }

        // 如果是从右向左，翻转坐标
        if (direction == Direction.RIGHT_TO_LEFT) {
            List<Double> reversedList = new ArrayList<>();
            for (Double x : xList) {
                reversedList.add(totalLength - x);
            }
            return reversedList;
        }

        return xList;
    }

    /**
     * 根据计算好的 X 坐标生成垂直 crack 命令
     * @param xList   坐标集合
     * @param yBottom 煤层底部高度
     * @param yTop    煤层顶部高度
     * @return 包含所有 crack 命令的字符串
     */
    public static String generateVerticalCracks(List<Double> xList, double yBottom, double yTop) {
        StringBuilder sb = new StringBuilder();
        for (Double x : xList) {
            sb.append(String.format("crack %.2f %.2f %.2f %.2f\n", x, yBottom, x, yTop));
        }
        return sb.toString();
    }

    /**
     * 根据传入的高度数组，生成水平层理 crack 命令字符串
     * @param heightList 高度数组（从高到低，包含顶部和底部边界）
     * @param modelLength 模型总长度
     * @return 包含所有水平 crack 命令的字符串
     */
    public static String generateHorizontalCracks(double[] heightList, double modelLength) {
        StringBuilder sb = new StringBuilder();
        // 从索引 1 开始，到倒数第二个结束，跳过最顶层和最底层
        for (int i = 1; i < heightList.length - 1; i++) {
            double height = heightList[i];
            sb.append(String.format("crack 0 %.2f %.2f %.2f\n", height, modelLength, height));
        }
        return sb.toString();
    }

    /**
     * 根据 X 坐标集合，生成煤层的分组（group）命令字符串
     *
     * @param xList     计算好的 X 坐标集合
     * @param yBottom   煤层的底部高度
     * @param yTop      煤层的顶部高度
     * @param layerNum  煤层编号（例如 9 或 10），用于生成对应的 group 名称
     * @return 包含所有 group 命令的字符串
     */
    public static String generateGroupCommands(List<Double> xList, double yBottom, double yTop, int layerNum) {
        StringBuilder sb = new StringBuilder();

        // 1. 生成切眼分组（第一段）
        // 索引 0 和 1 分别是切眼的左边界和右边界
        sb.append(String.format("group openOffCut_%d range %.2f %.2f %.2f %.2f\n",
                layerNum, xList.get(0), xList.get(1), yBottom, yTop));

        // 2. 生成开采区分组（后续段）
        // 从索引 1 开始循环到倒数第二个，每次取相邻两个点
        // 例如：i=1 时，取 xList[1] 和 xList[2] 作为 state_1_9 的左右边界
        for (int i = 1; i < xList.size() - 1; i++) {
            double leftX = xList.get(i);
            double rightX = xList.get(i + 1);

            sb.append(String.format("group state_%d_%d range %.2f %.2f %.2f %.2f\n",
                    i, layerNum, leftX, rightX, yBottom, yTop));
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
     * @param modelLength 模型总长
     * @param spacingList 各层节理间距数组（对应 jregion 1 到 N）
     * @return jset 命令字符串
     */
    public static String generateJsets(double modelLength, double[] spacingList) {
        StringBuilder sb = new StringBuilder();

        // 遍历 spacing 数组，长度是多少就生成多少个 jregion 的节理
        for (int i = 1; i <= spacingList.length; i++) {
            double spacing = spacingList[i - 1];
            String range = "range jregion " + i;

            // 0度节理
            sb.append(String.format("jset 0 0 %.2f 0 0 0 %.2f 0 0 0 %s\n", modelLength, spacing, range));
            // 90度节理
            sb.append(String.format("jset 90 0 %.2f 0 0 0 %.2f 0 0 0 %s\n", modelLength, spacing, range));
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
}
