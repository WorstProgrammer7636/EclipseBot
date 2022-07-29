package jda.standardcommand.gamesSetupFiles;

import java.util.ArrayList;
import java.util.List;

public class Node {
    public int data;
    public Node next;
    public Node(int data, Node next){
        this.data = data;
        this.next = next;
    }

    public String toString(){
        return data + "";
    }
}
