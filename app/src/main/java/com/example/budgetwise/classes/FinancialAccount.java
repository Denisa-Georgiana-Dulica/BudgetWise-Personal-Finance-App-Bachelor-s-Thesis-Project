package com.example.budgetwise.classes;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
@IgnoreExtraProperties
public class FinancialAccount implements Parcelable,Cloneable {
    private String idAccount;
    private User user;
    private String accountName;
    private AccountType accountType;
    private double currentBalance;
    private String notes;
    private double initialBalance = 0.0;


    public FinancialAccount() {

    }

    public FinancialAccount(User user, String accountName, AccountType accountType, double currectBalance, String notes) {
        this.user = user;
        this.accountName = accountName;
        this.accountType = accountType;
        this.currentBalance = currectBalance;
        this.notes = notes;
    }

    protected FinancialAccount(Parcel in) {
        idAccount = in.readString();
        user = in.readParcelable(User.class.getClassLoader());
        accountName = in.readString();
        String accountTypeName = in.readString();
        accountType = accountTypeName != null ? AccountType.valueOf(accountTypeName) : null;
        currentBalance = in.readDouble();
        notes = in.readString();
        initialBalance = in.readDouble();
    }

    public static final Creator<FinancialAccount> CREATOR = new Creator<FinancialAccount>() {
        @Override
        public FinancialAccount createFromParcel(Parcel in) {
            return new FinancialAccount(in);
        }

        @Override
        public FinancialAccount[] newArray(int size) {
            return new FinancialAccount[size];
        }
    };

    public double getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(double initialBalance) {
        this.initialBalance = initialBalance;
    }

    public String getIdAccount() {
        return idAccount;
    }

    public void setIdAccount(String idAccount) {
        this.idAccount = idAccount;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return  accountName + " - Currect balance:" + currentBalance;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(idAccount);
        dest.writeParcelable(user, flags);
        dest.writeString(accountName);
        dest.writeString(accountType != null ? accountType.name() : null);
        dest.writeDouble(currentBalance);
        dest.writeString(notes);
        dest.writeDouble(initialBalance);
    }

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        FinancialAccount copy = new FinancialAccount();
        copy.idAccount = this.idAccount;
        copy.user = this.user != null ? (User) this.user.clone() : null;
        copy.accountName = this.accountName;
        copy.accountType = this.accountType;
        copy.currentBalance = this.currentBalance;
        copy.notes = this.notes;
        copy.initialBalance = this.initialBalance;
        return copy;
    }
}
