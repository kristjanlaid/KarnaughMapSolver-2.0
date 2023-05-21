package project.karnaughmapsolver;

public class LogicHelper {

    public enum GateType {
        NAND,
        XOR,
        NOR
    }

    public static void displaySimplifiedLogicSchema(GateType gateType, int gateCount, String logicExpression) {
        System.out.println("Simplified Logic Schema:");
        System.out.println("Gate Type: " + gateType);
        System.out.println("Gate Count: " + gateCount);
        System.out.println("Logic Expression: " + logicExpression);
    }

    public static String generateLogicSchema(String logicExpression) {
        return logicExpression;
    }

    public static int countGates(int gateCount) {
        return gateCount;
    }

    public static void displayUnsimplifiedLogicSchema(int inputSize, String logicExpression) {
        System.out.println("Unsimplified Logic Schema:");
        System.out.println("Gate Type: AND");
        System.out.println("Gate Count: " + inputSize);
        System.out.println("Logic Expression: " + logicExpression);
    }

    public static void displaySimplifiedLogicSchemaByGates(GateType gateType, int gateCount, String logicExpression) {
        System.out.println("Simplified Logic Schema by Gates:");
        System.out.println("Gate Type: " + gateType);
        System.out.println("Gate Count: " + gateCount);
        System.out.println("Logic Expression: " + logicExpression);
    }

    public static String generateLogicSchemaByGates(String logicExpression) {
        return logicExpression;
    }

    public static int countGatesByType(GateType gateType, int gateCount) {
        if (gateType == gateType) {
            return gateCount;
        } else {
            return 0;
        }
    }

}
