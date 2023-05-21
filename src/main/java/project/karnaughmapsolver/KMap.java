package project.karnaughmapsolver;

import project.karnaughmapsolver.LogicHelper.GateType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KMap {
    private final ValueSet[][][] values;
    private List<ValueSet> primeImplicantsSOP;
    private List<ValueSet> primeImplicantsPOS;
    private List<ValueSet> minimalCoverSOP;
    private List<ValueSet> minimalCoverPOS;

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

    public List<ValueSet> getMinimalCoverSOP() {
        return minimalCoverSOP;
    }

    public List<ValueSet> getMinimalCoverPOS() {
        return minimalCoverPOS;
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
            case 7 -> new ValueSet[8][4][4];
            default -> new ValueSet[1][1][1];
        };
    }


    public void findImplicants() {
        primeImplicantsSOP = new ArrayList<>();
        primeImplicantsPOS = new ArrayList<>();
        minimalCoverSOP = new ArrayList<>();
        minimalCoverPOS = new ArrayList<>();
        resetValueSets();

        List<ValueSet> foundImplicants;

        // find all minterms
        foundImplicants = getTerms('1', true);
        // find SOP prime implicants
        while (!foundImplicants.isEmpty()) {
            foundImplicants = findPrimeImplicants(foundImplicants, true);
        }

        // find all maxterms
        foundImplicants = getTerms('0', true);
        // find POS prime implicants
        while (!foundImplicants.isEmpty()) {
            foundImplicants = findPrimeImplicants(foundImplicants, false);
        }

        // find minimal SOP function
        foundImplicants = findEssentialImplicants(findTerms('1'), minimalCoverSOP, primeImplicantsSOP, true);
        foundImplicants = findMinimalCover(findNonEssentialImplicants(true), foundImplicants, true);
        minimalCoverSOP.addAll(foundImplicants);

        // find minimal POS function
        foundImplicants = findEssentialImplicants(findTerms('0'), minimalCoverPOS, primeImplicantsPOS, true);
        foundImplicants = findMinimalCover(findNonEssentialImplicants(false), foundImplicants, false);
        minimalCoverPOS.addAll(foundImplicants);

        Collections.sort(primeImplicantsSOP);
        Collections.reverse(primeImplicantsSOP);
        Collections.sort(primeImplicantsPOS);
        Collections.reverse(primeImplicantsPOS);
        Collections.sort(minimalCoverSOP);
        Collections.reverse(minimalCoverSOP);
        Collections.sort(minimalCoverPOS);
        Collections.reverse(minimalCoverPOS);

        assignColors();
    }


    private void resetValueSets() {
        for (int x = 0; x < sizeX(); x++) {
            for (int y = 0; y < sizeY(); y++) {
                for (int z = 0; z < sizeZ(); z++) {
                    values[x][y][z].resetLinkedImplicants();
                }
            }
        }
    }


    private List<ValueSet> getTerms(char requiredValue, boolean includeDoNotCare) {
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


    private List<ValueSet> findTerms(char requiredValue) {
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


    private List<ValueSet> findPrimeImplicants(List<ValueSet> oldImplicants, boolean isSOP) {
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
        List<ValueSet> essentialPrimeImplicants = (isSOP ? minimalCoverSOP : minimalCoverPOS);

        for (ValueSet essentialPrimeImplicant : essentialPrimeImplicants) {
            primeImplicants.remove(essentialPrimeImplicant);
        }

        return primeImplicants;
    }


    private List<ValueSet> findEssentialImplicants(List<ValueSet> terms, List<ValueSet> essentialImplicants, List<ValueSet> primeImplicants, boolean first) {

        // link terms and implicants on the first call
        if (first) {
            for (ValueSet term : terms) {
                for (ValueSet implicant : primeImplicants) {
                    if (implicant.contains(term)) {
                        term.addImplicant(implicant);
                        implicant.addImplicant(term);
                    }
                }
            }
        }

        Collections.sort(terms);

        List<ValueSet> temp = new ArrayList<>(List.copyOf(terms));
        for (ValueSet term : terms) {
            if (term.getLinkedImplicants().size() == 1) {
                ValueSet implicant = term.getLinkedImplicants().get(0);
                if (!essentialImplicants.contains(implicant)) {
                    essentialImplicants.add(implicant);
                    for (ValueSet valueSet : implicant.getLinkedImplicants()) {
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
            implicant.getLinkedImplicants().removeIf(s -> !terms.contains(s));
        }

        // add implicants until all terms are covered
        while (!terms.isEmpty()) {
            Collections.sort(implicants);
            Collections.reverse(implicants);

            ValueSet implicant = implicants.get(0);
            newPrimeImplicants.add(implicant);

            // remove used terms
            terms.removeIf(s -> implicant.getLinkedImplicants().contains(s));

            // remove all redundant implicants
            for (ValueSet implicant1 : implicants) {
                implicant1.getLinkedImplicants().removeIf(s -> !terms.contains(s));
            }
            implicants.removeIf(s -> s.getLinkedImplicants().size() == 0);

            // remove what is covered by others
            removeCovered(implicants);

            // remove deleted implicants from terms
            for (ValueSet term : terms) {
                term.getLinkedImplicants().removeIf(s -> !implicants.contains(s));
            }
            terms.removeIf(s -> s.getLinkedImplicants().size() == 0);

            //find essential implicants again
            findEssentialImplicants(terms, newPrimeImplicants, implicants, false);

            // remove all redundant implicants
            for (ValueSet implicant1 : implicants) {
                implicant1.getLinkedImplicants().removeIf(s -> !terms.contains(s));
            }
            implicants.removeIf(s -> s.getLinkedImplicants().size() == 0);
        }

        return newPrimeImplicants;
    }

    private void removeCovered(List<ValueSet> implicants) {
        List<ValueSet> temp = new ArrayList<>();

        for (int i = 0; i < implicants.size(); i++) {
            loop:
            for (int j = i + 1; j < implicants.size(); j++) {
                for (ValueSet term : implicants.get(i).getLinkedImplicants()) {
                    if (!implicants.get(j).getLinkedImplicants().contains(term)) {
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

    public void printValueSetMethods(ValueSet valueSet) {
        // 1) Display simplified logic schema
        valueSet.displaySimplifiedLogicSchema();

        // 2) Display unsimplified logic schema
        valueSet.displayUnsimplifiedLogicSchema();

        // 3) Display simplified logic schema by gates
        valueSet.displaySimplifiedLogicSchemaByGates();

        // 4) Generate simplified logic schema
        String simplifiedLogicSchema = valueSet.generateLogicSchema();
        System.out.println("Generated Simplified Logic Schema: " + simplifiedLogicSchema);

        // 5) Count gates
        int gateCount = valueSet.countGates();
        System.out.println("Gate Count: " + gateCount);

        // 6) Generate simplified logic schema by gates
        String simplifiedLogicSchemaByGates = valueSet.generateLogicSchemaByGates();
        System.out.println("Generated Simplified Logic Schema by Gates: " + simplifiedLogicSchemaByGates);

        // 7) Count gates by type
        GateType gateType = GateType.NAND; // Replace with desired gate type
        int gateCountByType = valueSet.countGatesByType(gateType);
        System.out.println("Gate Count for Gate Type " + gateType + ": " + gateCountByType);
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
