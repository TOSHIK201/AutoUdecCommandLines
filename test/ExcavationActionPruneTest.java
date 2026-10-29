// 删除开挖区域时，对应的开挖动作应一并删除，且可以撤销
public class ExcavationActionPruneTest {
    public static void main(String[] args) {
        ParameterList.syncFromLayers();
        ParameterList.stage4Actions.clear();

        ExcavationLayer r9 = region("9");
        ExcavationLayer r10 = region("10");
        check("region 9 exists", r9 != null);
        check("region 10 exists", r10 != null);

        ParameterList.stage4Actions.add(action(Stage4Geometry.openGroup(r9)));
        ParameterList.stage4Actions.add(action("state_2_" + r9.name));
        ParameterList.stage4Actions.add(action(Stage4Geometry.openGroup(r10)));
        Stage4Action solve = new Stage4Action(Stage4Action.Type.SOLVE);
        ParameterList.stage4Actions.add(solve);
        int before = ParameterList.stage4Actions.size();
        check("start with 4 actions", before == 4);

        UndoManager.push();
        ParameterList.excavationLayers.remove(r9);
        Stage4Sync.removeExcavationActions(r9);

        check("two actions removed (" + ParameterList.stage4Actions.size() + ")",
                ParameterList.stage4Actions.size() == 2);
        check("region 10 action kept", has(Stage4Geometry.openGroup(r10)));
        check("solve kept", ParameterList.stage4Actions.contains(solve));
        check("no leftover for region 9", !has(Stage4Geometry.openGroup(r9))
                && !has("state_2_" + r9.name));

        check("undo available", UndoManager.undo());
        check("actions restored (" + ParameterList.stage4Actions.size() + ")",
                ParameterList.stage4Actions.size() == 4);
        check("region 9 restored", region("9") != null);
        check("region 9 action restored", has(Stage4Geometry.openGroup(r9))
                && has("state_2_" + r9.name));

        System.out.println("EXCAVATION ACTION PRUNE TEST PASSED");
    }

    private static ExcavationLayer region(String name) {
        for (ExcavationLayer e : ParameterList.excavationLayers) {
            if (name.equals(e.name)) return e;
        }
        return null;
    }

    private static Stage4Action action(String group) {
        Stage4Action a = new Stage4Action(Stage4Action.Type.EXCAVATE);
        a.group = group;
        return a;
    }

    private static boolean has(String group) {
        for (Stage4Action a : ParameterList.stage4Actions) {
            if (a.type == Stage4Action.Type.EXCAVATE && group.equals(a.group)) return true;
        }
        return false;
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
