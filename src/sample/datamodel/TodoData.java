package sample.datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.net.ssl.SNIHostName;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

public class TodoData {
    private static TodoData instance = new TodoData();
    private static String filename = "todoData.txt";

    private ObservableList<ToDoItem> toDoItems;
    private DateTimeFormatter formatter;

    private TodoData(){
        formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    }

    public static TodoData getInstance(){
        return instance;
    }

    public ObservableList<ToDoItem> getToDoItems() {
        return toDoItems;
    }

    public void addTodoItem(ToDoItem item){
        toDoItems.add(item);
    }

    // this method reads toDoITem from file (filename)
    public void loadDataFromFile() throws IOException {
        toDoItems = FXCollections.observableArrayList();
        Path path = Paths.get(filename);
        BufferedReader br = Files.newBufferedReader(path);

        String input;

        try{
            while((input = br.readLine()) != null){
                String[] itemPieces = input.split("\t");

                String shortDescription = itemPieces[0];
                String details = itemPieces[1];
                String dateTime = itemPieces[2];

                LocalDate date = LocalDate.parse(dateTime, formatter);
                ToDoItem newItem = new ToDoItem(shortDescription, details, date);
                toDoItems.add(newItem);
            }
            System.out.println("all data were in file is loaded well");
        }finally {
            if(br != null){
                br.close();
            }
        }
    }


    //this method write todoItem to file
    public void storeDataInFile() throws IOException{
        Path path = Paths.get(filename);
        BufferedWriter bw = Files.newBufferedWriter(path);
        try{
            Iterator<ToDoItem> iter = toDoItems.iterator();
            while(iter.hasNext()){
                ToDoItem item = iter.next();
                bw.write(String.format("%s\t%s\t%s\t",
                        item.getShortDescription(), item.getDetails(), item.getDeadLine().format(formatter)));
                bw.newLine();
            }
            System.out.println("the file has been created");
        }finally {
            if(bw != null){
                bw.close();
            }
        }
    }

    public void removeTodoItem(ToDoItem item){
        toDoItems.remove(item);
    }
}
