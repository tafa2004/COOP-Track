package com.cooptrack;

public abstract class Record {
    private int id;
    private String member;
    private String description;
    private double amount;

    public Record() {}

    public Record(int id, String member, String description, double amount) {
        this.id = id;
        this.member = member;
        this.description = description;
        this.amount = amount;
    }

    public int getId() { return id; }
    public String getMember() { return member; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }

    public void setId(int id) { this.id = id; }
    public void setMember(String member) { this.member = member; }
    public void setDescription(String description) { this.description = description; }
    public void setAmount(double amount) { this.amount = amount; }

    // Each subclass must provide its own summary (polymorphism)
    public abstract String getSummary();
}