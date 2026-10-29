import java.util.HashSet;
import java.util.Set;

class Stage4Sync {
    // 开挖区域删除后，其开切眼与各步分组对应的开挖动作一并移除
    static void removeExcavationActions(ExcavationLayer e) {
        if (e == null) return;
        Set<String> names = new HashSet<>();
        names.add(Stage4Geometry.openGroup(e));
        java.util.List<Double> xs = Util.calculateExcavationXList(e);
        for (int i = 1; i < xs.size() - 1; i++) {
            names.add("state_" + i + "_" + e.name);
        }
        ParameterList.stage4Actions.removeIf(a ->
                a.type == Stage4Action.Type.EXCAVATE
                        && a.group != null && names.contains(a.group.trim()));
    }

    static void syncBoltActions() {
        Set<String> names = new HashSet<>();
        for (BoltGroup g : ParameterList.boltGroups) names.add(g.name);

        ParameterList.stage4Actions.removeIf(a ->
                a.type == Stage4Action.Type.BOLT
                        && (a.name == null || a.name.trim().isEmpty() || !names.contains(a.name)));

        int insert = -1;
        for (int i = 0; i < ParameterList.stage4Actions.size(); i++) {
            if (ParameterList.stage4Actions.get(i).type == Stage4Action.Type.BOLT) insert = i;
        }
        for (BoltGroup g : ParameterList.boltGroups) {
            boolean exists = false;
            for (Stage4Action a : ParameterList.stage4Actions) {
                if (a.type == Stage4Action.Type.BOLT && g.name.equals(a.name)) {
                    exists = true;
                    break;
                }
            }
            if (exists) continue;
            Stage4Action action = new Stage4Action(Stage4Action.Type.BOLT);
            action.name = g.name;
            if (insert < 0) {
                ParameterList.stage4Actions.add(action);
                insert = ParameterList.stage4Actions.size() - 1;
            } else {
                ParameterList.stage4Actions.add(insert + 1, action);
                insert++;
            }
        }

        int firstBolt = -1;
        int firstMaterial = -1;
        for (int i = 0; i < ParameterList.stage4Actions.size(); i++) {
            Stage4Action a = ParameterList.stage4Actions.get(i);
            if (a.type == Stage4Action.Type.MATERIAL && firstMaterial < 0) firstMaterial = i;
            if (a.type == Stage4Action.Type.BOLT && firstBolt < 0) firstBolt = i;
        }
        if (firstBolt >= 0 && (firstMaterial < 0 || firstMaterial > firstBolt)) {
            Stage4Action material = new Stage4Action(Stage4Action.Type.MATERIAL);
            material.cableMat = ParameterList.cableMaterial();
            material.groutMat = ParameterList.groutMaterial();
            ParameterList.stage4Actions.add(firstBolt, material);
        }
    }
}
