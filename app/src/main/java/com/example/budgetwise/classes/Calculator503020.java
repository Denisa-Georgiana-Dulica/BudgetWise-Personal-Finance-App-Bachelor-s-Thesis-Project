package com.example.budgetwise.classes;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Calculator503020 {
    private String id;
    private double monthlyIncome;
    private double needs;
    private double wishes;
    private double savings;

    public Calculator503020() {
    }

    public Calculator503020(double monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
        this.needs = 0.5*monthlyIncome;
        this.wishes = 0.3*monthlyIncome;
        this.savings = 0.2*monthlyIncome;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(double monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public double getNeeds() {
        return needs;
    }

    public void setNeeds(double needs) {
        this.needs = needs;
    }

    public double getWishes() {
        return wishes;
    }

    public void setWishes(double wishes) {
        this.wishes = wishes;
    }

    public double getSavings() {
        return savings;
    }

    public void setSavings(double savings) {
        this.savings = savings;
    }
}
