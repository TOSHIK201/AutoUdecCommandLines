public class BoundaryGradientTest {
    public static void main(String[] args) throws Exception {
        BoundaryCondition g = new BoundaryCondition(
                BoundaryCondition.Type.STRESS, 0, 10, 10, 10, 0.2);
        g.syy = -1.0e6;
        g.useXGrad = true;
        g.sxxx = 100;
        g.useYGrad = true;
        g.syyy = 25000;

        String command = g.command();
        check("stress command", command.startsWith("bound stress 0.0000e+00 0.0000e+00 -1.0000e+06"));
        check("xgrad", command.contains("xgrad 1.0000e+02 0.0000e+00 0.0000e+00"));
        check("ygrad", command.contains("ygrad 0.0000e+00 0.0000e+00 2.5000e+04"));
        check("range last", command.endsWith("range 0.00 10.00 9.90 10.10"));

        ParameterList.boundaries.add(g);
        byte[] data = ProjectIO.toBytes();
        ParameterList.boundaries.clear();
        ProjectIO.loadBytes(data);
        BoundaryCondition loaded = ParameterList.boundaries.get(ParameterList.boundaries.size() - 1);
        check("x flag reload", loaded.useXGrad);
        check("y flag reload", loaded.useYGrad);
        check("sxxx reload", Math.abs(loaded.sxxx - 100) < 1e-9);
        check("syyy reload", Math.abs(loaded.syyy - 25000) < 1e-9);

        System.out.println("BOUNDARY GRADIENT TEST PASSED");
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
