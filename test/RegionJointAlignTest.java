public class RegionJointAlignTest {
    public static void main(String[] args) {
        ParameterList.syncFromLayers();
        ParameterList.modelLength = 200.0;
        ExcavationLayer region = ParameterList.excavationLayers.get(0);
        region.autoGeometry = false;
        region.x1 = 71; region.x2 = 129;
        region.y1 = 21.6; region.y2 = 26.4;
        region.cutWidth = 8; region.stepDist = 10;

        double spacing = 0.96;
        double h = Util.layerHeightSpacing(6, spacing);
        double w = Util.layerWidthSpacing(6, spacing);
        System.out.println(String.format(java.util.Locale.ROOT,
                "工程口径：区域宽 58，切眼 8，步距 10，行高 %.4f，列宽 %.4f，长宽比 %.3f", h, w, w / h));

        check("行高", near(h, 0.96, 1e-6));
        check("区域宽度整除列宽", multiple(58, w));
        check("切眼宽度整除列宽", multiple(region.cutWidth, w));
        check("步距整除列宽", multiple(region.stepDist, w));
        check("长宽比", Math.abs(w / h - 1) < 0.10);

        java.util.List<Double> xs = Util.calculateExcavationXList(region);
        for (double x : xs) {
            check("裂隙 x=" + x + " 与竖节理同相", multiple(x - region.x1, w));
        }
        double staggerLeft = w / 2;
        double staggerRight = (58 - staggerLeft) - Math.floor((58 - staggerLeft) / w + 1e-9) * w;
        System.out.println(String.format(java.util.Locale.ROOT,
                "错缝组：左边缘余量 %.4f，右边缘余量 %.4f（列宽 %.4f）", staggerLeft, staggerRight, w));
        check("错缝组右侧不出现薄块", staggerRight >= w * 0.5 - 1e-6 || staggerRight < 1e-4);

        region.cutWidth = 7.5; region.stepDist = 12;
        double w2 = Util.layerWidthSpacing(6, spacing);
        double ratioCut = Math.min(rem(7.5, w2), w2 - rem(7.5, w2)) / w2;
        double ratioStep = Math.min(rem(12, w2), w2 - rem(12, w2)) / w2;
        System.out.println(String.format(java.util.Locale.ROOT,
                "非整步距：列宽 %.4f，切眼残差 %.3f，步距残差 %.3f", w2, ratioCut, ratioStep));
        check("非整步距区域边缘不出现薄块", acceptable(58, w2));
        check("非整步距切片不小于半列", notThin(ratioCut) && notThin(ratioStep));
        check("非整步距长宽比", Math.abs(w2 / h - 1) < 0.10);

        System.out.println("REGION JOINT ALIGN TEST PASSED");
    }

    private static double rem(double v, double s) {
        return v - Math.floor(v / s + 1e-9) * s;
    }

    private static boolean near(double a, double b, double tol) {
        return Math.abs(a - b) < tol;
    }

    private static boolean multiple(double value, double spacing) {
        double r = rem(value, spacing);
        return Math.min(r, spacing - r) < 2e-3;
    }

    private static boolean notThin(double ratio) {
        return ratio <= 1e-3 || ratio >= 0.5 - 1e-6;
    }

    private static boolean acceptable(double value, double spacing) {
        double r = rem(value, spacing);
        double edge = Math.min(r, spacing - r);
        return edge <= 2e-3 || edge >= spacing * 0.5 - 2e-3;
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}