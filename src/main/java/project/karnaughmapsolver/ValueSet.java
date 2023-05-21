package project.karnaughmapsolver;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ValueSet implements Comparable<ValueSet> {
    private final char[] binary;
    private char f;
    private List<ValueSet> linkedImplicants = new ArrayList<>();
    private boolean added = false;
    private boolean highlighted = false;
    private boolean clicked = false;
    private String color;
    private int index;

    public ValueSet(int number, int length, char f, int index) {
        this.binary = Integer.toBinaryString(0b10000000 | number).substring(8 - length).toCharArray();
        this.f = f;
        this.index = index;
    }

    public ValueSet(ValueSet valueSet) {
        this.binary = valueSet.getBinary().clone();
        this.f = valueSet.getF();
    }

    public char[] getBinary() {
        return binary;
    }

    public char getF() {
        return f;
    }

    public void setF(char f) {
        this.f = f;
    }

    public boolean isAdded() {
        return added;
    }

    public void setAdded(boolean added) {
        this.added = added;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public boolean isClicked() {
        return clicked;
    }

    public void setClicked(boolean clicked) {
        this.clicked = clicked;
    }

    public List<ValueSet> getLinkedImplicants() {
        return linkedImplicants;
    }

    public void resetLinkedImplicants() {
        this.linkedImplicants = new ArrayList<>();
    }

    private void setBitDoNotMatter(int i) {
        binary[i] = '-';
    }

    public void addImplicant(ValueSet valueSet) {
        linkedImplicants.add(valueSet);
    }

    // methods for TableView value factory
    public char getX0() {
        return binary[0];
    }

    public char getX1() {
        return binary[1];
    }

    public char getX2() {
        return binary[2];
    }

    public char getX3() {
        return binary[3];
    }

    public char getX4() {
        return binary[4];
    }

    public char getX5() {
        return binary[5];
    }

    public char getX6() { return binary[6]; }

    public int getIndex() {
        return Integer.parseInt(String.valueOf(binary), 2);
    }

    public String getFormulaSOP() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < binary.length; i++) {
            if (binary[i] == '0') {
                sb.append('¬');
                sb.append((char) ('A' + i));
            } else if (binary[i] == '1') {
                sb.append((char) ('A' + i));
            }
        }

        if (sb.length() == 0) {
            sb.append(1);
        }

        return sb.toString();
    }

    public String getFormulaPOS() {
        StringBuilder sb = new StringBuilder();

        sb.append("(");
        for (int i = 0; i < binary.length; i++) {
            if (binary[i] == '0') {
                sb.append((char) ('A' + i));
                sb.append("+");
            } else if (binary[i] == '1') {
                sb.append('¬');
                sb.append((char) ('A' + i));
                sb.append("+");
            }
        }
        sb.deleteCharAt(sb.length() - 1);

        if (sb.length() == 0) {
            sb.append(0);
            return sb.toString();
        }

        sb.append(")");

        return sb.toString();
    }

    public String getSimplifiedSOPFormula() {
        String formula = getFormulaSOP();

        // Apply boolean algebra reduction to simplify the formula
        formula = applyBooleanAlgebraReduction(formula);

        return formula;
    }

    public String getSimplifiedPOSFormula() {
        String formula = getFormulaPOS();

        // Apply boolean algebra reduction to simplify the formula
        formula = applyBooleanAlgebraReduction(formula);

        return formula;
    }

    private String applyBooleanAlgebraReduction(String formula) {

        int gateCount = 0;

        for (int i = 0; i < binary.length; i++) {
            char searchchar = (char) ('A' + i);
            String searchcharInverse = String.valueOf('¬' + searchchar);

            // Complementation rule
            if (formula.contains(searchchar + " + " + searchcharInverse) || formula.contains(searchcharInverse + " + " + searchchar)) {
                formula = formula.replace(MessageFormat.format("{0} + {1}", searchchar, searchcharInverse), "1");
                formula = formula.replace(MessageFormat.format("{1} + {0}", searchchar, searchcharInverse), "1");
                gateCount++;
            }

            if (formula.contains(searchchar + "" + searchcharInverse) || formula.contains(searchcharInverse + " + " + searchchar)) {
                formula = formula.replace(MessageFormat.format("{0}{1}", searchchar, searchcharInverse), "0");
                formula = formula.replace(MessageFormat.format("{1}{0}", searchchar, searchcharInverse), "0");
                gateCount++;
            }

            // Identity rule
            if (formula.contains(searchchar + " + " + "0") || formula.contains("0" + " + " + searchchar)) {
                formula = formula.replace(MessageFormat.format("{0} + 0", searchchar), String.valueOf(searchchar));
                formula = formula.replace(MessageFormat.format("0 + {0}", searchchar), "1");
                gateCount++;
            }

            // Null element rule
            if (formula.contains(searchchar + "1") || formula.contains("1" + searchchar)) {
                formula = formula.replace(MessageFormat.format("{0} * 0", searchchar), "0");
                formula = formula.replace(MessageFormat.format("0 * {0}", searchchar), "0");
                gateCount++;
            }

            // Domination rule
            if (formula.contains(searchchar + " + " + searchchar)) {
                formula = formula.replace(MessageFormat.format("{0} + {0}", searchchar), String.valueOf(searchchar));
                gateCount++;
            }

            if (formula.contains(searchchar + " * " + searchchar)) {
                formula = formula.replace(MessageFormat.format("{0} * {0}", searchchar), String.valueOf(searchchar));
                gateCount++;
            }

            // Idempotent rule
            if (formula.contains(searchchar + " + " + searchchar + " + " + searchchar)) {
                formula = formula.replace(MessageFormat.format("{0} + {0} + {0}", searchchar), String.valueOf(searchchar));
                gateCount++;
            }

            if (formula.contains(searchchar + " * " + searchchar + " * " + searchchar)) {
                formula = formula.replace(MessageFormat.format("{0} * {0} * {0}", searchchar), String.valueOf(searchchar));
                gateCount++;
            }

            // Distributive rule
            for (int j = i + 1; j < binary.length; j++) {
                char searchchar2 = (char) ('A' + j);
                String searchchar2Inverse = String.valueOf('¬' + searchchar2);

                if (formula.contains(searchchar + " * (" + searchchar2 + " + " + searchchar2Inverse + ")")) {
                    formula = formula.replace(MessageFormat.format("{0} * ({1} + {2})", searchchar, searchchar2, searchchar2Inverse),
                            MessageFormat.format("{0} * {1} + {0} * {2}", searchchar, searchchar2, searchchar2Inverse));
                    gateCount+=2;
                }

                if (formula.contains("(" + searchchar + " + " + searchcharInverse + ") * " + searchchar2)) {
                    formula = formula.replace(MessageFormat.format("({0} + {1}) * {2}", searchchar, searchcharInverse, searchchar2),
                            MessageFormat.format("{0} * {2} + {1} * {2}", searchchar, searchcharInverse, searchchar2));
                    gateCount+=2;
                }
            }

            // De Morgan's law
            if (formula.contains("(" + searchchar + " + " + searchcharInverse + ")'")) {
                formula = formula.replace(MessageFormat.format("({0} + {1})'", searchchar, searchcharInverse),
                        MessageFormat.format("{0}' * {1}'", searchchar, searchcharInverse));
                gateCount+=2;
            }

            if (formula.contains("(" + searchchar + " * " + searchcharInverse + ")'")) {
                formula = formula.replace(MessageFormat.format("({0} * {1})'", searchchar, searchcharInverse),
                        MessageFormat.format("{0}' + {1}'", searchchar, searchcharInverse));
                gateCount+=2;
            }

        }
        formula += " Gate count: " + gateCount;
        return formula;
    }

    public ValueSet getCombinedImplicant(ValueSet valueSet) {
        int dif = 0;
        int idx = 0;

        for (int i = 0; i < binary.length; i++) {
            if (this.binary[i] != valueSet.getBinary()[i]) {
                dif++;
                idx = i;
            }
        }

        if (dif == 1) {
            ValueSet result = new ValueSet(valueSet);
            result.setBitDoNotMatter(idx);
            return result;
        }

        return null;
    }

    public boolean isSame(ValueSet valueSet) {
        for (int i = 0; i < binary.length; i++) {
            if (this.binary[i] != valueSet.getBinary()[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean contains(ValueSet valueSet) {
        for (int i = 0; i < binary.length; i++) {
            if (this.binary[i] != '-' && binary[i] != valueSet.getBinary()[i]) {
                return false;
            }
        }
        return true;
    }

    public void rotateF() {
        switch (f) {
            case '0' -> f = '1';
            case '1' -> f = '?';
            case '?' -> f = '0';
        }
    }

    @Override
    public int compareTo(ValueSet o) {
        int a = this.linkedImplicants.size();
        int b = o.linkedImplicants.size();
        if (a == b) {
            return Arrays.toString(this.binary).compareTo(Arrays.toString(o.binary));
        }
        return Integer.compare(a, b);
    }

    @Override
    public String toString() {
        return "ValueSet{" +
                "binary=" + Arrays.toString(binary) +
                ", f=" + f +
                ", primeImplicants=" + linkedImplicants.size() +
                '}';
    }
}
