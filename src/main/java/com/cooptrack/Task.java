package com.cooptrack;

public class Task{

    //The following are instance variables or TaskManager attributes
    private int id;
    private String title;
    private String assignee;
    private String deadline;
    private int workDays;
    private String progress;
    
    //The following is a default constructor as it which takes no parameters allowing instantiation of an object without providing values when creating it
    //A constructor is given the same name as the class

    public Task(){
        this.progress = "Pending";
    }

    //The following are parameterized constructs that  takes parameters when an objects are created making the program more resource effiicient when initializing values
    public Task(int id, String title, String assignee, String deadline, int workDays, String progress){
        this.id = id;
        this.title = title;
        this.assignee = assignee;
        this.deadline = deadline;
        this.workDays = workDays;
        this.progress = progress;
    
    }

    //The following are getters used to retrieve the values of private attributes. Private attributes can only be accessed within the class, for them to be accessible outside the class, getters are used
    public int getId(){
        return id;
    }

    public String getTitle(){
        return title;
    }

    public String getAssignee(){
        return assignee;
    }

    public String getDeadline(){
        return deadline;
    }

    public int getWorkDays(){
        return workDays;
    }

    public String getProgress(){
        return progress;
    }

    //The following are setters awhich  are used to modify values of private attributes
    public void setId(int id){
        this.id = id;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setAssignee(String assignee){
        this.assignee = assignee;
    }
     
    public void setDeadline(String deadline){
        this.deadline = deadline;
    }

    public void setProgress(String progress){
        this.progress = progress;
    }

    public void setWorkDays(int workDays){
        if (workDays < 0){
            throw new IllegalArgumentException("Work days cannot be negative");
        }
        this.workDays = workDays;
    }




}