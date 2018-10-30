package com.example.myriam.myapplication;

public class Rdv implements Comparable {
    private String nom;
    private int Jour;
    private int mois;
    private String horaire;

    public Rdv(String nom, String horaire) {
        this.nom = nom;
        this.horaire = horaire;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getJour() {
        return Jour;
    }

    public void setJour(int jour) {
        Jour = jour;
    }

    public int getMois() {
        return mois;
    }

    public void setMois(int mois) {
        this.mois = mois;
    }

    public String getHoraire() {
        return horaire;
    }

    public void setHoraire(String horaire) {
        this.horaire = horaire;
    }

    @Override
    public String toString() {
        return horaire + " \n" + nom;
    }

    @Override
    public int compareTo(Object o) {
        Rdv rdv = (Rdv) o;
        return this.horaire.compareTo(rdv.getHoraire());
    }
}
