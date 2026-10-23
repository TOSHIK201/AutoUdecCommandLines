public class BoltLayoutTest {
    public static void main(String[] args) {
        BoltGroup g = new BoltGroup("T");
        g.x1 = 0; g.y1 = 0; g.x2 = 10; g.y2 = 0;
        g.count = 5; g.spacing = 2;
        g.rebuild();
        check("count with offset", g.bolts.size() == 5);
        check("first offset", Math.abs(g.bolts.get(0).sx - 1) < 1e-9);
        check("last offset", Math.abs(g.bolts.get(4).sx - 9) < 1e-9);

        g.count = 5; g.spacing = 3;
        g.rebuild();
        check("span exceeds line", Math.abs(g.endOffset) < 1e-9
                && Math.abs(g.bolts.get(4).sx - 12) < 1e-9);
        System.out.println("BOLT LAYOUT PASSED");
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
