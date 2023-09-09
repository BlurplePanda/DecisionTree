// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2023T2, Assignment 4
 * Name: Amy Booth
 * Username: boothamy
 * ID: 300653766
 */

/**
 * Implements a decision tree that asks a user yes/no questions to determine a decision.
 * Eg, asks about properties of an animal to determine the type of animal.
 * 
 * A decision tree is a tree in which all the internal nodes have a question, 
 * The answer to the question determines which way the program will
 *  proceed down the tree.  
 * All the leaf nodes have the decision (the kind of animal in the example tree).
 *
 * The decision tree may be a predetermined decision tree, or it can be a "growing"
 * decision tree, where the user can add questions and decisions to the tree whenever
 * the tree gives a wrong answer.
 *
 * In the growing version, when the program guesses wrong, it asks the player
 * for another question that would help it in the future, and adds it (with the
 * correct answers) to the decision tree. 
 *
 */

import ecs100.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.awt.Color;

public class DecisionTree {

    public DTNode theTree;    // root of the decision tree;
    public enum Branch {
        yes, no
    }

    /**
     * Setup the GUI and make a sample tree
     */
    public static void main(String[] args){
        DecisionTree dt = new DecisionTree();
        dt.setupGUI();
        dt.loadTree("sample-animal-tree.txt");
    }

    /**
     * Set up the interface
     */
    public void setupGUI(){
        UI.addButton("Load Tree", ()->{loadTree(UIFileChooser.open("File with a Decision Tree"));});
        UI.addButton("Print Tree", this::printTree);
        UI.addButton("Run Tree", this::runTree);
        UI.addButton("Grow Tree", this::growTree);
        // UI.addButton("Save Tree", this::saveTree);  // for completion
        // UI.addButton("Draw Tree", this::drawTree);  // for challenge
        UI.addButton("Reset", ()->{loadTree("sample-animal-tree.txt");});
        UI.addButton("Quit", UI::quit);
        UI.setDivider(0.5);
    }

    /**  
     * Print out the contents of the decision tree in the text pane.
     * The root node should be at the top, followed by its "yes" subtree,
     * and then its "no" subtree.
     * Needs a recursive "helper method" which is passed a node.
     * 
     * COMPLETION:
     * Each node should be indented by how deep it is in the tree.
     * The recursive "helper method" is passed a node and an indentation string.
     *  (The indentation string will be a string of space characters)
     */
    public void printTree(){
        UI.clearText();
        UI.println(nodeText(theTree));
        printSubTree(theTree.getYes(), Branch.yes, 1);
        printSubTree(theTree.getNo(), Branch.no, 1);
    }

    /**
     * Helper method for printing trees
     * Prints the subtree(s) of a node
     * With "yes:" or "no:" in front, to show which branch it is in
     * @param node the node at the "root" of the subtree (cannot be root of a tree)
     * @param branch enum to represent if it is in the "yes" or "no" branch of the previous node
     */
    public void printSubTree(DTNode node, Branch branch, int layer) {
        if (node == null) { return; }
        UI.print((branch.name()+":"+nodeText(node)).indent(layer*4));
        printSubTree(node.getYes(), Branch.yes, layer+1);
        printSubTree(node.getNo(), Branch.no, layer+1);
    }

    /**
     * Helper method to print node text with question marks.
     * @param node the node to get text from
     * @return the node text with a question mark at the end if it is not an answer
     */
    public String nodeText(DTNode node) {
        return node.getText()+(!node.isAnswer()?"?":"");
    }

    /**
     * Run the tree by starting at the top (of theTree), and working
     * down the tree until it gets to a leaf node (a node with no children)
     * If the node is a leaf it prints the answer in the node
     * If the node is not a leaf node, then it asks the question in the node,
     * and depending on the answer, goes to the "yes" child or the "no" child.
     */
    public void runTree() {
        UI.clearText();
        runSubTree(theTree);
    }

    public DTNode runSubTree(DTNode node) {
        if (node.isAnswer()) {
            UI.println("The answer is: "+node.getText());
            return node;
        }
        boolean yes = UI.askBoolean("Is it true: " + node.getText() + " (Y/N): ");
        if (yes) {
            return runSubTree(node.getYes());
        } else {
            return runSubTree(node.getNo());
        }
    }

    /**
     * Grow the tree by allowing the user to extend the tree.
     * Like runTree, it starts at the top (of theTree), and works its way down the tree
     *  until it finally gets to a leaf node. 
     * If the current node has a question, then it asks the question in the node,
     * and depending on the answer, goes to the "yes" child or the "no" child.
     * If the current node is a leaf it prints the decision, and asks if it is right.
     * If it was wrong, it
     *  - asks the user what the decision should have been,
     *  - asks for a question to distinguish the right decision from the wrong one
     *  - changes the text in the node to be the question
     *  - adds two new children (leaf nodes) to the node with the two decisions.
     */
    public void growTree () {
        DTNode leaf = runSubTree(theTree);
        boolean correctAnswer = UI.askBoolean("Is that right? ");
        if (!correctAnswer) {
            String actualAnswer = UI.askString("OK, what should the answer be? ");
            String property = UI.askString("Oh. I can't distinguish a "+leaf.getText()+" from a "
                    +actualAnswer+"\n"+"Tell me something that's true for a "+actualAnswer+" but not for a "
                    +leaf.getText()+"?\n"+"Property: ");
            DTNode answerYes = new DTNode(actualAnswer);
            DTNode answerNo = new DTNode(leaf.getText());
            leaf.setText(property);
            leaf.setChildren(answerYes, answerNo);

        }

    }

    // You will need to define methods for the Completion and Challenge parts.

    // Written for you

    /** 
     * Loads a decision tree from a file.
     * Each line starts with either "Question:" or "Answer:" and is followed by the text
     * Calls a recursive method to load the tree and return the root node,
     *  and assigns this node to theTree.
     */
    public void loadTree (String filename) { 
        if (!Files.exists(Path.of(filename))){
            UI.println("No such file: "+filename);
            return;
        }
        try{theTree = loadSubTree(new ArrayDeque<String>(Files.readAllLines(Path.of(filename))));}
        catch(IOException e){UI.println("File reading failed: " + e);}
    }

    /**
     * Loads a tree (or subtree) from a Scanner and returns the root.
     * The first line has the text for the root node of the tree (or subtree)
     * It should make the node, and 
     *   if the first line starts with "Question:", it loads two subtrees (yes, and no)
     *    from the scanner and add them as the  children of the node,
     * Finally, it should return the  node.
     */
    public DTNode loadSubTree(Queue<String> lines){
        Scanner line = new Scanner(lines.poll());
        String type = line.next();
        String text = line.nextLine().trim();
        DTNode node = new DTNode(text);
        if (type.equals("Question:")){
            DTNode yesCh = loadSubTree(lines);
            DTNode noCh = loadSubTree(lines);
            node.setChildren(yesCh, noCh);
        }
        return node;

    }



}
