package project.karnaughmapsolver;

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

    public ValueSet(int number, int length, char f) {
        this.binary = Integer.toBinaryString(0b1000000 | number).substring(7 - length).toCharArray();
        this.f = f;
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
