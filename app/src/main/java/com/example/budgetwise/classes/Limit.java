package com.example.budgetwise.classes;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;
@IgnoreExtraProperties
public class Limit implements Parcelable {
    private String idLimit;
    private String idCategory;
    private double maxSum;
    private double spentAmount;

    private Date dateStartLimit;
    private Date dateFinalLimit;

    public Limit() {
    }

    public Limit( String idCategory,  double maxSum, Date dateStartLimit, Date dateFinalLimit) {
        this.idCategory = idCategory;
        this.maxSum = maxSum;
        this.dateStartLimit = dateStartLimit;
        this.dateFinalLimit = dateFinalLimit;
    }
    protected Limit(Parcel in) {
        idLimit = in.readString();
        idCategory = in.readString();
        maxSum = in.readDouble();
        spentAmount = in.readDouble();
        long startMillis = in.readLong();
        dateStartLimit = startMillis != -1 ? new Date(startMillis) : null;
        long endMillis = in.readLong();
        dateFinalLimit = endMillis != -1 ? new Date(endMillis) : null;
    }

    public static final Creator<Limit> CREATOR = new Creator<Limit>() {
        @Override
        public Limit createFromParcel(Parcel in) {
            return new Limit(in);
        }

        @Override
        public Limit[] newArray(int size) {
            return new Limit[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(idLimit);
        dest.writeString(idCategory);
        dest.writeDouble(maxSum);
        dest.writeDouble(spentAmount);
        dest.writeLong(dateStartLimit != null ? dateStartLimit.getTime() : -1);
        dest.writeLong(dateFinalLimit != null ? dateFinalLimit.getTime() : -1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getIdLimit() {
        return idLimit;
    }

    public void setIdLimit(String idLimit) {
        this.idLimit = idLimit;
    }

    public String getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(String idCategory) {
        this.idCategory = idCategory;
    }

    public double getMaxSum() {
        return maxSum;
    }

    public void setMaxSum(double maxSum) {
        this.maxSum = maxSum;
    }

    public Date getDateStartLimit() {
        return dateStartLimit;
    }

    public void setDateStartLimit(Date dateStartLimit) {
        this.dateStartLimit = dateStartLimit;
    }

    public Date getDateFinalLimit() {
        return dateFinalLimit;
    }

    public void setDateFinalLimit(Date dateFinalLimit) {
        this.dateFinalLimit = dateFinalLimit;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }
}
