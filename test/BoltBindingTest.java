public class BoltBindingTest {
    public static void main(String[] args) throws Exception {
        ParameterList.syncFromLayers();
        ExcavationLayer coal = ParameterList.excavationLayers.get(0);
        coal.autoGeometry = false;
        coal.direction = Util.Direction.LEFT_TO_RIGHT;
        coal.x1 = 100; coal.x2 = 138;
        coal.y1 = 21.6; coal.y2 = 26.4;

        BoltGroup g = new BoltGroup("T");
        g.x1 = 100; g.y1 = 26.4;
        g.x2 = 108; g.y2 = 26.4;
        g.count = 8;
        g.spacing = 1;
        g.rebuild();
        g.bindName = coal.name;
        g.captureOffsets();
        check("offset x1", Math.abs(g.bx1) < 1e-9);
        check("offset y1", Math.abs(g.by1 - 4.8) < 1e-9);
        check("offset x2", Math.abs(g.bx2 - 8) < 1e-9);
        ParameterList.boltGroups.add(g);

        coal.x1 = 90; coal.x2 = 128;
        ParameterList.refreshBoltBindings();
        check("follow left", Math.abs(g.x1 - 90) < 1e-9);
        check("follow right", Math.abs(g.x2 - 98) < 1e-9);
        check("follow y", Math.abs(g.y1 - 26.4) < 1e-9);
        check("first bolt", Math.abs(g.bolts.get(0).sx - 90.5) < 1e-9);

        coal.direction = Util.Direction.RIGHT_TO_LEFT;
        ParameterList.refreshBoltBindings();
        check("right anchor", Math.abs(g.x1 - 128) < 1e-9);
        check("right anchor end", Math.abs(g.x2 - 136) < 1e-9);

        coal.direction = Util.Direction.LEFT_TO_RIGHT;
        ParameterList.refreshBoltBindings();
        byte[] data = ProjectIO.toBytes();
        g.bindName = "";
        g.x1 = 0;
        ProjectIO.loadBytes(data);

        BoltGroup loaded = null;
        for (BoltGroup item : ParameterList.boltGroups) {
            if ("T".equals(item.name)) loaded = item;
        }
        check("loaded group", loaded != null);
        check("binding persisted", coal.name.equals(loaded.bindName));
        check("loaded position", Math.abs(loaded.x1 - 90) < 1e-9);
        check("loaded bolts", loaded.bolts.size() == 8);
        System.out.println("BOLT BINDING TEST PASSED");
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
