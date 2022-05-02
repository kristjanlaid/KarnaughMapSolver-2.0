package project.karnaughmapsolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KMap {
    private final ValueSet[][][] values;
    private List<ValueSet> primeImplicantsSOP;
    private List<ValueSet> primeImplicantsPOS;
    private List<ValueSet> essentialPrimeImplicantsSOP;
    private List<ValueSet> essentialPrimeImplicantsPOS;

    public KMap(List<ValueSet> valueSets, int var) {
        values = getMap(var);

        for (int x = 0; x < sizeX(); x++) {
            for (int y = 0; y < sizeY(); y++) {
                for (int z = 0; z < sizeZ(); z++) {
                    values[x][y][z] = valueSets.get(y + x * sizeY() + z * sizeX() * sizeY());
                }
            }
        }

        // correcting kmap indexes
        if (sizeX() == 4) {
            ValueSet[][] temp = values[3];
            values[3] = values[2];
            values[2] = temp;
        }

        if (sizeY() == 4) {
            for (int i = 0; i < sizeX(); i++) {
                ValueSet[] temp = values[i][3];
                values[i][3] = values[i][2];
                values[i][2] = temp;
            }
        }

        if (sizeZ() == 4) {
            for (int i = 0; i < sizeX(); i++) {
                for (int j = 0; j < sizeY(); j++) {
                    ValueSet temp = values[i][j][3];
                    values[i][j][3] = values[i][j][2];
                    values[i][j][2] = temp;
                }
            }
        }
    }

    public List<ValueSet> getPrimeImplicantsSOP() {
        return primeImplicantsSOP;
    }

    public List<ValueSet> getPrimeImplicantsPOS() {
        return primeImplicantsPOS;
    }

    public List<ValueSet> getEssentialPrimeImplicantsSOP() {
        return essentialPrimeImplicantsSOP;
    }

    public List<ValueSet> getEssentialPrimeImplicantsPOS() {
        return essentialPrimeImplicantsPOS;
    }

    public ValueSet getValue(int x, int y, int z) {
        return values[x][y][z];
    }

    public int sizeX() {
        return values.length;
    }

    public int sizeY() {
        return values[0].length;
    }

    public int sizeZ() {
        return values[0][0].length;
    }

    private ValueSet[][][] getMap(int variables) {
        return switch (variables) {
            case 2 -> new ValueSet[2][2][1];
            case 3 -> new ValueSet[4][2][1];
            case 4 -> new ValueSet[4][4][1];
            case 5 -> new ValueSet[4][4][2];
            case 6 -> new ValueSet[4][4][4];
            default -> new ValueSet[1][1][1];
        };
    }


    public void findPrimeImplicants() {
        primeImplicantsSOP = new ArrayList<>();
        primeImplicantsPOS = new ArrayList<>();
        essentialPrimeImplicantsSOP = new ArrayList<>();
        essentialPrimeImplicantsPOS = new ArrayList<>();
        resetValueSets();

        List<ValueSet> foundImplicants;

        foundImplicants = findTerms('1', true);
        while (!foundImplicants.isEmpty()) {
            foundImplicants = findImplicants(foundImplicants, true);
        }

        foundImplicants = findTerms('0', true);
        while (!foundImplicants.isEmpty()) {
            foundImplicants = findImplicants(foundImplicants, false);
        }

        //foundImplicants = findEssentialImplicants(true);
        foundImplicants = findEssentialImplicants(allValueSets('1'), essentialPrimeImplicantsSOP, primeImplicantsSOP, true);
        foundImplicants = findMinimalCover(findNonEssentialImplicants(true), foundImplicants, true);
        essentialPrimeImplicantsSOP.addAll(foundImplicants);

        //foundImplicants = findEssentialImplicants(false);
        foundImplicants = findEssentialImplicants(allValueSets('0'), essentialPrimeImplicantsPOS, primeImplicantsPOS, true);
        foundImplicants = findMinimalCover(findNonEssentialImplicants(false), foundImplicants, false);
        essentialPrimeImplicantsPOS.addAll(foundImplicants);

        Collections.sort(primeImplicantsSOP);
        Collections.reverse(primeImplicantsSOP);
        Collections.sort(primeImplicantsPOS);
        Collections.reverse(primeImplicantsPOS);
        Collections.sort(essentialPrimeImplicantsSOP);
        Collections.reverse(essentialPrimeImplicantsSOP);
        Collections.sort(essentialPrimeImplicantsPOS);
        Collections.reverse(essentialPrimeImplicantsPOS);

        assignColors();
    }


    private void resetValueSets() {
        for (int x = 0; x < sizeX(); x++) {
            for (int y = 0; y < sizeY(); y++) {
                for (int z = 0; z < sizeZ(); z++) {
                    values[x][y][z].resetPrimeImplicants();
                }
            }
        }
    }


    private List<ValueSet> findTerms(char requiredValue, boolean includeDoNotCare) {
        List<ValueSet> valueSets = new ArrayList<>();

        for (int x = 0; x < sizeX(); x++) {
            for (int y = 0; y < sizeY(); y++) {
                for (int z = 0; z < sizeZ(); z++) {
                    if (getValue(x, y, z).getF() == requiredValue || (includeDoNotCare && getValue(x, y, z).getF() == '?')) {
                        valueSets.add(new ValueSet(getValue(x, y, z)));
                    }
                }
            }
        }

        return valueSets;
    }


    private List<ValueSet> allValueSets(char requiredValue) {
        List<ValueSet> valueSets = new ArrayList<>();

        for (int x = 0; x < sizeX(); x++) {
            for (int y = 0; y < sizeY(); y++) {
                for (int z = 0; z < sizeZ(); z++) {
                    if (getValue(x, y, z).getF() == requiredValue) {
                        valueSets.add(getValue(x, y, z));
                    }
                }
            }
        }

        return valueSets;
    }


    private List<ValueSet> findImplicants(List<ValueSet> oldImplicants, boolean isSOP) {
        List<ValueSet> foundImplicants = new ArrayList<>();

        for (int i = 0; i < oldImplicants.size(); i++) {
            boolean notFound = true;
            ValueSet oldValueSet = oldImplicants.get(i);
            for (int j = i + 1; j < oldImplicants.size(); j++) {
                ValueSet oldValueSet2 = oldImplicants.get(j);
                ValueSet newValueSet = oldValueSet.getCombinedImplicant(oldValueSet2);
                if (newValueSet != null) {
                    oldValueSet2.setAdded(true);
                    notFound = false;
                    if (!contains(foundImplicants, newValueSet)) {
                        foundImplicants.add(newValueSet);
                    }
                }
            }

            if (notFound && !oldValueSet.isAdded()) {
                if (isSOP) {
                    primeImplicantsSOP.add(oldValueSet);
                } else {
                    primeImplicantsPOS.add(oldValueSet);
                }
            }
        }

        return foundImplicants;
    }


    private List<ValueSet> findNonEssentialImplicants(boolean isSOP) {
        List<ValueSet> primeImplicants = new ArrayList<>(isSOP ? primeImplicantsSOP : primeImplicantsPOS);
        List<ValueSet> essentialPrimeImplicants = (isSOP ? essentialPrimeImplicantsSOP : essentialPrimeImplicantsPOS);

        for (ValueSet essentialPrimeImplicant : essentialPrimeImplicants) {
            primeImplicants.remove(essentialPrimeImplicant);
        }

        return primeImplicants;
    }


    private List<ValueSet> findEssentialImplicants(List<ValueSet> terms, List<ValueSet> essentialImplicants, List<ValueSet> primeImplicants, boolean first) {

        if (first) {
            for (ValueSet term : terms) {
                for (ValueSet valueSet : primeImplicants) {
                    if (valueSet.contains(term)) {
                        term.addImplicant(valueSet);
                        valueSet.addImplicant(term);
                    }
                }
            }
        }

        Collections.sort(terms);

        List<ValueSet> temp = new ArrayList<>(List.copyOf(terms));
        for (ValueSet term : terms) {
            if (term.getPrimeImplicants().size() == 1) {
                ValueSet implicant = term.getPrimeImplicants().get(0);
                if (!essentialImplicants.contains(implicant)) {
                    essentialImplicants.add(implicant);
                    for (ValueSet valueSet : implicant.getPrimeImplicants()) {
                        temp.remove(valueSet);
                    }
                }
            } else {
                break;
            }
        }

        terms.removeIf(s -> !temp.contains(s));

        return terms;
    }

    private List<ValueSet> findMinimalCover(List<ValueSet> implicants, List<ValueSet> terms, boolean isSOP) {
        List<ValueSet> newPrimeImplicants = new ArrayList<>();

        // remove all implicants with terms that are covered
        for (ValueSet implicant : implicants) {
            implicant.getPrimeImplicants().removeIf(s -> !terms.contains(s));
        }

        // add implicants until all terms are covered
        while (!terms.isEmpty()) {
            Collections.sort(implicants);
            Collections.reverse(implicants);

            //test
//            System.out.println("implikandid: ");
//            for (ValueSet implicant : implicants) {
//                System.out.println(implicant);
//            }
//            System.out.println("termid: ");
//            for (ValueSet implicant : terms) {
//                System.out.println(implicant);
//            }
//            System.out.println("-------------------------------------------");

            ValueSet implicant = implicants.get(0);
            newPrimeImplicants.add(implicant);

            // remove used terms
            terms.removeIf(s -> implicant.getPrimeImplicants().contains(s));

            // remove all redundant implicants
            for (ValueSet implicant1 : implicants) {
                implicant1.getPrimeImplicants().removeIf(s -> !terms.contains(s));
            }
            implicants.removeIf(s -> s.getPrimeImplicants().size() == 0);

            //test
//            System.out.println("after 1 removed:");
//            System.out.println("implikandid: ");
//            for (ValueSet implicant1 : implicants) {
//                System.out.println(implicant1);
//            }
//            System.out.println("termid: ");
//            for (ValueSet implicant1 : terms) {
//                System.out.println(implicant1);
//            }
//            System.out.println("-------------------------------------------");

            // remove what is covered by others
            //implicants.removeIf(s -> isCovered(implicants, s));
            removeCovered(implicants);

            // remove deleted implicants from terms
            for (ValueSet term : terms) {
                term.getPrimeImplicants().removeIf(s -> !implicants.contains(s));
            }
            terms.removeIf(s -> s.getPrimeImplicants().size() == 0);

            //test
//            System.out.println("after covered by others removed:");
//            System.out.println("implikandid: ");
//            for (ValueSet implicant1 : implicants) {
//                System.out.println(implicant1);
//            }
//            System.out.println("termid: ");
//            for (ValueSet implicant1 : terms) {
//                System.out.println(implicant1);
//            }
//            System.out.println("-------------------------------------------");

            //find essential implicants again
            findEssentialImplicants(terms, newPrimeImplicants, implicants, false);

            // remove all redundant implicants
            for (ValueSet implicant1 : implicants) {
                implicant1.getPrimeImplicants().removeIf(s -> !terms.contains(s));
            }
            implicants.removeIf(s -> s.getPrimeImplicants().size() == 0);
//            System.out.println("\n*******************************************\n");
        }

        return newPrimeImplicants;
    }

    private void removeCovered(List<ValueSet> implicants) {
        List<ValueSet> temp = new ArrayList<>();

        for (int i = 0; i < implicants.size(); i++) {
            loop:
            for (int j = i + 1; j < implicants.size(); j++) {
                for (ValueSet term : implicants.get(i).getPrimeImplicants()) {
                    if (!implicants.get(j).getPrimeImplicants().contains(term)) {
                        continue loop;
                    }
                }
                temp.add(implicants.get(i));
            }
        }

        implicants.removeIf(temp::contains);

    }

    private void assignColors() {
        String[] colors = {"A93226", "2471A3", "229954",
                "BA4A00", "884EA0", "17A589", "D4AC0D",
                "2E4053", "641E16", "1B4F72", "145A32",
                "6E2C00", "512E5F", "0E6251", "7D6608"};

        for (int i = 0; i < primeImplicantsPOS.size(); i++) {
            primeImplicantsPOS.get(i).setColor(colors[i % 15]);
        }

        for (int i = 0; i < primeImplicantsSOP.size(); i++) {
            primeImplicantsSOP.get(i).setColor(colors[i % 15]);
        }
    }

    private boolean contains(List<ValueSet> foundImplicants, ValueSet newValueSet) {
        for (ValueSet implicant : foundImplicants) {
            if (implicant.isSame(newValueSet)) {
                return true;
            }
        }
        return false;
    }

    // for testing
    public void printKMap(boolean printIndexes) {
        for (int z = 0; z < sizeZ(); z++) {
            for (int y = 0; y < sizeY(); y++) {
                for (int x = 0; x < sizeX(); x++) {
                    if (printIndexes)
                        System.out.print(values[x][y][z].getIndex() + " ");
                    else
                        System.out.print(values[x][y][z].getF() + " ");
                }
                System.out.println();
            }
            System.out.println();
            System.out.println();
        }
    }
}
