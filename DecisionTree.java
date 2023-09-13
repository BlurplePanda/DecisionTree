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
 * <p>
 * A decision tree is a tree in which all the internal nodes have a question,
 * The answer to the question determines which way the program will
 * proceed down the tree.
 * All the leaf nodes have the decision (the kind of animal in the example tree).
 * <p>
 * The decision tree may be a predetermined decision tree, or it can be a "growing"
 * decision tree, where the user can add questions and decisions to the tree whenever
 * the tree gives a wrong answer.
 * <p>
 * In the growing version, when the program guesses wrong, it asks the player
 * for another question that would help it in the future, and adds it (with the
 * correct answers) to the decision tree.
 */

import ecs100.*;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.awt.Color;

public class DecisionTree {

    public DTNode theTree;    // root of the decision tree;

    public enum Branch { // type to record which branch it's on
        yes, no
    }

    /**
     * Set up the GUI and make a sample tree
     */
    public static void main(String[] args) {
        DecisionTree dt = new DecisionTree();
        dt.setupGUI();
        dt.loadTree("sample-animal-tree.txt");
    }

    /**
     * Set up the interface
     */
    public void setupGUI() {
        UI.addButton("Load Tree", () -> {
            loadTree(UIFileChooser.open("File with a Decision Tree"));
        });
        UI.addButton("Print Tree", this::printTree);
        UI.addButton("Run Tree", this::runTree);
        UI.addButton("Grow Tree", this::growTree);
        UI.addButton("Save Tree", this::saveTree);  // for completion
        UI.addButton("Draw Tree", this::drawTree);  // for challenge
        UI.addButton("Reset", () -> {
            loadTree("sample-animal-tree.txt");
        });
        UI.addButton("Quit", UI::quit);
        UI.setDivider(0.5);
    }

    /**
     * Print out the contents of the decision tree in the text pane.
     * The root node should be at the top, followed by its "yes" subtree,
     * and then its "no" subtree.
     * Needs a recursive "helper method" which is passed a node.
     * <p>
     * COMPLETION:
     * Each node should be indented by how deep it is in the tree.
     * The recursive "helper method" is passed a node and an indentation string.
     * (The indentation string will be a string of space characters)
     */
    public void printTree() {
        UI.clearText();
        UI.println(nodeText(theTree));
        printSubTree(theTree.getYes(), Branch.yes, 1);
        printSubTree(theTree.getNo(), Branch.no, 1);
    }

    /**
     * Helper method for printing trees
     * Prints the subtree(s) of a node
     * With "yes:" or "no:" in front, to show which branch it is in
     *
     * @param node   the node at the "root" of the subtree (cannot be root of a tree)
     * @param branch enum to represent if it is in the "yes" or "no" branch of the previous node
     */
    public void printSubTree(DTNode node, Branch branch, int layer) {
        if (node == null) {
            return;
        }
        UI.print((branch.name() + ":" + nodeText(node)).indent(layer * 4));
        printSubTree(node.getYes(), Branch.yes, layer + 1);
        printSubTree(node.getNo(), Branch.no, layer + 1);
    }

    /**
     * Helper method to print node text with question marks.
     *
     * @param node the node to get text from
     * @return the node text with a question mark at the end if it is not an answer
     */
    public String nodeText(DTNode node) {
        return node.getText() + (!node.isAnswer() ? "?" : "");
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

    /**
     * helper method for runTree and growTree
     * runs through a subtree until it reaches a leaf node
     * prints and returns the leaf node
     *
     * @param node the "root" of the subtree
     * @return the leaf node it ends at (used for growing)
     */
    public DTNode runSubTree(DTNode node) {
        if (node.isAnswer()) {
            UI.println("The answer is: " + node.getText());
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
     * until it finally gets to a leaf node.
     * If the current node has a question, then it asks the question in the node,
     * and depending on the answer, goes to the "yes" child or the "no" child.
     * If the current node is a leaf it prints the decision, and asks if it is right.
     * If it was wrong, it
     * - asks the user what the decision should have been,
     * - asks for a question to distinguish the right decision from the wrong one
     * - changes the text in the node to be the question
     * - adds two new children (leaf nodes) to the node with the two decisions.
     */
    public void growTree() {
        UI.clearText();
        DTNode leaf = runSubTree(theTree); // the leaf node it ends up at
        boolean correctAnswer = UI.askBoolean("Is that right? ");
        if (!correctAnswer) {
            String actualAnswer = UI.askString("OK, what should the answer be? ");
            String property = UI.askString("Oh. I can't distinguish a " + leaf.getText() + " from a "
                    + actualAnswer + "\n" + "Tell me something that's true for a " + actualAnswer + " but not for a "
                    + leaf.getText() + "?\n" + "Property: ");
            DTNode answerYes = new DTNode(actualAnswer);
            DTNode answerNo = new DTNode(leaf.getText());
            leaf.setText(property);
            leaf.setChildren(answerYes, answerNo);

        }

    }

    /**
     * Saves the tree to a file chosen/created by the user
     * Saves it in the correct format to be able to be loaded back using loadTree
     */
    public void saveTree() {
        try {
            String file = UIFileChooser.save(); // choose file to save to
            if (file != null) { // ensure a file has been chosen (to avoid null pointer exceptions)
                PrintStream out = new PrintStream(file); // create PrintStream (writer) to file
                saveSubTree(theTree, out);
                out.close();
            }
        } catch (IOException e) {
            UI.println("File saving failed: " + e);
        }
    }

    /**
     * helper method for saveTree
     * recursively runs through every node of the tree and saves to file
     *
     * @param node the "root" of the subtree
     * @param out  the PrintStream (writer to file) to use - ensures all is written to same file.
     */
    public void saveSubTree(DTNode node, PrintStream out) {
        if (node == null) {
            return;
        }

        if (node.isAnswer()) {
            out.print("Answer: ");
        } else {
            out.print("Question: ");
        }
        out.println(node.getText());
        saveSubTree(node.getYes(), out);
        saveSubTree(node.getNo(), out);
    }

    /**
     * Draws the tree on the graphics pane
     * Left to right
     * Yes above and no below each parent node (both to the right of it)
     */
    public void drawTree() {
        UI.clearGraphics();
        drawSubTree(theTree, 0, UI.getCanvasHeight(), 60);
    }

    /**
     * Helper method for drawTree
     * Draws lines between nodes first to avoid visual overlap
     * Lines are green for "yes" branch and red for "no" branch
     * Then draw the node and recursively its children and their children etc.
     * Draws each layer increasingly close together vertically
     * since each subtree has to be in half the height of its parent
     * Each layer is 150 further to the right
     *
     * @param node   the "root" of the tree
     * @param top    the top (visually) of the area the subtree can take up
     * @param height the height of the area the subtree can take up
     * @param x      the x position to draw the node at
     */
    public void drawSubTree(DTNode node, double top, double height, double x) {
        if (node == null) {
            return;
        }
        if (!node.isAnswer()) { // only draw line(s) to child node if there is a child node
            UI.setColor(Color.green);
            UI.drawLine(x, top + height / 2, x + 150, top + height / 4); // yes/up line
            UI.setColor(Color.red);
            UI.drawLine(x, top + height / 2, x + 150, top + 3 * height / 4); // no/down line
        }
        UI.setColor(Color.black);
        node.draw(x, top + height / 2);
        drawSubTree(node.getYes(), top, height / 2, x + 150);
        drawSubTree(node.getNo(), top + height / 2, height / 2, x + 150);
    }

    // Written for you

    /**
     * Loads a decision tree from a file.
     * Each line starts with either "Question:" or "Answer:" and is followed by the text
     * Calls a recursive method to load the tree and return the root node,
     * and assigns this node to theTree.
     */
    public void loadTree(String filename) {
        if (!Files.exists(Path.of(filename))) {
            UI.println("No such file: " + filename);
            return;
        }
        try {
            theTree = loadSubTree(new ArrayDeque<String>(Files.readAllLines(Path.of(filename))));
        } catch (IOException e) {
            UI.println("File reading failed: " + e);
        }
    }

    /**
     * Loads a tree (or subtree) from a Scanner and returns the root.
     * The first line has the text for the root node of the tree (or subtree)
     * It should make the node, and
     * if the first line starts with "Question:", it loads two subtrees (yes, and no)
     * from the scanner and add them as the  children of the node,
     * Finally, it should return the  node.
     */
    public DTNode loadSubTree(Queue<String> lines) {
        Scanner line = new Scanner(lines.poll());
        String type = line.next();
        String text = line.nextLine().trim();
        DTNode node = new DTNode(text);
        if (type.equals("Question:")) {
            DTNode yesCh = loadSubTree(lines);
            DTNode noCh = loadSubTree(lines);
            node.setChildren(yesCh, noCh);
        }
        return node;

    }


}
