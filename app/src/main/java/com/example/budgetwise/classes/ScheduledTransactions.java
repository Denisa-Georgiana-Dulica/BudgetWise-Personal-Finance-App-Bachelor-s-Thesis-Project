package com.example.budgetwise.classes;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@IgnoreExtraProperties
public class ScheduledTransactions extends Transaction implements Parcelable,Cloneable {

    private String transactionFrequency;
    private Date startDate;
    private Date endDate;
    private boolean processed = false;
    private Date lastProcessedDate; // default null
    private List<Date> failedDates=new ArrayList<>();
    private List<Date> successDates=new ArrayList<>();
    private transient boolean isCloneInstance = false;//only local save and not serialized
    private transient boolean isMarkedForDeletion = false;


    public ScheduledTransactions()
    {

    }
    public ScheduledTransactions(TransactionCategory transactionCategory, String idAccount, User user, TransactionType transactionType, Date transactionDate, double transactionAmount, String transactionDescription, String transactionFrequency, Date startDate, Date endDate) {
        super(transactionCategory, idAccount, user, transactionType, transactionDate, transactionAmount, transactionDescription);
        this.transactionFrequency = transactionFrequency;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    protected ScheduledTransactions(Parcel in) {
        super(in); // read from Transaction
        transactionFrequency = in.readString();
        long startMillis = in.readLong();
        startDate = startMillis == -1 ? null : new Date(startMillis);
        long endMillis = in.readLong();
        endDate = endMillis == -1 ? null : new Date(endMillis);
        processed = in.readByte() != 0;
        long lastProcessedMillis = in.readLong();
        lastProcessedDate = lastProcessedMillis == -1 ? null : new Date(lastProcessedMillis);
        int size = in.readInt();
        failedDates = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            long millis = in.readLong();
            failedDates.add(new Date(millis));
        }
        int size1 = in.readInt();
        successDates = new ArrayList<>();
        for (int i = 0; i < size1; i++) {
            long millis = in.readLong();
            successDates.add(new Date(millis));
        }
    }

    public static final Creator<ScheduledTransactions> CREATOR = new Creator<ScheduledTransactions>() {
        @Override
        public ScheduledTransactions createFromParcel(Parcel in) {
            return new ScheduledTransactions(in);
        }

        @Override
        public ScheduledTransactions[] newArray(int size) {
            return new ScheduledTransactions[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(transactionFrequency);
        dest.writeLong(startDate != null ? startDate.getTime() : -1);
        dest.writeLong(endDate != null ? endDate.getTime() : -1);
        dest.writeByte(processed ? (byte) 1 : (byte) 0);
        dest.writeLong(lastProcessedDate != null ? lastProcessedDate.getTime() : -1);
        if (failedDates != null) {
            dest.writeInt(failedDates.size());
            for (Date date : failedDates) {
                dest.writeLong(date.getTime());
            }
        } else {
            dest.writeInt(0);
        }
        if (successDates != null) {
            dest.writeInt(successDates.size());
            for (Date date : successDates) {
                dest.writeLong(date.getTime());
            }
        } else {
            dest.writeInt(0);
        }
    }

//    public boolean isCloneInstance() {
//        return isCloneInstance;
//    }

    public boolean isCloneInstanceLocalOnly() {
        return isCloneInstance;
    }

    public void setCloneInstance(boolean cloneInstance) {
        isCloneInstance = cloneInstance;
    }

    public boolean isMarkedForDeletionLocalOnly() {
        return isMarkedForDeletion;
    }

    public void setMarkedForDeletion(boolean markedForDeletion) {
        isMarkedForDeletion = markedForDeletion;
    }

    public List<Date> getSuccessDates() {
        return successDates;
    }

    public void setSuccessDates(List<Date> successDates) {
        this.successDates = successDates;
    }

    public void addFailedDate(Date date) {
        if (!failedDates.contains(date)) {
            failedDates.add(date);
        }
    }
    public void removeFailedDate(Date date) {
        failedDates.remove(date);
    }


    public List<Date> getFailedDates() {
        return failedDates;
    }

    public void setFailedDates(List<Date> failedDates) {
        this.failedDates = failedDates;
    }

    public Date getLastProcessedDate() {
        return lastProcessedDate;
    }

    public void setLastProcessedDate(Date lastProcessedDate) {
        this.lastProcessedDate = lastProcessedDate;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public String getTransactionFrequency() {
        return transactionFrequency;
    }

    public void setTransactionFrequency(String transactionFrequency) {
        this.transactionFrequency = transactionFrequency;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }



    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        ScheduledTransactions copy = (ScheduledTransactions) super.clone();
        copy.transactionFrequency = this.transactionFrequency;
        copy.startDate = this.startDate != null ? (Date) this.startDate.clone() : null;
        copy.endDate = this.endDate != null ? (Date) this.endDate.clone() : null;
        copy.lastProcessedDate = this.lastProcessedDate != null ? (Date) this.lastProcessedDate.clone() : null;
        copy.processed = this.processed;
        copy.failedDates = new ArrayList<>();
        for (Date d : this.failedDates) {
            copy.failedDates.add(d != null ? (Date) d.clone() : null);
        }
        copy.successDates = new ArrayList<>();
        for (Date d : this.successDates) {
            copy.successDates.add(d != null ? (Date) d.clone() : null);
        }
        copy.setTransactionCategory(this.getTransactionCategory());
        copy.setUser(this.getUser());
        copy.setCloneInstance(true);

        return copy;
    }

}
