public class Stage4SyncTest {
    public static void main(String[] args) {
        for (PlotPreset p : ParameterList.plotPresets4) {
            check("default has no hist plot", !p.selected || p.histCommand == null);
        }
        ParameterList.boltGroups.clear();
        ParameterList.boltGroups.add(group("A", 10, 10, 12, 10));
        ParameterList.boltGroups.add(group("B", 10, 12, 12, 12));
        ParameterList.stage4Actions.clear();
        Stage4Sync.syncBoltActions();
        check("one action per group", boltActions() == ParameterList.boltGroups.size());
        check("group names", actionName(0).equals(ParameterList.boltGroups.get(0).name));

        ParameterList.boltGroups.remove(0);
        Stage4Sync.syncBoltActions();
        check("remove orphan action", boltActions() == ParameterList.boltGroups.size());

        BoltGroup extra = new BoltGroup("EXTRA");
        extra.x1 = 10; extra.y1 = 10; extra.x2 = 12; extra.y2 = 10;
        extra.rebuild();
        ParameterList.boltGroups.add(extra);
        Stage4Sync.syncBoltActions();
        check("add action for new group", boltActions() == ParameterList.boltGroups.size());
        System.out.println("STAGE4 SYNC PASSED");
    }

    private static int boltActions() {
        int n = 0;
        for (Stage4Action a : ParameterList.stage4Actions) {
            if (a.type == Stage4Action.Type.BOLT) n++;
        }
        return n;
    }

    private static BoltGroup group(String name, double x1, double y1, double x2, double y2) {
        BoltGroup g = new BoltGroup(name);
        g.x1 = x1; g.y1 = y1; g.x2 = x2; g.y2 = y2;
        g.spacing = 1.0;
        g.rebuild();
        return g;
    }

    private static String actionName(int index) {
        int n = 0;
        for (Stage4Action a : ParameterList.stage4Actions) {
            if (a.type == Stage4Action.Type.BOLT) {
                if (n == index) return a.name;
                n++;
            }
        }
        return "";
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
