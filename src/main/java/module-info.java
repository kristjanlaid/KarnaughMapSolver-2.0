module project.karnaughmapsolver {
    requires javafx.controls;
    requires javafx.fxml;


    opens project.karnaughmapsolver to javafx.fxml;
    exports project.karnaughmapsolver;
}