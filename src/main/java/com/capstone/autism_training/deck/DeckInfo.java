package com.capstone.autism_training.deck;

public class DeckInfo {

    public int id;
    public byte[] image;
    public String name;
    public String description;

    public DeckInfo(int id, byte[] image, String name, String description) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.description = description;
    }
}
