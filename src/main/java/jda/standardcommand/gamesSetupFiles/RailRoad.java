package jda.standardcommand.gamesSetupFiles;

import java.util.ArrayList;
import java.util.Arrays;

public class RailRoad extends MonopolyProperties {
    public static ArrayList<RailRoad> properties = new ArrayList<RailRoad>();
    String name;
    int price;
    int mortgageValue;
    int unmortgageValue;
    int rent;
    int rent2;
    int rent3;
    int rent4;
    boolean hasBeenBought;

    public RailRoad(String name, int price, int mortgageValue, int unmortgageValue, int rent, int rent2, int rent3, int rent4, boolean hasBeenBought){
        super();
        this.name = name;
        this.price = price;
        this.mortgageValue = mortgageValue;
        this.unmortgageValue = unmortgageValue;
        this.rent = rent;
        this.rent2 = rent2;
        this.rent3 = rent3;
        this.rent4 = rent4;
        this.hasBeenBought = hasBeenBought;
        properties.add(this);
    }

    public String getName() {
        return name;
    }

    public int getPrice(){
        return price;
    }

    public int getMortgageValue(){
        return mortgageValue;
    }

    public int getUnmortgageValue(){
        return unmortgageValue;
    }

    public int getRent(){
        return rent;
    }

    public int getRent2(){
        return rent2;
    }

    public int getRent3(){
        return rent3;
    }

    public int getRent4(){
        return rent4;
    }

    public boolean getHasBeenBought(){
        return hasBeenBought;
    }



    public ArrayList getInstances(){
        return properties;
    }

    public static void main(String[] args){
        System.out.println(Arrays.toString(properties.toArray()));
    }
}
