package eu.ha3.presencefootsteps.world;

public interface Emitter {
    String UNASSIGNED = "UNASSIGNED";
    String NOT_EMITTER = "NOT_EMITTER";
    String MESSY_GROUND = "MESSY_GROUND";

    static boolean isNonEmitter(String association) {
        return NOT_EMITTER.equals(association);
    }

    static boolean isResult(String association) {
        return !UNASSIGNED.equals(association);
    }

    static boolean isEmitter(String association) {
        return isResult(association) && !isNonEmitter(association);
    }

    static String combine(String a, String b) {
        if (isEmitter(b)) {
            return a + "," + b;
        }
        return a;
    }
}
