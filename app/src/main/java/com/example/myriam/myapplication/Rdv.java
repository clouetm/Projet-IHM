package com.example.myriam.myapplication;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Rdv implements Comparable {
    private String nom;
    private Date date;

    public Rdv(String nom, Date date) {
        this.nom = nom;
        this.date = date;
    }

    public Rdv(String nom, String horraire) {
        this.nom = nom;
        if(date != null) {
            Log.i("RDV","modif rdv "+ nom + " à " + horraire);
            date.setTime(Integer.parseInt(horraire));
        }
        else {
            Log.i("RDV","new rdv "+ nom + " à " + horraire);
            date = Calendar.getInstance().getTime();
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            try {
                date.setTime(format.parse(horraire).getTime());
            } catch (ParseException e) {
                Log.i("format parse","parse horaire "+ horraire);
            }
        }
    }

    public String getNom() {
        return nom;
    }

    public String getJour() {
        DateFormat format = new SimpleDateFormat("E");
        return format.format(date.getTime());
    }



    public String getMois() {
        DateFormat format = new SimpleDateFormat("M");
        return format.format(date.getTime());
    }


    public String getHoraire() {
        DateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(date.getTime());
    }


    @Override
    public String toString() {
        return getHoraire() + " \n" + nom;
    }

    @Override
    public int compareTo(Object o) {
        Rdv rdv = (Rdv) o;
        return this.getHoraire().compareTo(rdv.getHoraire());
    }

    @Override
    public boolean equals(Object obj) {
        boolean equals = false;
        Rdv rdv = (Rdv) obj;
        if(rdv.getNom().equals(this.nom) && rdv.getHoraire().equals(this.getHoraire())) equals = true;
        return equals;
    }

}
