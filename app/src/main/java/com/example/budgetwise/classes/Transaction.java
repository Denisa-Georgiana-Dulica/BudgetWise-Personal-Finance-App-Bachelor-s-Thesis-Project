package com.example.budgetwise.classes;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
@IgnoreExtraProperties
public class Transaction implements Parcelable, Cloneable {//We use Parcelable so we can pass objects between activities/fragments
    private String idTransaction;
    private TransactionCategory transactionCategory;
    private String idAccount;
    private User user;
    private TransactionType transactionType;
    private Date transactionDate;
    private double transactionAmount;
    private String transactionDescription;

    public Transaction() {
    }

    public Transaction(TransactionCategory transactionCategory, String idAccount, User user, TransactionType transactionType, Date transactionDate, double transactionAmount, String transactionDescription) {
        this.transactionCategory = transactionCategory;
        this.idAccount = idAccount;
        this.user = user;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
        this.transactionAmount = transactionAmount;
        this.transactionDescription = transactionDescription;
    }

    protected Transaction(Parcel in) {
        idTransaction = in.readString();
        transactionCategory = in.readParcelable(TransactionCategory.class.getClassLoader());
        idAccount = in.readString();
        user = in.readParcelable(User.class.getClassLoader());
        String transactionTypeName = in.readString();
        transactionType = transactionTypeName != null ? TransactionType.valueOf(transactionTypeName) : null;
        long dateMillis = in.readLong();
        transactionDate = dateMillis == -1 ? null : new Date(dateMillis);
        transactionAmount = in.readDouble();
        transactionDescription = in.readString();
    }

    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

    public String getIdTransaction() {
        return idTransaction;
    }

    public void setIdTransaction(String idTransaction) {
        this.idTransaction = idTransaction;
    }

    public TransactionCategory getTransactionCategory() {
        return transactionCategory;
    }

    public void setTransactionCategory(TransactionCategory transactionCategory) {
        this.transactionCategory = transactionCategory;
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

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public double getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public void setTransactionDescription(String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(idTransaction);
        dest.writeParcelable(transactionCategory, flags);
        dest.writeString(idAccount);
        dest.writeParcelable(user, flags);
        dest.writeString(transactionType != null ? transactionType.name() : null);
        dest.writeLong(transactionDate != null ? transactionDate.getTime() : -1);
        dest.writeDouble(transactionAmount);
        dest.writeString(transactionDescription);
    }

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            Transaction copy = (Transaction) super.clone();
            copy.transactionCategory = this.transactionCategory != null ? (TransactionCategory) this.transactionCategory.clone() : null;
            copy.idAccount = this.idAccount;
            copy.user = this.user != null ? (User) this.user.clone() : null;
            copy.transactionType = this.transactionType;
            copy.transactionAmount = this.transactionAmount;
            copy.transactionDescription = this.transactionDescription;
            copy.idTransaction = this.idTransaction;
            copy.transactionDate = this.transactionDate != null ? (Date) this.transactionDate.clone() : null;
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
