import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

class ParameterList {
    // 所有可配置模型参数默认值集中在此；其他类只读取这里，不再写死默认值。
    // 体积模量 K、剪切模量 G 不在此配置，生成 prop mat 时由 E 与泊松比计算。
    // 越靠下的参数越不建议更改！

    // 输出路径
    public static String outputPath = "F:/Itasca/UDECProjects/102";
    public static String projectPath = "";
    public static boolean restoring = false;

    // 阶段自动调用开关
    public static boolean s1Calls2 = true;
    public static boolean s2Calls3 = true;
    public static boolean s3Calls4 = false;

    // 输出图片尺寸
    public static int outputWidth = 1920;
    public static int outputHeight = 1200;

    // 模型总宽度
    public static double modelLength = 150;

    // 开挖参数
    public static int step = 3;
    public static double stepDist = 10;
    public static double cutWidth = 8;
    public static Util.Direction direction = Util.Direction.LEFT_TO_RIGHT;
    public static double excavationHeight = 0.0;
    public static boolean excavationCentered = true;
    public static double excavationOriginX = Double.NaN;
    public static double excavationOriginY = Double.NaN;
    public static String excavationPreset = "开切眼 + 工作面推进";

    // UDEC 可执行文件与自动调用
    public static String udecExePath = "F:/Itasca/udec4.0/udec400/udec_dp.exe";
    public static boolean autoRunAfterGenerate = true;
    public static boolean debugMode = false;
    public static double debugScale = 10.0;
    public static int udecMemoryHigh = 2048;
    public static int udecMemoryLow = 1024;
    public static int udecWindowPollCount = 20;
    public static int udecWindowPollMs = 500;
    public static int udecActivateWaitSec = 2;

    // 各阶段图标题
    public static String step1Title = "模型的建立与岩体节理的生成";
    public static String step2Title = "模型网格生成与岩体参数的设定";
    public static final java.util.List<PlotPreset> plotPresets1 = new java.util.ArrayList<>();
    public static final java.util.List<PlotPreset> plotPresets2 = new java.util.ArrayList<>();
    public static final java.util.List<PlotPreset> plotPresets3 = new java.util.ArrayList<>();

    // 煤层在 layers 中的下标（从上往下，0 开始）
    public static int coal9Layer = 6;
    public static int coal10Layer = 8;

    // 数值控制
    public static double round = 0.03;
    public static double ovtol = 0.8;
    public static double boundTol = 0.1;
    public static double gravity = -10.0;
    public static double stressTop = 0.0;
    public static double stressGradient = 25000.0;
    public static double stressX = 0.0;
    public static double stressXY = 0.0;
    public static boolean stressEnabled = true;

    // 画线边界
    public static double boundaryWidth = 0.2;
    public static final List<BoundaryCondition> boundaries = new ArrayList<>();

    // 新建节理组的默认值
    public static double jointAngle = 45.0;
    public static double jointAngleStdDev = 0.0;
    public static double jointBridgeLength = 5.0;
    public static double jointBridgeStdDev = 0.0;
    public static double jointTraceLength = 10.0;
    public static double jointTraceStdDev = 0.0;
    public static double jointSpacing = 2.0;
    public static double jointSpacingStdDev = 0.0;
    public static double jointStartX = 0.0;
    public static double jointStartY = 0.0;

    // Step4 支护与监测参数
    public static int supportMat = 21;
    public static double supportKn = 1.0e9;
    public static double supportSpacing = 1.0;
    public static double supportYcomp = 1.0e6;
    public static int supportSegments = 5;
    public static double supportOffset = 0.5;
    public static double monitorStep = 10.0;

    // 界面数据模型：从上到下
    public static final List<LayerData> layers = new ArrayList<>();
    public static final List<ExcavationLayer> excavationLayers = new ArrayList<>();

    // 供原生成器使用的数组（由 layers 同步得到）
    public static double[] heightList;
    public static double[] spacing;
    public static double[] raw_E;
    public static double[] raw_poisson;
    public static double[] raw_density;
    public static double[] raw_friction;
    public static double[] raw_cohesion;
    public static double[] raw_tension;
    public static double[] jmat_kn;
    public static double[] jmat_ks;
    public static double[] jmat_fric;
    public static double[] jmat_coh;
    public static double[] jmat_ten;
    public static List<List<JointSet>> layerJoints = new ArrayList<>();

    static {
        layers.add(new LayerData("细砂岩", 2.7, 6.076, 0.26, 2.654, 37.26, 10.607, 1.723,
                10e9, 20e9, 15, 0, 10e6, 0.30, new Color(194, 178, 128)));
        layers.add(new LayerData("泥岩", 3.3, 1.983, 0.31, 2.543, 43.14, 3.579, 1.494,
                8e9, 16e9, 8, 0, 0, 0.30, new Color(120, 120, 120)));
        layers.add(new LayerData("中粒砂岩", 6.9, 5.211, 0.226, 2.585, 41.886, 14.203, 2.55,
                8e9, 16e9, 8, 0, 0, 0.25, new Color(210, 180, 140)));
        layers.add(new LayerData("泥岩", 6.1, 5.033, 0.354, 2.571, 42.63, 4.047, 1.2,
                4e9, 8e9, 4, 0, 0, 0.25, new Color(110, 110, 110)));
        layers.add(new LayerData("粉砂岩", 9.1, 6.723, 0.155, 2.631, 39.4, 6.616, 1.21,
                5e9, 10e9, 5, 0, 0, 0.2, new Color(180, 160, 120)));
        layers.add(new LayerData("泥岩", 7.8, 6.715, 0.173, 2.609, 35.9, 8.596, 2.509,
                8e9, 16e9, 15, 0, 0, 0.2, new Color(100, 100, 100)));
        layers.add(new LayerData("9#煤", 4.8, 2.479, 0.107, 1.224, 41.5, 1.167, 0.225,
                12e9, 24e9, 20, 0, 0, 0.25, new Color(35, 35, 35)));
        layers.add(new LayerData("粉砂岩", 5.8, 9.937, 0.259, 2.398, 36.7, 9.906, 3.543,
                10e9, 20e9, 15, 0, 10e6, 0.2, new Color(190, 170, 130)));
        layers.add(new LayerData("10#煤", 4.2, 6.032, 0.355, 1.412, 42.9, 0.686, 0.187,
                8e9, 16e9, 8, 0, 0, 0.25, new Color(25, 25, 25)));
        layers.add(new LayerData("粗砂岩", 11.6, 20.588, 0.231, 2.523, 41.3, 11.072, 7.445,
                8e9, 16e9, 8, 0, 0, 0.25, new Color(160, 140, 100)));

        excavationLayers.add(new ExcavationLayer(6, "9"));
        excavationLayers.add(new ExcavationLayer(8, "10"));
        for (ExcavationLayer e : excavationLayers) {
            LayerData l = layers.get(e.layerIndex);
            if (l.savedColor == null) l.savedColor = l.color;
            l.color = Color.BLACK;
        }

        plotPresets1.addAll(PlotPresets.stage1());
        plotPresets2.addAll(PlotPresets.stage2());
        plotPresets3.addAll(PlotPresets.stage3());
        syncFromLayers();
        initDefaultBoundaries();
    }

    static void ensurePlotPresets() {
        if (plotPresets1.isEmpty()) plotPresets1.addAll(PlotPresets.stage1());
        if (plotPresets2.isEmpty()) plotPresets2.addAll(PlotPresets.stage2());
        if (plotPresets3.isEmpty()) plotPresets3.addAll(PlotPresets.stage3());
    }

    static void initDefaultBoundaries() {
        boundaries.clear();
        double h = heights().get(0);
        double l = modelLength;
        BoundaryCondition left = new BoundaryCondition(
                BoundaryCondition.Type.VELOCITY_X, 0, 0, 0, h, boundaryWidth);
        left.name = "左边界";
        left.edge = BoundaryCondition.Edge.LEFT;
        BoundaryCondition right = new BoundaryCondition(
                BoundaryCondition.Type.VELOCITY_X, l, 0, l, h, boundaryWidth);
        right.name = "右边界";
        right.edge = BoundaryCondition.Edge.RIGHT;
        BoundaryCondition bottom = new BoundaryCondition(
                BoundaryCondition.Type.VELOCITY_Y, 0, 0, l, 0, boundaryWidth);
        bottom.name = "底边界";
        bottom.edge = BoundaryCondition.Edge.BOTTOM;
        BoundaryCondition top = new BoundaryCondition(
                BoundaryCondition.Type.STRESS, 0, h, l, h, boundaryWidth);
        top.name = "上边界（应力）";
        top.edge = BoundaryCondition.Edge.TOP;
        boundaries.add(left);
        boundaries.add(right);
        boundaries.add(bottom);
        boundaries.add(top);
    }

    public static void syncBoundaryEdges() {
        if (heightList == null || heightList.length == 0) return;
        double h = heightList[0];
        double l = modelLength;
        for (BoundaryCondition b : boundaries) {
            if (b.edge == BoundaryCondition.Edge.LEFT) {
                b.sx = 0; b.ex = 0; b.sy = 0; b.ey = h;
            } else if (b.edge == BoundaryCondition.Edge.RIGHT) {
                b.sx = l; b.ex = l; b.sy = 0; b.ey = h;
            } else if (b.edge == BoundaryCondition.Edge.BOTTOM) {
                b.sx = 0; b.ex = l; b.sy = 0; b.ey = 0;
            } else if (b.edge == BoundaryCondition.Edge.TOP) {
                b.sx = 0; b.ex = l; b.sy = h; b.ey = h;
            }
        }
    }

    static void applyExcavationDefaults(ExcavationLayer e) {
        e.preset = excavationPreset;
        e.cutWidth = cutWidth;
        e.overallHeight = excavationHeight;
        e.steps = step;
        e.stepDist = stepDist;
        e.direction = direction;
        e.centered = excavationCentered;
        e.originX = excavationOriginX;
        e.originY = excavationOriginY;
    }

    static void applyJointDefaults(JointSet js) {
        js.angleStdDev = jointAngleStdDev;
        js.bridgeLength = jointBridgeLength;
        js.bridgeStdDev = jointBridgeStdDev;
        js.traceLength = jointTraceLength;
        js.traceStdDev = jointTraceStdDev;
        js.spacing = jointSpacing;
        js.spacingStdDev = jointSpacingStdDev;
    }

    public static void syncFromLayers() {
        int n = layers.size();
        heightList = new double[n + 1];
        spacing = new double[n];
        raw_E = new double[n];
        raw_poisson = new double[n];
        raw_density = new double[n];
        raw_friction = new double[n];
        raw_cohesion = new double[n];
        raw_tension = new double[n];
        jmat_kn = new double[n];
        jmat_ks = new double[n];
        jmat_fric = new double[n];
        jmat_coh = new double[n];
        jmat_ten = new double[n];
        layerJoints = new ArrayList<>();

        for (ExcavationLayer e : excavationLayers) {
            if (e.layerIndex >= 0) {
                e.layerIndex = Math.min(e.layerIndex, Math.max(0, n - 1));
            }
        }
        if (!excavationLayers.isEmpty() && excavationLayers.get(0).layerIndex >= 0) {
            coal9Layer = excavationLayers.get(0).layerIndex;
        }
        if (excavationLayers.size() > 1 && excavationLayers.get(1).layerIndex >= 0) {
            coal10Layer = excavationLayers.get(1).layerIndex;
        }

        double h = 0;
        for (int i = n - 1; i >= 0; i--) {
            h += layers.get(i).thickness;
            h = Math.round(h * 1e6) / 1e6;
            heightList[i] = h;
        }
        heightList[n] = 0;

        for (int i = 0; i < n; i++) {
            LayerData l = layers.get(i);
            for (JointSet js : l.joints) {
                if (js.baseGroup) {
                    js.angleStdDev = 0.0;
                    js.bridgeLength = 0.0;
                    js.bridgeStdDev = 0.0;
                    js.traceStdDev = 0.0;
                    js.startX = 0.0;
                    js.startY = heightList[i + 1];
                    js.linkSpacing = true;
                    if (Math.abs(js.angle) < 1e-6) {
                        js.traceLength = modelLength;
                    } else if (Math.abs(js.angle - 90.0) < 1e-6) {
                        js.traceLength = l.thickness;
                    }
                }
            }
            spacing[i] = l.meshSize;
            raw_E[i] = l.E;
            raw_poisson[i] = l.poisson;
            raw_density[i] = l.density;
            raw_friction[i] = l.friction;
            raw_cohesion[i] = l.cohesion;
            raw_tension[i] = l.tension;
            jmat_kn[i] = l.jkn;
            jmat_ks[i] = l.jks;
            jmat_fric[i] = l.jfric;
            jmat_coh[i] = l.jcoh;
            jmat_ten[i] = l.jten;
            layerJoints.add(l.joints);
        }
        syncExcavationGeometry();
        syncBoundaryEdges();
    }

    static void syncExcavationGeometry() {
        for (ExcavationLayer e : excavationLayers) {
            if (!e.autoGeometry || e.layerIndex < 0 || e.layerIndex + 1 >= heightList.length) {
                continue;
            }
            double bottom = heightList[e.layerIndex + 1];
            double height = e.overallHeight > 0
                    ? e.overallHeight
                    : layers.get(e.layerIndex).thickness;
            double total = e.cutWidth + e.steps * e.stepDist;
            if (total <= 0) total = e.cutWidth;
            double left = (modelLength - total) / 2.0;
            e.x1 = left;
            e.x2 = left + total;
            e.y1 = bottom;
            e.y2 = bottom + height;
        }
    }

    public static List<Double> heights() {
        if (heightList == null || heightList.length != layers.size() + 1) syncFromLayers();
        List<Double> list = new ArrayList<>();
        for (double h : heightList) list.add(h);
        return list;
    }

    public static int layerCount() {
        return layers.size();
    }
}
