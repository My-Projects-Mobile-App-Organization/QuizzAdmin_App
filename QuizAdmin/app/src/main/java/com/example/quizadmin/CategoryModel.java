package com.example.quizadmin;

public class CategoryModel {
    private String id;
    private String name;
    private int noOfTest;
    private int setCounter;

    public CategoryModel(String id, String name, int noOfTest, int setCounter) {
        this.id = id;
        this.name = name;
        this.noOfTest = noOfTest;
        this.setCounter = setCounter;
    }

    public int getSetCounter() {
        return setCounter;
    }

    public void setSetCounter(int setCounter) {
        this.setCounter = setCounter;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNoOfTest() {
        return noOfTest;
    }

    public void setNoOfTest(int noOfTest) {
        this.noOfTest = noOfTest;
    }
}
