package sample;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import sample.datamodel.ToDoItem;
import sample.datamodel.TodoData;

import java.time.LocalDate;

public class DialogController {
    @FXML
    private TextField shortDetailsField;
    @FXML
    private TextArea DetailsArea;
    @FXML
    private DatePicker dateTimeField;

    public ToDoItem proccesing() {
        String shortDescription = shortDetailsField.getText().trim();
        String details = DetailsArea.getText().trim();
        LocalDate deadLineValue = dateTimeField.getValue();
        ToDoItem newItem = new ToDoItem(shortDescription, details, deadLineValue);
        TodoData.getInstance().addTodoItem(newItem);
        return newItem;
    }
}
