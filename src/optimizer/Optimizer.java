/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimizer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import optimizer.datastructure.Connection;
import optimizer.datastructure.Node;
import optimizer.datastructure.Pair;
import optimizer.gui.ExampleFXClass;
import optimizer.logic.ExampleLogicClass;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author adas
 */
public class Optimizer {

    private final ExampleFXClass gui;
    private final ExampleLogicClass logic;
    private ArrayList<Node> nodesOnMap;
    private ArrayList<Connection> connectionsOnMap;
    private ArrayList<String> tasks;
    private Node activeNode;
    
    public Optimizer(){
        
        gui = new ExampleFXClass();
        logic = new ExampleLogicClass();
        
    }
    
    public static void main(String[] args) {
                
        Pair<ArrayList<Node>, ArrayList<Connection>> pair = loadMap("map");
        saveMap(pair.getFirst(), pair.getSecond(), "copy");
        /*Optimizer optimizer = new Optimizer();
        optimizer.run();*/
      
    }
    
    public ArrayList<Node> addNewNode(Node node){
        nodesOnMap.add(node);
        return nodesOnMap;
    }
    
    public ArrayList<Node> removeNode(Node node){
        nodesOnMap.remove(node);
        return nodesOnMap;
    }
    
    public ArrayList<Connection> addNewConnection(Connection connection){
        connectionsOnMap.add(connection);
        return connectionsOnMap;
    }
    
    public ArrayList<Connection> removeConnection(Connection connection){
        connectionsOnMap.remove(connection);
        return connectionsOnMap;
    }
    
    public static Pair<ArrayList<Node>, ArrayList<Connection>> loadMap(String filename){
        Iterator<JSONObject> nodesIterator = getJSONIterator(filename, "nodes");
        Iterator<JSONObject> connectionsIterator = getJSONIterator(filename, "connections");
        
        ArrayList<Node> nodes = new ArrayList<>();
        Node node;
        while(nodesIterator.hasNext()){
            JSONObject object = nodesIterator.next();
            node = new Node(
                (String)object.get("name"),
                (Long)object.get("time_required"),
                (Long)object.get("time_open"),
                (Long)object.get("time_close"),
                (Long)object.get("x"),
                (Long)object.get("y"),
                (Boolean)object.get("open")
            );
            nodes.add(node);
        }
        
        ArrayList<Connection> connections = new ArrayList<>();
        Connection connection;
        while(connectionsIterator.hasNext()){
            JSONObject object = connectionsIterator.next();
            connection = new Connection(
                (String)object.get("name"),
                (Long)object.get("time_required"),
                (Long)object.get("time_open"),
                (Long)object.get("time_close"),
                (Boolean)object.get("open"),
                (Long)object.get("attach1"),
                (Long)object.get("attach2")
            );
            connections.add(connection);
        }
        
        Pair<ArrayList<Node>, ArrayList<Connection>> readyPair = new Pair<>();
        readyPair.setFirst(nodes);
        readyPair.setSecond(connections);
        return readyPair;
    }
    
    public static boolean saveMap(ArrayList<Node> nodes, ArrayList<Connection> connections, String filename){
        
        JSONArray JSONNodes = new JSONArray();
        nodes.forEach((node) -> {
            JSONObject JSONNode = new JSONObject();
            JSONNode.put("name", node.getName());
            JSONNode.put("time_required", node.getTimeRequired());
            JSONNode.put("time_open", node.getTimeOpen());
            JSONNode.put("time_close", node.getTimeClose());
            JSONNode.put("x", node.getX());
            JSONNode.put("y", node.getY());
            JSONNode.put("open", node.isOpen());
            JSONNodes.add(JSONNode);
        });
  
        JSONArray JSONConnections = new JSONArray();
        connections.forEach((connection) -> {
            JSONObject JSONConnection = new JSONObject();
            JSONConnection.put("name", connection.getName());
            JSONConnection.put("time_required", connection.getTimeRequired());
            JSONConnection.put("time_open", connection.getTimeOpen());
            JSONConnection.put("time_close", connection.getTimeClose());
            JSONConnection.put("open", connection.isOpen());
            JSONConnection.put("attach1", connection.getAttach1());
            JSONConnection.put("attach2", connection.getAttach2());
            JSONConnections.add(JSONConnection);
        });
        JSONObject finalObject = new JSONObject();
        finalObject.put("nodes", JSONNodes);
        finalObject.put("connections", JSONConnections);

        String path = Paths.get(".")
                .toAbsolutePath()
                .normalize()
                .toString() + "\\data\\" + filename + ".txt";
        try (FileWriter file = new FileWriter(path)) {

            file.write(finalObject.toJSONString());
            file.flush();

        } catch (IOException e) {
            return false;
        }
        
        return true;
    }
    
    public ArrayList<String> addTask(String task){
        tasks.add(task);
        
        if(activeNode != null){
            updatePath();
        }
        
        return tasks;
    }
    
    public ArrayList<String> removeTask(String task){
        tasks.remove(task);
        return tasks;
    }
    
    public boolean setActivePoint(Node node){
        activeNode = null;
        int index = nodesOnMap.indexOf(node);
        activeNode = nodesOnMap.get(index);
        
        if(activeNode == null)
                return false;
        
        gui.dehighlightAllNodes();
        gui.highlightNode(activeNode);
        
        updatePath();        
        
        return true;
    }
    
    public boolean doStep(){
        if(activeNode == null || tasks.isEmpty())
            return false;
        
        gui.removeTask(tasks.get(0));
        
        Pair<ArrayList<Node>, ArrayList<Connection>> map;
        map = logic.randomizeMap(nodesOnMap, connectionsOnMap);
        nodesOnMap = map.getFirst();
        connectionsOnMap = map.getSecond();
        
        return setActivePoint(nodesOnMap.get(0));
    }

    private void run() {
        ArrayList<Node> possibleNodes = readPossibleNodes();
        ArrayList<Connection> possibleConnections = readPossibleConnections();
        ArrayList<String> possibleTasks = getPossibleTasks(possibleNodes);
        
        gui.showWindow(this);
        
        gui.showPossibleNodes(possibleNodes);
        gui.showPossibleConnections(possibleConnections);
        gui.showPossibleTasks(possibleTasks);  
    }

    private ArrayList<Node> readPossibleNodes() {

        ArrayList<Node> nodes = new ArrayList<>();
        Iterator<JSONObject> iterator = getJSONIterator("elements", "nodes");
        Node node;
        while(iterator.hasNext()){
            JSONObject object = iterator.next();
            node = new Node(
                (String)object.get("name"),
                (Long)object.get("time_required"),
                (Long)object.get("time_open"),
                (Long)object.get("time_close"),
                (long)-1,
                (long)-1,
                false
            );
            nodes.add(node);
        }
        return nodes;
    }

    private ArrayList<Connection> readPossibleConnections() {

        ArrayList<Connection> connections = new ArrayList<>();
        Iterator<JSONObject> iterator = getJSONIterator("elements", "connections");
        Connection connection;
        while(iterator.hasNext()){
            JSONObject object = iterator.next();
            connection = new Connection(
                (String)object.get("name"),
                (Long)object.get("time_required"),
                (Long)object.get("time_open"),
                (Long)object.get("time_close"),
                false,
                (long)0,
                (long)0
            );
            connections.add(connection);
        }
        return connections;
    }

    private static Iterator<JSONObject> getJSONIterator(String filename, String element){
               
        JSONParser parser = new JSONParser();
        Iterator<JSONObject> iterator = null;
        try {

            String path = Paths.get(".")
                    .toAbsolutePath()
                    .normalize()
                    .toString() + "\\data\\" + filename + ".txt";
            
            Object obj = parser.parse(new FileReader(path));

            JSONObject jsonObject = (JSONObject) obj;

            // loop array
            JSONArray msg = (JSONArray) jsonObject.get(element);
            iterator = msg.iterator();

        } catch (FileNotFoundException e) {
        } catch (IOException | ParseException e) {
        }
        
        return iterator;
    }
    
    private ArrayList<String> getPossibleTasks(ArrayList<Node> possibleNodes) {
        
        HashSet<String> tasksSet = new HashSet<>();
        
        for(Node node : possibleNodes){
            if(!tasksSet.contains(node.getName()))
                tasksSet.add(node.getName());
        }
        
        return new ArrayList<>(tasksSet);
        
    }
    
    private void updatePath(){
        Pair<ArrayList<Node>, ArrayList<Connection>> path;
        path = logic.getPath(
                nodesOnMap, 
                connectionsOnMap, 
                tasks, 
                activeNode,
                0);//Te zero tu siedzi bo jeszcze tryby szukania ścieżek trzeba wymyślić
        
        gui.showPath(path.getFirst(), path.getSecond());
    }
}
