package com.capstone.autism_training.help;

public class HelpCardModel {

    public long id;
    public String name;
    public byte[] image;

    public HelpCardModel(long id, String name, byte[] image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }
}
