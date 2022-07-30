package jda.standardcommand.gamesSetupFiles;

import okhttp3.Headers;

import java.util.ArrayList;
import java.util.Arrays;

public class MonopolyProperties {
    public static ArrayList<MonopolyProperties> properties = new ArrayList<>();
    String name;
    String color;
    int price;
    int mortgageValue;
    int unmortgageValue;
    int rent;
    int rentWithColor;
    int oneHouse;
    int twoHouse;
    int threeHouse;
    int fourHouse;
    int hotel;
    boolean hasBeenBought;

    public MonopolyProperties(String name, String color, int price, int mortgageValue, int unmortgageValue, int rent, int rentWithColor, int oneHouse, int twoHouse, int threeHouse, int fourHouse, int hotel, boolean hasBeenBought){
        this.name = name;
        this.color = color;
        this.price = price;
        this.mortgageValue = mortgageValue;
        this.unmortgageValue = unmortgageValue;
        this.rent = rent;
        this.rentWithColor = rentWithColor;
        this.oneHouse = oneHouse;
        this.twoHouse = twoHouse;
        this.threeHouse = threeHouse;
        this.fourHouse = fourHouse;
        this.hotel = hotel;
        this.hasBeenBought = hasBeenBought;
        properties.add(this);
    }

    public MonopolyProperties() {

    }

    public String getName() {
        return name;
    }

    public String getColor(){
        return color;
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

    public int getRentWithColor(){
        return rentWithColor;
    }

    public int getOneHouse(){
        return oneHouse;
    }

    public int getTwoHouse(){
        return twoHouse;
    }

    public int getThreeHouse(){
        return threeHouse;
    }

    public int getFourHouse(){
        return fourHouse;
    }

    public int getHotel(){
        return hotel;
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
