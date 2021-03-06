/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimizer.gui;

//import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import optimizer.Optimizer;
import optimizer.datastructure.Connection;
import optimizer.datastructure.Node;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import optimizer.datastructure.Pair;
import optimizer.logic.ExampleLogicClass;

/**
 *
 * @author adas
 */
public class View implements Initializable {

    @FXML
    TableView<Node> nodesTable;
    @FXML
    TableColumn<Node, String> nodesColumn;
    @FXML
    TableView<Connection> connectionsTable;
    @FXML
    TableColumn<Connection, String> connectionsColumn;
    @FXML
    TableView<String> allTasksTable;
    @FXML
    TableColumn<String, String> allTasksColumn;
    @FXML
    TableView<String> selectedTasksTable;
    @FXML
    TableColumn<String, String> selectedTasksColumn;
    @FXML
    ScrollPane imageMap;
    @FXML
    BorderPane window;
    @FXML
    Canvas map;
    @FXML
    Button startButton;
    @FXML
    Button randomButton;
    private GraphicsContext gc;
    public Node connectionFirstNode = null;
    private ExampleLogicClass logic;
    ArrayList<Node> shortestPath;
    Optimizer optimizer;
    ArrayList<String> tasks;
    ObservableList<String> selectedTask;

    public View() {
        this.optimizer = new Optimizer();
        this.logic = new ExampleLogicClass();
        this.shortestPath = new ArrayList<Node>();
        this.tasks = new ArrayList<String>();
        this.selectedTask = FXCollections.<String>observableArrayList();
        //this.tasks.add("supermarket");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.optimizer.initialize(this);
        this.nodesTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                connectionsTable.getSelectionModel().select(null);
                connectionFirstNode = null;
            }
        });
        this.connectionsTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                nodesTable.getSelectionModel().select(null);
                connectionFirstNode = null;
            }
        });
        this.allTasksTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {  
                selectedTask.add(allTasksTable.getSelectionModel().getSelectedItem());
                System.out.println("Task to select is:" + selectedTask);
                selectedTasksColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue()));
                selectedTasksTable.setItems(selectedTask);
                selectedTasksTable.getColumns().addAll();
                showSelectedTasks(selectedTask);
                tasks = new ArrayList<String>(selectedTasksTable.getItems());
            }
        });
        this.imageMap.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                addNodeOnMap(event);
                addConnectionOnMap(event);
            }
        });
        this.startButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("Clicked");
                showPath();
            }
        });
        this.randomButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("Clicked");
                random();
            }
        });
        gc = map.getGraphicsContext2D();
    }

    public void showPossibleNodes(ArrayList<Node> possibleNodes) {
        ObservableList<Node> observablePossibleNodes = FXCollections.observableArrayList(possibleNodes);
        nodesColumn.setCellValueFactory(new PropertyValueFactory<Node, String>("name"));
        nodesTable.setItems(observablePossibleNodes);
    }

    public void showPossibleConnections(ArrayList<Connection> possibleConnections) {
        ObservableList<Connection> observablePossibleConnections = FXCollections.observableArrayList(possibleConnections);
        connectionsColumn.setCellValueFactory(new PropertyValueFactory<Connection, String>("name"));
        connectionsTable.setItems(observablePossibleConnections);
    }

    public void showPossibleTasks(ArrayList<String> possibleTasks) {
        ObservableList<String> observablePossibleTasks = FXCollections.observableArrayList(possibleTasks);
        allTasksColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        allTasksTable.setItems(observablePossibleTasks);

    }

    public void showSelectedTasks(ObservableList<String> selectedTasks) {
        ObservableList<String> observableChoosenTasks = FXCollections.observableArrayList(selectedTasks);
        selectedTasksColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        selectedTasksTable.setItems(observableChoosenTasks);
        System.out.println("funckja showSelectedTasks" + selectedTasks);
    }

    public void addNodeOnMap(MouseEvent event) {
        Node nodeOnMap = nodesTable.getSelectionModel().getSelectedItem();
        if (nodeOnMap == null) {
            System.out.println("Node type not selected.");
            return;
        }
        Long x = new Double(event.getX()).longValue();
        Long y = new Double(event.getY()).longValue();
        System.out.println(nodeOnMap.toString());
        Node nodeToAdd = new Node(nodeOnMap.getName(), nodeOnMap.getTimeRequired(), nodeOnMap.getTimeOpen(), nodeOnMap.getTimeClose(), x, y, nodeOnMap.isOpen());
        System.out.println("Utworzono " + nodeToAdd.toString());
        ArrayList<Node> nodesOnMap = optimizer.addNewNode(nodeToAdd);
        drawNode(nodeToAdd.getX(), nodeToAdd.getY(), Color.BLUE);
        if (shortestPath.size() == 0) {
            shortestPath.add(nodeToAdd);
        }
        //displayMapWithNodesOnIt(nodesOnMap);
        gc.strokeText(nodeToAdd.getName() + " " + nodeToAdd.getTimeRequired(), nodeToAdd.getX(), nodeToAdd.getY());
    }

    public void addConnectionOnMap(MouseEvent event) {
        Connection connectionOnMap = connectionsTable.getSelectionModel().getSelectedItem();
        if (connectionOnMap == null) {
            System.out.println("Connection type not selected.");
            return;
        }
        Long x = new Double(event.getX()).longValue();
        Long y = new Double(event.getY()).longValue();
        if (connectionFirstNode == null) {
            connectionFirstNode = findNodeOnMap(x, y);
            System.out.println(connectionFirstNode.toString());
        } else {
            Connection connectionToAdd = new Connection(
                    connectionOnMap.getName(),
                    connectionOnMap.getTimeRequired(),
                    connectionOnMap.getTimeOpen(),
                    connectionOnMap.getTimeClose(),
                    connectionOnMap.isOpen(),
                    connectionFirstNode,
                    findNodeOnMap(x, y)
            );

            connectionFirstNode.adjacencies.add(connectionToAdd);
            findNodeOnMap(x, y).adjacencies.add(connectionToAdd);
            System.out.println("Connection created between " + connectionFirstNode.getName() + " and " + findNodeOnMap(x, y).getName());
            ArrayList<Connection> connectionsOnMap = optimizer.addNewConnection(connectionToAdd);
            if ("broken".equals(connectionToAdd.getName())) {
                connectionToAdd.setOpen(false);
                drawConnection(connectionToAdd.getAttach1(), connectionToAdd.getAttach2(), Color.RED);
            } else {
                drawConnection(connectionToAdd.getAttach1(), connectionToAdd.getAttach2(), Color.BLACK);
            }
            //displayMapWithConnectionsOnIt(connectionsOnMap);
            Node n1 = connectionToAdd.getAttach1();
            Node n2 = connectionToAdd.getAttach2();
            gc.setStroke(Color.BLACK);
            gc.strokeText(Long.toString(connectionToAdd.getTimeRequired()), (n1.getX() + n2.getX()) / 2, (n1.getY() + n2.getY()) / 2);
        }
    }

    public Node findNodeOnMap(Long x, Long y) {
        ArrayList<Node> nodesOnMap = optimizer.getNodesOnMap();
        double min = 1000;
        Node closestNode = null;
        for (Node node : nodesOnMap) {
            double distance = Math.sqrt(Math.pow(x - node.getX(), 2) + Math.pow(y - node.getY(), 2));
            System.out.println(distance);
            if (distance < min) {
                min = distance;
                closestNode = node;
            }
        }
        return closestNode;
    }

    private void drawNode(Long x, Long y, Color color) {
        gc.setFill(color);
        gc.fillOval(x, y, 10, 10);
    }

    public void drawConnection(Node start, Node end, Color color) {
        gc.setStroke(color);
        gc.strokeLine(
                start.getX(),
                start.getY(),
                end.getX(),
                end.getY()
        );
    }

    public void displayMapWithNodesOnIt(ArrayList<Node> nodesOnMap) {
        for (Node node : nodesOnMap) {
            drawNode(node.getX(), node.getY(), Color.BLUE);
        }
    }

    public void displayMapWithConnectionsOnIt(ArrayList<Connection> connectionsOnMap) {
        for (Connection connection : connectionsOnMap) {
            if (connection.isOpen()) {
                drawConnection(connection.getAttach1(), connection.getAttach2(), Color.BLACK);
            } else {
                drawConnection(connection.getAttach1(), connection.getAttach2(), Color.RED);
            }
        }
    }

    public void dehighlightAllNodes() {

    }

    public void highlightNode(Node activeNode) {

    }

    public void removeTask(String task) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /*
    public void drawPath(List<Node> path) {
        gc.setStroke(Color.BLUE);
        for (int it = 1; it < path.size(); it++) {
            Node start = path.get(it - 1);
            Node end = path.get(it);
            gc.strokeLine(
                    start.getX(),
                    start.getY(),
                    end.getX(),
                    end.getY()
            );
        }
    }
     */
    public void printPath(List<Node> path) {
        for (Node x : path) {
            System.out.print(x.getName() + " - ");
        }
    }

    public void showPath() {
        //drawPath(shortestPath);
        if (shortestPath.size() != 0) {
            ArrayList<Node> first = optimizer.getNodesOnMap();
            printPath(shortestPath);
            Node visited = shortestPath.get(0);
            drawNode(visited.getX(), visited.getY(), Color.GREEN);
            shortestPath.remove(0);

            tasks.remove(visited.getName());
        }
    }

    public void random() {
        ArrayList<Node> first = optimizer.getNodesOnMap();
        ArrayList<Connection> second = optimizer.getConnectionsOnMap();
        Node activeNode;
        if (shortestPath.size() == 0) {
            activeNode = first.get(0);
        } else {
            activeNode = this.shortestPath.get(0);
        }
        this.shortestPath = new ArrayList<Node>();
        logic.randomizeMap(first, second);

        Pair<ArrayList<Node>, ArrayList<Connection>> pair = logic.getPath(first, second, this.tasks, activeNode, 0);
        this.shortestPath = pair.getFirst();
        this.shortestPath.add(first.get(0));
        displayMapWithConnectionsOnIt(optimizer.getConnectionsOnMap());
        printPath(this.shortestPath);
    }

    public void chooseStartNode() {
    }

    ;
    public void loadMap() {
    }

    ;

    public void saveMap() {
    }
;

}
