package com.example.budgetwise.classes;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
@IgnoreExtraProperties
public class TransactionCategory implements Parcelable,Cloneable {
    private String idCategory;
    private String categoryName;
    private String iconRes;
    private String type;
    private String categoryType503020;

    public TransactionCategory() {
    }

    public TransactionCategory(String idCategory, String categoryName, String iconRes,String type,String categoryType503020) {
        this.idCategory = idCategory;
        this.categoryName = categoryName;
        this.iconRes = iconRes;
        this.type=type;
        this.categoryType503020 = categoryType503020;
    }

    protected TransactionCategory(Parcel in) {
        idCategory = in.readString();
        categoryName = in.readString();
        iconRes = in.readString();
        type = in.readString();
        categoryType503020 = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(idCategory);
        dest.writeString(categoryName);
        dest.writeString(iconRes);
        dest.writeString(type);
        dest.writeString(categoryType503020);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TransactionCategory> CREATOR = new Creator<TransactionCategory>() {
        @Override
        public TransactionCategory createFromParcel(Parcel in) {
            return new TransactionCategory(in);
        }

        @Override
        public TransactionCategory[] newArray(int size) {
            return new TransactionCategory[size];
        }
    };

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(String idCategory) {
        this.idCategory = idCategory;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getIconRes() {
        return iconRes;
    }

    public void setIconRes(String iconRes) {
        this.iconRes = iconRes;
    }

    public String getCategoryType503020() {
        return categoryType503020;
    }

    public void setCategoryType503020(String categoryType503020) {
        this.categoryType503020 = categoryType503020;
    }

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return new TransactionCategory(
                this.idCategory,
                this.categoryName,
                this.iconRes,
                this.type,
                this.categoryType503020
        );
    }
}
