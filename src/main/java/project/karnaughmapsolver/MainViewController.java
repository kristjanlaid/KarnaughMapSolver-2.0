package project.karnaughmapsolver;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
    private GridPane extraSettings;
    @FXML
    private CheckBox show3D;
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
    private Canvas canvasPOS;
    @FXML
    private Canvas canvasSOP;
    @FXML
    private TextFlow solutionTextPOS;
    @FXML
    private TextFlow solutionTextSOP;


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
    private void solveButtonClicked() {
        solve();
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

    private void initTruthTable() {
        truthTable.setOnMouseClicked(event -> {
            ValueSet focusedItem = truthTable.getFocusModel().getFocusedItem();
            if (focusedItem != null && truthTable.getFocusModel().getFocusedCell().getColumn() == variablesChoiceBox.getValue()) {
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
            extraSettings.setDisable(!show3D.isSelected());
        });
        exit.setOnAction(event -> System.exit(0));

        ToggleGroup tg = new ToggleGroup();
        initZeros.setToggleGroup(tg);
        initOnes.setToggleGroup(tg);
        initDontCares.setToggleGroup(tg);
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
            alert.setTitle("Help");
            alert.setContentText("""
                    This is an application for simplifying boolean functions using Karnaugh Map method.

                    Kaarel Kütt
                    2022""");
            alert.showAndWait();
        });
        help.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.NONE, "", ButtonType.OK);
            alert.setTitle("Help");
            alert.setContentText("How to use this application");
            alert.showAndWait();
        });
    }

    private void variableChange() {
        loadTruthTable();
        updateKMap();
        updateCanvas();
        show3D.setDisable(variablesChoiceBox.getValue() < 5);
        extraSettings.setDisable(variablesChoiceBox.getValue() < 5 || !show3D.isSelected());
    }

    private void loadTruthTable() {
        int numberOfVariables = variablesChoiceBox.getValue();
        truthTable.getItems().clear();
        truthTable.getColumns().clear();

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
        int numberOfRows = (int) Math.pow(2, numberOfVariables);
        for (int i = 0; i < numberOfRows; i++) {
            char initValue;
            if (initZeros.isSelected()) {
                initValue = '0';
            } else if (initOnes.isSelected()) {
                initValue = '1';
            } else {
                initValue = '?';
            }
            ValueSet valueSet = new ValueSet(i, numberOfVariables, initValue);
            truthTable.getItems().add(valueSet);
        }

        truthTable.refresh();
    }

    private void updateCanvas() {
        if (truthTable.getItems().isEmpty()) {
            return;
        }

        GraphicsContext[] graphicsContexts = {canvasPOS.getGraphicsContext2D(), canvasSOP.getGraphicsContext2D()};
        int rectSize = 35;

        for (GraphicsContext context : graphicsContexts) {
            context.clearRect(0, 0, context.getCanvas().getWidth(), context.getCanvas().getHeight());
            context.setLineWidth(2);

            drawHeaders(context, rectSize);

            for (int z = kMap.sizeZ() - 1; z >= 0; z--) {

                if (show3D.isSelected()) {
                    int dif = (int) Math.abs(Math.round(focusSlider.getValue()) - z);
                    context.setStroke(getColor(dif, cellOpacitySlider.getValue()));
                    context.setFill(getColor(dif, textOpacitySlider.getValue()));
                    int offset = (int) (spacingSlider.getValue() * z);

                    for (int x = 0; x < kMap.sizeX(); x++) {
                        for (int y = 0; y < kMap.sizeY(); y++) {
                            drawElement(x, y, offset, offset, rectSize, kMap.getValue(x, y, z), context);
                        }
                    }
                } else {
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


        String[][] labels = {{"0", "1"}, {"00", "01", "11", "10"}, {"000", "001", "011", "010", "110", "111", "101", "100"}};

        String letters = "ABCDEF";
        int charIndex = 0;

        //z axle
        int lim = (show3D.isSelected() && variables > 4) ? ((variables > 5) ? 4 : 2) : 0;
        context.fillText(letters.substring(charIndex, charIndex + lim / 2), 80 + 4 * rectSize, 25);
        charIndex = charIndex + lim / 2;
        for (int i = 0; i < lim; i++) {
            context.fillText(labels[lim / 4][i], 63 + 4 * rectSize + i * Math.max(spacingSlider.getValue(), 12), 43 + i * Math.max(spacingSlider.getValue(), 12));
        }

        //x axle
        lim = (!show3D.isSelected() && variables > 4) ? 8 : ((variables > 2) ? 4 : 2);
        context.fillText(letters.substring(charIndex, charIndex + lim / 4 + 1), 30, 12);
        charIndex = charIndex + lim / 4 + 1;
        for (int i = 0; i < lim; i++) {
            context.fillText(labels[lim / 4][i], 62 + i * rectSize - lim / 4 * 3, 35);
        }

        //y axle
        lim = (!show3D.isSelected() && variables > 5) ? 8 : ((variables > 3) ? 4 : 2);
        context.fillText(letters.substring(charIndex, charIndex + lim / 4 + 1), 10 - lim, 35);
        for (int i = 0; i < lim; i++) {
            context.fillText(labels[lim / 4][i], 30 - lim / 4 * 3, 62 + i * rectSize);
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

    private Color getColor(int multiplier, double val) {
        int hex = (int) (0x44 + multiplier * val * 5);
        String color = Integer.toHexString(hex).repeat(3);
        double opacity = 1 - multiplier * val * 0.02;
        return Color.web(color, opacity);
    }

    private void solve() {
        kMap.findPrimeImplicants();

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

        for (int i = 0; i < kMap.getEssentialPrimeImplicantsSOP().size(); i++) {
            ValueSet valueSet = kMap.getEssentialPrimeImplicantsSOP().get(i);
            Text text = new Text(valueSet.getFormulaSOP());
            bindTextWithValueSet(text, valueSet);
            Text sign = new Text((i == kMap.getEssentialPrimeImplicantsSOP().size() - 1) ? "" : " + ");
            solutionTextSOP.getChildren().addAll(text, sign);
        }
        if (kMap.getEssentialPrimeImplicantsSOP().size() == 0) {
            solutionTextSOP.getChildren().add(new Text("0"));
        }

        for (ValueSet valueSet : kMap.getEssentialPrimeImplicantsPOS()) {
            Text text = new Text(valueSet.getFormulaPOS() + "  ");
            bindTextWithValueSet(text, valueSet);
            solutionTextPOS.getChildren().add(text);
        }
        if (kMap.getEssentialPrimeImplicantsPOS().size() == 0) {
            solutionTextPOS.getChildren().add(new Text("1"));
        }

        updateCanvas();
    }

    private void bindTextWithValueSet(Text text, ValueSet valueSet) {
        text.setFill(Color.web(valueSet.getColor()));

        text.setOnMousePressed(event -> {
            valueSet.setHighlighted(true);
            updateCanvas();
        });
        text.setOnMouseReleased(event -> {
            valueSet.setHighlighted(false);
            updateCanvas();
        });
    }
}
