package com.example.myapplication;
import java.io.Serializable;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;
public class Ticket implements Serializable {
    private String startstation;
    private String endstation;
    private String formattedDate;
    private String formattedTime;
    private Long price;
    private String userEmail;
    public Ticket(){};
    public Ticket(String startstation, String endstation, Timestamp firebaseTimestamp, Long price) {
        this.startstation = startstation;
        this.endstation = endstation;
        this.price = price;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()); SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        if (firebaseTimestamp != null) {
            Date date = firebaseTimestamp.toDate();
            this.formattedDate = dateFormat.format(date);
            this.formattedTime = timeFormat.format(date);
        } else {
            this.formattedDate = "N/A Datum";
            this.formattedTime = "N/A Vreme";
        } this.userEmail = null;
    }
    public Ticket(String startStation, String endStation, String formattedDate, String formattedTime, String priceStr, String userEmail) {
        this.startstation = startStation;
        this.endstation = endStation;
        this.formattedDate = formattedDate;
        this.formattedTime = formattedTime;
        this.userEmail = userEmail;
        try { this.price = Long.parseLong(priceStr);
        } catch (NumberFormatException e) {
            this.price = 0L;
        }
    }
    public String getStartstation() {
        return startstation;
    }
    public String getEndstation() {
        return endstation;
    }
    public String getFormattedDate() {
        return formattedDate;
    }
    public String getFormattedTime() {
        return formattedTime;
    }
    public Long getPrice() {
        return price;
    }
    public String getUserEmail() {
        return userEmail;
    }
    public void setStartstation(String startstation) {
        this.startstation = startstation;
    }
    public void setEndstation(String endstation) {
        this.endstation = endstation;
    }
    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }
    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }
    public void setPrice(Long price) {
        this.price = price;
    }
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    @Override public String toString() {
        return "Vreme: " + formattedTime + "\nCena: " + price + " RSD";
    }
}