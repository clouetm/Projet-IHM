package com.example.myriam.myapplication;


public class Action {
    public enum TypeAction { NO_REC, AJOUTER, SUPPRIMER, MODIFIER};

    public TypeAction typeAction;
    public Rdv rdv ;

    public Action(TypeAction typeAction){
        this.typeAction = typeAction;
        rdv = new Rdv("","");

    }


    public Action(TypeAction typeAction, Rdv rdv){
        this.typeAction = typeAction;
        this.rdv = rdv;

    }




}
