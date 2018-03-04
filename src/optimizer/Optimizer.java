/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimizer;

import java.util.ArrayList;
import optimizer.datastructure.Connection;
import optimizer.datastructure.Node;
import optimizer.datastructure.Pair;
import optimizer.gui.ExampleFXClass;
import optimizer.logic.ExampleLogicClass;

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
    
    public static void main() {
        
        Optimizer optimizer = new Optimizer();
        optimizer.run();
      
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
    
    public Pair<ArrayList<Node>, ArrayList<Connection>> loadMap(String filename){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.        
    }
    
    public boolean saveMap(ArrayList<Node> nodes, ArrayList<Connection> connections, String filename){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.        
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private ArrayList<Connection> readPossibleConnections() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private ArrayList<String> getPossibleTasks(ArrayList<Node> possibleNodes) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
