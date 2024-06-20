//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//
//import javax.xml.parsers.*;
//import java.io.File;
//import java.util.HashMap;
//
//public class XMLFileReader {
//    String file;
//    public XMLFileReader(String file) {
//        this.file = file;
//        readXMLFile();
//    }
//
//    public HashMap<String, Node> readXMLFile() {
//        HashMap<String, Node> variableMap = new HashMap<>();
//        try {
//            File inputFile = new File(file);
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(inputFile);
//            doc.getDocumentElement().normalize();
//
//            System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
//
//            NodeList variableList = doc.getElementsByTagName("VARIABLE");
//            System.out.println("----------------------------");
//
//            for (int temp = 0; temp < variableList.getLength(); temp++) {
//                Node variableNode = variableList.item(temp);
//
//                if (variableNode.getNodeType() == Node.ELEMENT_NODE) {
//                    Element variableElement = (Element) variableNode;
//                    System.out.println("Variable Name: "
//                            + variableElement.getElementsByTagName("NAME").item(0).getTextContent());
//
//                    variableMap.put(variableElement.getElementsByTagName("NAME").item(0).getTextContent(), variableNode);
//
//                    NodeList outcomeList = variableElement.getElementsByTagName("OUTCOME");
//                    for (int count = 0; count < outcomeList.getLength(); count++) {
//                        Node outcomeNode = outcomeList.item(count);
//                        System.out.println("Outcome: " + outcomeNode.getTextContent());
//
//                        variableNode.setNodeValue(outcomeNode.getTextContent());
//
//                        variableMap.put(variableElement.getElementsByTagName("NAME").item(0).getTextContent(), variableNode);
//                    }
//                }
//            }
//
//            NodeList definitionList = doc.getElementsByTagName("DEFINITION");
//            System.out.println("----------------------------");
//
//            for (int temp = 0; temp < definitionList.getLength(); temp++) {
//                Node definitionNode = definitionList.item(temp);
//
//                if (definitionNode.getNodeType() == Node.ELEMENT_NODE) {
//                    Element definitionElement = (Element) definitionNode;
//                    System.out.println("Definition For: "
//                            + definitionElement.getElementsByTagName("FOR").item(0).getTextContent());
//
//                    NodeList givenList = definitionElement.getElementsByTagName("GIVEN");
//                    for (int count = 0; count < givenList.getLength(); count++) {
//                        Node givenNode = givenList.item(count);
//                        System.out.println("Given: " + givenNode.getTextContent());
//                    }
//
//                    System.out.println("Table: "
//                            + definitionElement.getElementsByTagName("TABLE").item(0).getTextContent());
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return variableMap;
//    }
//}


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/* XMLFileReade4: The purpose of this class is to read the XML fil.
 * It works by reading line by line. It builds the network
 * (represented by an HashMap) by matching each variable to its node.
 */

public class XMLFileReader {
    String file;
    public XMLFileReader(String file) throws IOException {
        this.file = file;

    }

    public HashMap<String, Node> getMap() throws IOException {
        return readXMLFile(file);
    }

    public HashMap<String, Node> readXMLFile(String path) throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String currLine;
        if (!Objects.equals(currLine = reader.readLine(), "<NETWORK>")) {
            throw new IllegalArgumentException();
        }
        HashMap<String, Node> varsMap = new HashMap<String, Node>();
        String toAdd = "";
        int start, end;
        start = end = 0;
        while ((currLine = reader.readLine()) != null) {
            if (currLine.startsWith("<VARIABLE>")) {
                Node newNode = new Node(toAdd);
                while (!currLine.contains("</VARIABLE>")) {
                    if (currLine.startsWith("\t<NAME>")) {
                        start = 7;
                        end = currLine.length() - 7;
                        toAdd = currLine.substring(start, end);
                        newNode.key = toAdd;
                        varsMap.put(toAdd, newNode);
                    }
                    else if (currLine.startsWith("\t<OUTCOME>")) {
                        start = 10;
                        end = currLine.length() - 10;
                        toAdd = currLine.substring(start, end);
                        newNode.outcome.add(toAdd);
                    }
                    else {

                    }
                    currLine = reader.readLine();
                }
            }
            else if (currLine.startsWith("<DEFINITION>")) {
                String name = "";
                Node currNode = null;
                while (!currLine.contains("</DEFINITION>")) {
                    if (currLine.startsWith("\t<FOR>")) {
                        start = 6;
                        end = currLine.length() - 6;
                        name = currLine.substring(start, end);
                        currNode = varsMap.get(name);
                    }
                    else if (currLine.startsWith("\t<GIVEN>")) {
                        start = 8;
                        end = currLine.length() - 8;
                        toAdd = currLine.substring(start, end);
                        Node parent = varsMap.get(toAdd);
                        if (parent != null)
                            parent.children.add(currNode);
                        if (currNode != null)
                            currNode.parents.add(parent);
                    }
                    else if (currLine.startsWith("\t<TABLE>")) {
                        start = 8;
                        end = currLine.length() - 8;
                        toAdd = currLine.substring(start, end);
                        addTableValues(currNode, toAdd);
                    }
                    currLine = reader.readLine();
                }
                if (currNode != null)
                    currNode.parents = reverseGiven(currNode.parents);
            }
            else if (currLine.contains("</NETWORK>")) {
                reader.close();
                return varsMap;
            }
        }
        reader.close();
        throw new IOException();
    }

    private static ArrayList<Node> reverseGiven(ArrayList<Node> parents){
        ArrayList<Node> res = new ArrayList<Node>();
        for (int i = parents.size()-1; i > -1; i--)
            res.add(parents.get(i));
        return res;
    }

    private static void addTableValues(Node currNode, String toAdd){
        for (String value : toAdd.split(" ")) {
            currNode.table.add(Float.parseFloat(value));
        }
    }
}