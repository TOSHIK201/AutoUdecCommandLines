class ParameterList {
    //输出路径
    public static final String outputPath = "F:/Itasca/UDECProjects/101";

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
    public static final double[] spacing = new double[]{10, 5, 2.5, 1.5, 1, 1, 10, 1, 10, 15};

    //圆角数
    public static final double round = 0.03;

    //嵌入容差
    public static final double ovtol = 0.8;

    //从上至下各层面相对于最下部岩层底部平面的高度
    public static final double[] heightList = new double[]{62.3, 59.6, 56.3, 49.4, 43.3, 34.2, 26.4, 21.6, 15.8, 11.6, 0};
}
