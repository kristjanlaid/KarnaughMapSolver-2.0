package project.karnaughmapsolver;

public class LogicHelper {

    public enum GateType {
        NAND,
        XOR,
        NOR
    }

    public static String displaySimplifiedLogicSchema(GateType gateType, int gateCount, String logicExpression) {
        String result = "Simplified Logic Schema: \n" + "Gate Type: " + gateType + "\nGate Count: " + gateCount + "\nLogic Expression: " + logicExpression;
        return result;
    }

    public static String generateLogicSchema(String logicExpression) {
        return logicExpression;
    }

    public static int countGates(int gateCount) {
        return gateCount;
    }

    public static String displayUnsimplifiedLogicSchema(int inputSize, String logicExpression) {
        String result = "Unsimplified Logic Schema:\n" + "Gate Type: AND"+ "\nGate Count: " + inputSize + "\nLogic Expression: " + logicExpression;
        return result;


    }

    public static String displaySimplifiedLogicSchemaByGates(GateType gateType, int gateCount, String logicExpression) {
        String result = "Simplified Logic Schema by Gates:\n" + "Gate Type: " + gateType + "\nGate Count: " + gateCount + "\nLogic Expression: " + logicExpression;
        return result;
    }

}
