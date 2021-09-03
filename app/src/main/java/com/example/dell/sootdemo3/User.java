package com.example.dell.sootdemo3;

public class User {
    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }

    public boolean isLow() {
        if (age<18)
            System.out.println("小于18");
        else if (age>20)
            System.out.println("大于20");
        else
            System.out.println("XXX");
        while(age>0)
            age--;
        return true;
    }

}
