package project.karnaughmapsolver;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.util.*;

public class MainViewController implements Initializable {

    private KMap kMap;
    String letters = "ABCDEFG";

    @FXML
    private TableView<ValueSet> truthTable;
    @FXML
    private ChoiceBox<Integer> variablesChoiceBox;
    @FXML
    private Slider spacingSlider;
    @FXML
    private Slider textOpacitySlider;
    @FXML
    private Slider cellOpacitySlider;
    @FXML
    private Slider focusSlider;
    @FXML
    public GridPane extraSettings;
    @FXML
    public CheckBox show3D;
    @FXML
    private MenuItem empty;
    @FXML
    private MenuItem exit;
    @FXML
    private MenuItem about;
    @FXML
    private MenuItem help;
    @FXML
    private CheckMenuItem showOnlyRelevant;
    @FXML
    private CheckMenuItem showIndexes;
    @FXML
    private RadioMenuItem initZeros;
    @FXML
    private RadioMenuItem initOnes;
    @FXML
    private RadioMenuItem initDontCares;
    @FXML
    private RadioMenuItem initRandom;
    @FXML
    public AnchorPane SOPParent;
    @FXML
    public AnchorPane POSParent;
    @FXML
    public Canvas canvasPOS;
    @FXML
    public Canvas canvasSOP;
    @FXML
    private TextFlow solutionTextPOS;
    @FXML
    private TextFlow solutionTextSOP;
    @FXML
    private Button inputButton;

    private CubeModelBuilder cubeModelBuilderSOP;
    private CubeModelBuilder cubeModelBuilderPOS;

    @FXML
    private void allSetZeroClicked() {
        setAll('0');
    }

    @FXML
    private void allSetOneClicked() {
        setAll('1');
    }

    @FXML
    private void allSetDoNotCareClicked() {
        setAll('?');
    }

    @FXML
    private void allSetRandomizedClicked() {
        Random rand = new Random();
        char rand_int = (char)(rand.nextInt(2)+'0');
        setRandom(rand_int);
    }

    @FXML
    private void solveButtonClicked() {
        solve();
    }

    @FXML
    private void reset3DModel(){
        if (show3D.isSelected()){
            cubeModelBuilderSOP.resetRotation();
            cubeModelBuilderPOS.resetRotation();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initMenuItems();
        initTruthTable();
    }

    public void updateKMap() {
        kMap = new KMap(truthTable.getItems(), variablesChoiceBox.getValue());
    }

    private void setAll(char c) {
        if (!truthTable.getItems().isEmpty()) {
            for (ValueSet item : truthTable.getItems()) {
                item.setF(c);
            }
            truthTable.refresh();
            updateKMap();
            updateCanvas();
        }
    }

    private void setRandom(char c) {
        if (!truthTable.getItems().isEmpty()) {
            for (ValueSet item : truthTable.getItems()) {
                Random rand = new Random();
                char rand_int = (char)(rand.nextInt(2)+'0');
                item.setF(rand_int);
            }
            truthTable.refresh();
            updateKMap();
            updateCanvas();
        }
    }

    private void initTruthTable() {
        truthTable.setOnMouseClicked(event -> {
            ValueSet focusedItem = truthTable.getFocusModel().getFocusedItem();
            if (focusedItem != null && truthTable.getFocusModel().getFocusedCell().getColumn() == variablesChoiceBox.getValue() + 1) {
                focusedItem.rotateF();
                updateKMap();
                updateCanvas();
                truthTable.refresh();
            }
        });
    }

    private void initMenuItems() {
        variablesChoiceBox.getItems().addAll(2, 3, 4, 5, 6);
        variablesChoiceBox.setOnAction(event -> {
            variableChange();
            if (variablesChoiceBox.getValue() > 5) {
                focusSlider.setMax(3);
            } else {
                focusSlider.setMax(1);
            }
        });
        showIndexes.setOnAction(event -> updateCanvas());
        showOnlyRelevant.setOnAction(event -> updateCanvas());
        show3D.setOnAction(event -> {
            updateCanvas();
            if (!show3D.isSelected()) {
                SOPParent.getChildren().remove(1);
                POSParent.getChildren().remove(1);
            }
            extraSettings.setDisable(!show3D.isSelected());
        });
        exit.setOnAction(event -> System.exit(0));

        ToggleGroup tg = new ToggleGroup();
        initZeros.setToggleGroup(tg);
        initOnes.setToggleGroup(tg);
        initDontCares.setToggleGroup(tg);
        initRandom.setToggleGroup(tg);
        initZeros.setSelected(true);

        spacingSlider.setMin(2);
        spacingSlider.setMax(50);
        spacingSlider.setValue(15);
        spacingSlider.setOnMouseDragged(event -> updateCanvas());

        textOpacitySlider.setMin(0);
        textOpacitySlider.setMax(10);
        textOpacitySlider.setValue(5);
        textOpacitySlider.setOnMouseDragged(event -> updateCanvas());

        cellOpacitySlider.setMin(3);
        cellOpacitySlider.setMax(10);
        cellOpacitySlider.setValue(8);
        cellOpacitySlider.setOnMouseDragged(event -> updateCanvas());

        focusSlider.setMin(0);
        focusSlider.setMax(1);
        focusSlider.setValue(0);
        focusSlider.setBlockIncrement(1);
        focusSlider.setMajorTickUnit(1);
        focusSlider.setMinorTickCount(0);
        focusSlider.setSnapToTicks(true);
        focusSlider.setShowTickMarks(true);
        focusSlider.setOnMouseDragged(event -> updateCanvas());
        focusSlider.setOnMouseReleased(event -> updateCanvas());

        Integer choice = variablesChoiceBox.getValue();

        inputButton.setDisable(choice == null);

        empty.setOnAction(event -> {
            variablesChoiceBox.setOnAction(event1 -> {
            });
            variablesChoiceBox.getSelectionModel().clearSelection();
            variablesChoiceBox.setOnAction(event1 -> {
                variableChange();
                if (variablesChoiceBox.getValue() > 5) {
                    focusSlider.setMax(3);
                } else {
                    focusSlider.setMax(1);
                }
            });
            truthTable.getItems().clear();
            truthTable.refresh();
            canvasPOS.getGraphicsContext2D().clearRect(0, 0, canvasPOS.getWidth(), canvasPOS.getHeight());
            canvasSOP.getGraphicsContext2D().clearRect(0, 0, canvasSOP.getWidth(), canvasSOP.getHeight());
            solutionTextSOP.getChildren().clear();
            solutionTextPOS.getChildren().clear();
            extraSettings.setDisable(true);
            show3D.setDisable(true);
        });
        about.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.NONE, "", ButtonType.OK);
            alert.setTitle("About");
            String content = """
                    About the authors
                        
                    This is an application was first developed by Kaarel Kütt in 2022 for simplifying boolean functions using Karnaugh Map method.
                    
                    In 2023 the application was further developed by Kristjan Laid with the addition of 3D model of the karnaugh Map method
                    
                    as well as adding new variable, optimizing code, schemes presented with only NAND and NORs and other developments
                    """;
            TextArea area = new TextArea(content);
            area.setWrapText(true);
            area.setEditable(false);

            alert.getDialogPane().setContent(area);
            alert.setResizable(true);
            alert.showAndWait();
        });
        help.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.NONE, "", ButtonType.OK);
            alert.setTitle("Help");
            String content = """
                    What is a Karnaugh Map

                    Karnaugh Map is a a method used for simplifying Boolean algebra expressions. It was first intoduced by Maurice Karnaugh in 1953
                    
                    as a refined method of Veitch's chart which was itself a rediscovery of Marquand diagram. Karnaugh maps are also known as 

                    Karnaugh-Veitch maps.

                    How to use this application

                    Step 1) Choose the number of variables

                    Step 2) Either use Set all 0, Set all 1 or Set all ? or you can also manually set each row y value by clicking on them.

                    Step 3) From settings you can toggle indexes and showing only relevant values to declutter the view.

                    Step 4) Click on Solve and the result will show up in the bottom of the window.

                    Step 5) Check Show in 3D to see the Karnaugh Map in 3d view which can be panned and rotated in all directions.        
                    """;
            TextArea area = new TextArea(content);
            area.setWrapText(true);
            area.setEditable(false);

            alert.getDialogPane().setContent(area);
            alert.setResizable(true);
            alert.showAndWait();
        });

        inputButton.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Enter input names");
            Label info = new Label("Input names must be inserted without spaces\nand as many variables you want,\none char counts as one variable.\nThe variable choice will be changed to\nhow many variables you enter.");
            info.setWrapText(true);
            dialog.getDialogPane().setHeader(info);

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(string -> {
                if (letters.length() == variablesChoiceBox.getValue()) {
                    letters = string;
                    variableChange();
                } else if (string.length() == 0) {
                    letters = "ABCDEFG";
                } else {
                    variablesChoiceBox.setValue(string.length());
                    variableChange();
                }

            });
        });
    }

    private void variableChange() {
        show3D.setDisable(variablesChoiceBox.getValue() < 5);
        extraSettings.setDisable(variablesChoiceBox.getValue() < 5 || !show3D.isSelected());
        focusSlider.setValue(0);

        Integer choice = variablesChoiceBox.getValue();
        inputButton.setDisable(choice == null);

        loadTruthTable();
        updateKMap();
        updateCanvas();
    }

    private void loadTruthTable() {
        int numberOfVariables = variablesChoiceBox.getValue();
        int numberOfRows = (int) Math.pow(2, numberOfVariables);

        truthTable.getItems().clear();
        truthTable.getColumns().clear();

        // Add index column
        TableColumn<ValueSet, Integer> indexColumn = new TableColumn<>("i");
        indexColumn.setCellValueFactory(new PropertyValueFactory<>("index"));
        indexColumn.setPrefWidth(20);
        indexColumn.setResizable(false);
        indexColumn.setStyle("-fx-font-weight: bold;-fx-font-size: 10px;");
        truthTable.getColumns().add(indexColumn);

        // Add colums according to number of variables
        for (int i = 0; i < numberOfVariables; i++) {
            TableColumn<ValueSet, String> column = new TableColumn<>(String.valueOf((char) ('A' + i)));
            column.setCellValueFactory(new PropertyValueFactory<>("x" + i));
            column.setPrefWidth(20);
            column.setSortable(false);
            column.setResizable(false);
            truthTable.getColumns().add(column);
        }
        TableColumn<ValueSet, String> column = new TableColumn<>("y");
        column.setCellValueFactory(new PropertyValueFactory<>("f"));
        column.setPrefWidth(30);
        column.setResizable(false);
        column.setStyle("-fx-font-weight: bold;");
        truthTable.getColumns().add(column);

        // Add listeners to tablerows
        truthTable.setRowFactory(s -> {
            TableRow<ValueSet> row = new TableRow<>();
            row.setOnMouseEntered(event -> {
                if (row.getItem() != null) {
                    truthTable.getItems().forEach(v -> v.setClicked(false));
                    row.getItem().setClicked(true);
                    updateCanvas();
                }
            });
            row.setOnMouseExited(event -> {
                if (row.getItem() != null) {
                    row.getItem().setClicked(false);
                    updateCanvas();
                }
            });

            return row;
        });

        // Add truth table values
        //int numberOfRows = (int) Math.pow(2, numberOfVariables);
        int idx = 0;
        for (int i = 0; i < numberOfRows; i++) {
            char initValue;
            if (initZeros.isSelected()) {
                initValue = '0';
            } else if (initOnes.isSelected()) {
                initValue = '1';
            } else if (initRandom.isSelected()) {
                Random rand = new Random();
                char rand_int = (char)(rand.nextInt(2)+'0');
                initValue = rand_int;
            } else {
                initValue = '?';
            }
            ValueSet valueSet = new ValueSet(i, numberOfVariables, initValue, idx);
            truthTable.getItems().add(valueSet);
            idx++;
        }

        truthTable.refresh();
    }

    public void updateCanvas() {
        if (truthTable.getItems().isEmpty()) {
            return;
        }

        if (show3D.isSelected() && SOPParent.getChildren().size() <= 1 && POSParent.getChildren().size() <= 1) {
            int numberOfVariables = variablesChoiceBox.getValue();

            Group groupSOP = new Group();
            Group groupPOS = new Group();

            PerspectiveCamera perspectiveCameraSOP = new PerspectiveCamera(true);
            PerspectiveCamera perspectiveCameraPOS = new PerspectiveCamera(true);

            cubeModelBuilderSOP = new CubeModelBuilder(groupSOP, perspectiveCameraSOP, numberOfVariables, kMap, "SOP");
            SubScene subSceneSOP = cubeModelBuilderSOP.createScene();

            cubeModelBuilderPOS = new CubeModelBuilder(groupPOS, perspectiveCameraPOS, numberOfVariables, kMap, "POS");
            SubScene subScenePOS = cubeModelBuilderPOS.createScene();

            // canvasSOP.visibleProperty().bind(show3D.selectedProperty().not());
            // canvasPOS.visibleProperty().bind(show3D.selectedProperty().not());
            SOPParent.getChildren().add(subSceneSOP);
            POSParent.getChildren().add(subScenePOS);

        } else if (show3D.isSelected() && SOPParent.getChildren().size() >= 2 && POSParent.getChildren().size() >= 2) {
            cubeModelBuilderSOP.refreshCubeValues();
            cubeModelBuilderPOS.refreshCubeValues();
        }

        GraphicsContext[] graphicsContexts = {canvasPOS.getGraphicsContext2D(), canvasSOP.getGraphicsContext2D()};
        int rectSize = 35; //3D view rectangle size for each value

        for (GraphicsContext context : graphicsContexts) {
            context.clearRect(0, 0, context.getCanvas().getWidth(), context.getCanvas().getHeight());
            context.setLineWidth(2);

            for (int z = kMap.sizeZ() - 1; z >= 0; z--) {

                if (!show3D.isSelected()) {
                    context.setStroke(Color.web("444444"));
                    context.setFill(Color.web("444444"));

                    int xOffset = z * 2 * rectSize;
                    int yOffset = 0;

                    for (int x = 0; x < kMap.sizeX(); x++) {
                        int _x = x;
                        if (z == 1 && kMap.sizeZ() == 2) {
                            _x = kMap.sizeX() - x - 1;
                            xOffset = 4 * rectSize;
                        }
                        if (kMap.sizeZ() == 4) {
                            _x = (z % 2 == 1) ? (1 - (x / 2)) : (x / 2);
                            yOffset = (x < 2) ? (x % 2 * 4 * rectSize) : ((4 * rectSize) - x % 2 * 4 * rectSize);
                        }
                        for (int y = 0; y < kMap.sizeY(); y++) {
                            int _y = y;
                            if (kMap.sizeZ() == 4 && (x == 1 || x == 2)) {
                                _y = kMap.sizeY() - 1 - y;
                            }
                            drawElement(_x, _y, xOffset, yOffset, rectSize, kMap.getValue(x, y, z), context);
                        }
                    }
                }
            }

            drawHeaders(context, rectSize);
        }
    }

    private void drawHeaders(GraphicsContext context, int rectSize) {
        int variables = variablesChoiceBox.getValue();
        context.setFont(new Font(15));
        context.setFill(Color.web("444444"));
        context.strokeLine(20, 10, 50, 40);
        if (show3D.isSelected() && variables > 4) {
            context.strokeLine(80 + 4 * rectSize, 10, 50 + 4 * rectSize, 40);
        }


        String[][] labels = {{"0", "1"},
                                {"00", "01", "11", "10"},
                                {"000", "001", "011", "010", "110", "111", "101", "100"},
                                {"111"},
                                {"0000", "0001", "0011", "0010",
                                 "0110", "0111", "0101", "0100",
                                 "1100", "1101", "1111", "1110",
                                 "1010", "1011", "1001", "1000"}};

        int charIndex = 0;

        //z axle
        int limZ = (show3D.isSelected() && variables > 4) ? ((variables > 5) ? 4 : 2) : 0;
        context.fillText(letters.substring(charIndex, charIndex + limZ / 2), 80 + 4 * rectSize, 25);
        charIndex = charIndex + limZ / 2;
        for (int i = 0; i < limZ; i++) {
            context.fillText(labels[limZ / 4][i], 63 + 4 * rectSize + i * Math.max(spacingSlider.getValue(), 12), 43 + i * Math.max(spacingSlider.getValue(), 12));
        }

        //x axle
        int limX = (!show3D.isSelected() && variables > 6) ? 16 : ((!show3D.isSelected() && variables > 4) ? 8 : ((variables > 2) ? 4 : 2));
        if (limX == 16) {
            context.fillText(letters.substring(charIndex, 4), 30, 12);
        } else {
            context.fillText(letters.substring(charIndex, charIndex + limX / 4 + 1), 30, 12);
        }
        charIndex = charIndex + limX / 4 + 1;
        for (int i = 0; i < limX; i++) {
            context.fillText(labels[limX / 4][i], 62 + i * rectSize - limX / 4 * 3, 35);
        }

        //y axle
        int limY = (!show3D.isSelected() && variables > 5) ? 8 : ((variables > 3) ? 4 : 2);
        if (limY == 8 && limX == 16) {
            context.fillText(letters.substring(4), 10 - limY, 35);
        } else {
            context.fillText(letters.substring(charIndex, charIndex + limY / 4 + 1), 10 - limY, 35);
        }
        for (int i = 0; i < limY; i++) {
            context.fillText(labels[limY / 4][i], 30 - limY / 4 * 3, 62 + i * rectSize);
        }
    }

    private void drawElement(int x, int y, int xOffset, int yOffset, int rectSize, ValueSet valueSet, GraphicsContext context) {
        Paint paint = context.getFill();

        List<ValueSet> implicants = getAllValueImplicants(valueSet, context);
        for (ValueSet implicant : implicants) {
            double opacity = implicant.isHighlighted() ? 0.9 : (13 - cellOpacitySlider.getValue()) / 10 * 0.2;
            context.setFill(Color.web(implicant.getColor(), opacity));
            context.fillRect(50 + xOffset + x * rectSize, 40 + yOffset + y * rectSize, rectSize, rectSize);

        }

        if (valueSet.isClicked()) {
            context.setFill(Color.web("0096c9", 0.9));
            context.fillRect(51 + xOffset + x * rectSize, 41 + yOffset + y * rectSize, rectSize - 1, rectSize - 1);
        }

        context.strokeRect(50 + xOffset + x * rectSize, 40 + yOffset + y * rectSize, rectSize, rectSize);
        context.setFill(paint);
        if (!showOnlyRelevant.isSelected() || (valueSet.getF() != '0' && context.getCanvas() == canvasSOP) || (valueSet.getF() != '1' && context.getCanvas() == canvasPOS)) {
            context.setFont(new Font(20));
            context.fillText(String.valueOf(valueSet.getF()), 60 + xOffset + x * rectSize, 60 + yOffset + y * rectSize);
        }
        if (showIndexes.isSelected()) {
            context.setFont(new Font(9));
            context.fillText(String.valueOf(valueSet.getIndex()), 72 + xOffset + x * rectSize, 72 + yOffset + y * rectSize);
        }
    }

    private List<ValueSet> getAllValueImplicants(ValueSet valueSet, GraphicsContext context) {
        List<ValueSet> valueSets = new ArrayList<>();
        List<ValueSet> implicants = (context.getCanvas() == canvasSOP) ? kMap.getPrimeImplicantsSOP() : kMap.getPrimeImplicantsPOS();

        if (implicants != null) {
            for (ValueSet implicant : implicants) {
                if (implicant.contains(valueSet)) {
                    valueSets.add(implicant);
                }
            }
        }

        return valueSets;
    }

    private void solve() {
        kMap.findImplicants();

        solutionTextSOP.getChildren().clear();
        solutionTextPOS.getChildren().clear();

        solutionTextSOP.getChildren().add(new Text("Prime implicants: \n"));
        solutionTextPOS.getChildren().add(new Text("Prime implicants: \n"));

        for (int i = 0; i < kMap.getPrimeImplicantsSOP().size(); i++) {
            ValueSet valueSet = kMap.getPrimeImplicantsSOP().get(i);
            Text text = new Text(valueSet.getFormulaSOP());
            bindTextWithValueSet(text, valueSet);
            Text sign = new Text((i == kMap.getPrimeImplicantsSOP().size() - 1) ? "" : " + ");
            solutionTextSOP.getChildren().addAll(text, sign);
        }
        if (kMap.getPrimeImplicantsSOP().size() == 0) {
            solutionTextSOP.getChildren().add(new Text("0"));
        }

        for (ValueSet valueSet : kMap.getPrimeImplicantsPOS()) {
            Text text = new Text(valueSet.getFormulaPOS() + "  ");
            bindTextWithValueSet(text, valueSet);
            solutionTextPOS.getChildren().add(text);
        }
        if (kMap.getPrimeImplicantsPOS().size() == 0) {
            solutionTextPOS.getChildren().add(new Text("1"));
        }

        solutionTextSOP.getChildren().add(new Text("\n\nMinimal SOP function: \n"));
        solutionTextPOS.getChildren().add(new Text("\n\nMinimal POS function: \n"));

        for (int i = 0; i < kMap.getMinimalCoverSOP().size(); i++) {
            ValueSet valueSet = kMap.getMinimalCoverSOP().get(i);
            Text text = new Text(valueSet.getFormulaSOP());
            bindTextWithValueSet(text, valueSet);
            Text sign = new Text((i == kMap.getMinimalCoverSOP().size() - 1) ? "" : " + ");
            solutionTextSOP.getChildren().addAll(text, sign);
        }
        if (kMap.getMinimalCoverSOP().size() == 0) {
            solutionTextSOP.getChildren().add(new Text("0"));
        }

        for (ValueSet valueSet : kMap.getMinimalCoverPOS()) {
            Text text = new Text(valueSet.getFormulaPOS() + "  ");
            bindTextWithValueSet(text, valueSet);
            solutionTextPOS.getChildren().add(text);
        }
        if (kMap.getMinimalCoverPOS().size() == 0) {
            solutionTextPOS.getChildren().add(new Text("1"));
        }

        solutionTextSOP.getChildren().add(new Text("\n\nSimplified SOP function: \n"));
        int gateCount = 0;
        for (int i = 0; i < kMap.getMinimalCoverSOP().size(); i++) {
            ValueSet valueSet = kMap.getPrimeImplicantsSOP().get(i);
            String formula = valueSet.getSimplifiedSOPFormula();
            int startIndex = formula.indexOf("Gate");
            if (startIndex != -1) {
                String removedString = formula.substring(startIndex);
                formula = formula.replace(removedString, "");

                // Parse the integer from the removed string
                String integerString = removedString.replaceAll("\\D", "");
                int parsedInt = Integer.parseInt(integerString);
                gateCount += parsedInt;
            }
            Text text = new Text(formula);
            bindTextWithValueSet(text, valueSet);
            Text sign = new Text((i == kMap.getMinimalCoverSOP().size() - 1) ? "" : " + ");
            solutionTextSOP.getChildren().addAll(text, sign);
        }
        solutionTextSOP.getChildren().add(new Text(" Gatecount: " + gateCount));

        solutionTextPOS.getChildren().add(new Text("\n\nSimplified POS function: \n"));
        int gateCountPOS = 0;
        for (int i = 0; i < kMap.getMinimalCoverPOS().size(); i++) {
            ValueSet valueSet = kMap.getPrimeImplicantsPOS().get(i);
            String formula = valueSet.getSimplifiedPOSFormula();
            int startIndex = formula.indexOf("Gate");
            if (startIndex != -1) {
                String removedString = formula.substring(startIndex);
                formula = formula.replace(removedString, "");

                // Parse the integer from the removed string
                String integerString = removedString.replaceAll("\\D", "");
                int parsedInt = Integer.parseInt(integerString);
                gateCountPOS += parsedInt;
            }
            Text text = new Text(formula);
            bindTextWithValueSet(text, valueSet);
            Text sign = new Text((i == kMap.getMinimalCoverPOS().size() - 1) ? "" : " + ");
            solutionTextPOS.getChildren().addAll(text, sign);
        }
        solutionTextPOS.getChildren().add(new Text(" Gatecount: " + gateCountPOS));


        updateCanvas();
    }

    private void bindTextWithValueSet(Text text, ValueSet valueSet) {
        text.setFill(Color.web(valueSet.getColor()));

        text.setOnMousePressed(event -> {
            valueSet.setHighlighted(true);
            if (show3D.isSelected()) {
                cubeModelBuilderSOP.highLightValues(valueSet);
                cubeModelBuilderPOS.highLightValues(valueSet);
            } else {
                updateCanvas();
            }
        });
        text.setOnMouseReleased(event -> {
            valueSet.setHighlighted(false);
            updateCanvas();
        });
    }
}
