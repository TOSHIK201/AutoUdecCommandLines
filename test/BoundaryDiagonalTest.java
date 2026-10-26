public class BoundaryDiagonalTest {
    public static void main(String[] args) {
        BoundaryCondition d = new BoundaryCondition(
                BoundaryCondition.Type.VELOCITY_X, 0, 0, 10, 10, 0.2);
        d.value = -0.02;
        check("diagonal detected", d.diagonal());
        double[] r = d.range();
        check("axis aligned x", Math.abs(r[0]) < 1e-9 && Math.abs(r[1] - 10) < 1e-9);
        check("axis aligned y", Math.abs((r[3] - r[2]) - 0.2) < 1e-9);
        check("strip range", d.command().contains("range 0.00 10.00 4.90 5.10"));

        BoundaryCondition h = new BoundaryCondition(
                BoundaryCondition.Type.VELOCITY_Y, 0, 0, 10, 0, 0.2);
        check("horizontal not diagonal", !h.diagonal());
        BoundaryCondition v = new BoundaryCondition(
                BoundaryCondition.Type.VELOCITY_X, 0, 0, 0, 10, 0.2);
        check("vertical not diagonal", !v.diagonal());
        BoundaryCondition slight = new BoundaryCondition(
                BoundaryCondition.Type.VELOCITY_X, 0, 0, 10, 1, 0.2);
        check("slight slope is diagonal", slight.diagonal());
        System.out.println("BOUNDARY DIAGONAL TEST PASSED");
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
