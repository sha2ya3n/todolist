package sample;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import sample.datamodel.ToDoItem;
import sample.datamodel.TodoData;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {
    private List<ToDoItem> itemList;

    @FXML
    private ListView<ToDoItem> todoListView;

    @FXML
    private TextArea itemDetailsTexArea;

    @FXML
    private Label deadlineLabel;

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private ContextMenu listContextMenu;

    @FXML
    private ToggleButton filterToggleButton;

    private FilteredList<ToDoItem> filteredList;
    private Predicate<ToDoItem> wantAllList;
    private Predicate<ToDoItem> wantTodaysList;

    @FXML
    private Button add_button;



    public void initialize() {

        add_button.setEffect(new DropShadow());
        filterToggleButton.setEffect(new DropShadow());


        // this is the way to initialize the delete item when user right click on each item.
        listContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ToDoItem item = todoListView.getSelectionModel().getSelectedItem();
                removeItem(item);
            }
        });
        listContextMenu.getItems().addAll(deleteMenuItem);


        todoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ToDoItem>() {
            @Override
            public void changed(ObservableValue<? extends ToDoItem> observableValue, ToDoItem oldValue, ToDoItem newValue) {
                if (newValue != null) {
                    ToDoItem item = todoListView.getSelectionModel().getSelectedItem();
                    itemDetailsTexArea.setText(item.getDetails());
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
                    deadlineLabel.setText(df.format(item.getDeadLine()));
                }
            }
        });
        
        wantAllList = new Predicate<ToDoItem>() {
            @Override
            public boolean test(ToDoItem item) {
                return true;
            }
        };

        wantTodaysList = new Predicate<ToDoItem>() {
            @Override
            public boolean test(ToDoItem item) {
                return item.getDeadLine().equals(LocalDate.now());
            }
        };

        filteredList = new FilteredList<ToDoItem>(TodoData.getInstance().getToDoItems(), wantAllList);

        SortedList<ToDoItem> deadLineSortList = new SortedList<ToDoItem>(filteredList,
                new Comparator<ToDoItem>() {
                    @Override
                    public int compare(ToDoItem o1, ToDoItem o2) {
                        return o1.getDeadLine().compareTo(o2.getDeadLine());
                    }
                });

        // these are the way if we want to change our sortListe by description or details.
        SortedList<ToDoItem> descriptionSortedList = new SortedList<ToDoItem>(TodoData.getInstance().getToDoItems(),
                new Comparator<ToDoItem>() {
                    @Override
                    public int compare(ToDoItem o1, ToDoItem o2) {
                        return o1.getShortDescription().compareTo(o2.getShortDescription());
                    }
                });

        SortedList<ToDoItem> detailsSortedList = new SortedList<ToDoItem>(TodoData.getInstance().getToDoItems(),
                new Comparator<ToDoItem>() {
                    @Override
                    public int compare(ToDoItem o1, ToDoItem o2) {
                        return o1.getDetails().compareTo(o2.getDetails());
                    }
                });

        todoListView.setItems(deadLineSortList);
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListView.getSelectionModel().selectFirst();

        // this is a way to customize each cell that display each todo item
        // for example we can change color of text
        todoListView.setCellFactory(new Callback<ListView<ToDoItem>, ListCell<ToDoItem>>() {
            @Override
            public ListCell<ToDoItem> call(ListView<ToDoItem> toDoItemListView) {
                ListCell<ToDoItem> cell = new ListCell<ToDoItem>(){
                    @Override
                    protected void updateItem(ToDoItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){
                            setText(null);
                        }else{
                            setText(item.getShortDescription());
                            if(item.getDeadLine().isBefore(LocalDate.now().plusDays(1))){
                                setTextFill(Color.RED);
                            }else if(item.getDeadLine().equals(LocalDate.now().plusDays(1))){
                                setTextFill(Color.BLUE);
                            }
                        }
                    }
                };

                cell.emptyProperty().addListener(
                        (obs, wasEmpty, isNowEmpty)->{
                            if(isNowEmpty){
                                cell.setContextMenu(null);
                            }else{
                                cell.setContextMenu(listContextMenu);
                            }
                        }
                );
                return cell;
            }
        });
    }

    @FXML
    public void showNewItemDialog(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("This is the way u can add new todo item");
        dialog.setHeaderText("This is header and bigger that the other");
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoDialog.fxml"));
        try{
            dialog.getDialogPane().setContent(fxmlLoader.load());
        }catch (IOException e){
            System.out.println("Can't procces your request");
            e.getStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){
            DialogController dialogController = fxmlLoader.getController();
            ToDoItem newItem = dialogController.proccesing();
            /*todoListView.getItems().setAll(TodoData.getInstance().getToDoItems());*/
            todoListView.getSelectionModel().select(newItem);
            System.out.println("OK pressed");
        }else{
            System.out.println("Cancel pressed");
        }
    }

    public void viewItemHandler() {
        ToDoItem item = (ToDoItem) todoListView.getSelectionModel().getSelectedItem();
        itemDetailsTexArea.setText(item.getDetails());
        deadlineLabel.setText(item.getDeadLine().toString());
    }

    public void removeItem(ToDoItem item){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete a todo Item");
        alert.setHeaderText("Delete item : " + item.getShortDescription());
        alert.setContentText("Are you sure aboud deleting the item? if you are press ok otherwise press cancel");

        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && (result.get() == ButtonType.OK)){
            TodoData.getInstance().removeTodoItem(item);
        }else{
            alert.close();
        }
    }

    // this method is a feature to remove each item by event key like Delete directly from keyboard
    @FXML
    public void removeByKey(KeyEvent keyEvent){
        ToDoItem deleteItem = todoListView.getSelectionModel().getSelectedItem();
        if(deleteItem != null){
            if(keyEvent.getCode().equals(KeyCode.DELETE)){
                TodoData.getInstance().removeTodoItem(deleteItem);
            }
        }
    }

    @FXML
    public void handleFilterItem(){
        ToDoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if(filterToggleButton.isSelected()){
            filteredList.setPredicate(wantTodaysList);
            if(filteredList.contains(selectedItem)){
                todoListView.getSelectionModel().select(selectedItem);
            }else{
                todoListView.getSelectionModel().selectFirst();
            }
        }else{
            filteredList.setPredicate(wantAllList);
            if(filteredList.contains(selectedItem)){
                todoListView.getSelectionModel().select(selectedItem);
            }else{
                todoListView.getSelectionModel().selectFirst();
            }
        }
    }

    @FXML
    public void handleExit(){
        Platform.exit();
    }

}







