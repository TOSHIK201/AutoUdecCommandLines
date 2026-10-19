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
}
