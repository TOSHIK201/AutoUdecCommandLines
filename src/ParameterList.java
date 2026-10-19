class ParameterList {
    //输出路径
    public static final String outputPath = "F:/Itasca/UDECProjects/101";

    //step1自动调用step2
    public static final boolean s1Calls2 = true;

    //step2自动调用step3
    public static final boolean s2Calls3 = true;

    //step3自动调用step4
    public static final boolean s3Calls4 = false;

    //输出图片宽度
    public static final int outputWidth = 1920;

    //输出图片高度
    public static final int outputHeight = 1200;

    //模型总宽度(m)
    public static final double modelLength = 150;

    //开挖步数
    public static final int step = 3;

    //开挖步距(m)
    public static final double stepDist = 10;

    //开切眼宽度(m)
    public static final double cutWidth = 8;

    //开挖方向(LEFT_TO_RIGHT,RIGHT_TO_LEFT)
    public static final Util.Direction direction = Util.Direction.LEFT_TO_RIGHT;

    //节理尺寸
    public static final double[] spacing = new double[]{0.7, 0.6, 0.6, 0.5, 0.5, 0.5, 0.7, 0.5, 0.7, 0.7};
    //public static final double[] spacing = new double[]{14, 13, 12, 11, 10, 10, 15, 10, 15, 15};

    //从此往下的参数请不要轻易更改，除非你很清楚你在干什么
    //圆角数
    public static final double round = 0.03;

    //嵌入容差
    public static final double ovtol = 0.8;

    //从上至下各层面相对于最下部岩层底部平面的高度
    public static final double[] heightList = new double[]{62.3, 59.6, 56.3, 49.4, 43.3, 34.2, 26.4, 21.6, 15.8, 11.6, 0};

    // 平均弹性模量 (GPa)
    public static final double[] raw_E = new double[]{6.076, 1.983, 5.211, 5.033, 6.723, 6.715, 2.479, 9.937, 6.032, 20.588};

    // 平均泊松比
    public static final double[] raw_poisson = new double[]{0.26, 0.31, 0.226, 0.354, 0.155, 0.173, 0.107, 0.259, 0.355, 0.231};

    // 平均单轴抗拉强度 (MPa)
    public static final double[] raw_tension = new double[]{1.723, 1.494, 2.55, 1.2, 1.21, 2.509, 0.225, 3.543, 0.187, 7.445};

    // 平均内聚力 (MPa)
    public static final double[] raw_cohesion = new double[]{10.607, 3.579, 14.203, 4.047, 6.616, 8.596, 1.167, 9.906, 0.686, 11.072};

    // 平均内摩擦角 (°)
    public static final double[] raw_friction = new double[]{37.26, 43.14, 41.886, 42.63, 39.4, 35.9, 41.5, 36.7, 42.9, 41.3};

    // 平均块体密度 (g/cm3)
    public static final double[] raw_density = new double[]{2.654, 2.543, 2.585, 2.571, 2.631, 2.609, 1.224, 2.398, 1.412, 2.523};

    // 节理参数 (从上到下)
    public static final double[] jmat_kn = new double[]{10e9, 8e9, 8e9, 4e9, 5e9, 8e9, 12e9, 10e9, 8e9, 8e9};

    public static final double[] jmat_ks = new double[]{20e9, 16e9, 16e9, 8e9, 10e9, 16e9, 24e9, 20e9, 16e9, 16e9};

    public static final double[] jmat_fric = new double[]{15, 8, 8, 4, 5, 15, 20, 15, 8, 8};

    public static final double[] jmat_coh = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    public static final double[] jmat_ten = new double[]{10e6, 0, 0, 0, 0, 0, 0, 10e6, 0, 0};
}
