package com.example.myriam.myapplication;

import android.util.Log;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SpeechRec {

    /**
     * Date associée à l'activité
     */
    Calendar day;


    int numTest = 1;


    public SpeechRec(Calendar day){
        this.day = day;
    }


    /**
     * analyse de la phrase entendue pour reconnaitre l'action à effectuer
     * @param phrase phrase entendue
     * @return une String correspondant à l'action reconnue
     */
    public Action reconnaissanceAction(String phrase){
        String[] mots = phrase.split("\\s+");

        Action.TypeAction typeAction = Action.TypeAction.NO_REC;
        String nom = "rendez-vous";
        DateFormat horaireFormatHeures = new SimpleDateFormat("HH");
        DateFormat horaireFormatMinutes = new SimpleDateFormat("mm");
        String horaire = horaireFormatHeures.format(day.getTime()) +"h"+ horaireFormatMinutes.format(day.getTime());

        ArrayList<String> list_mot = new ArrayList<String>();

        for(int i=0; i < mots.length; i++){
            list_mot.add(mots[i]);
        }
        if(list_mot.contains("ajouter")){
            typeAction = Action.TypeAction.AJOUTER;
        }
        if(list_mot.contains("déplacer")){
            typeAction = Action.TypeAction.DEPLACER;
        }
        if(list_mot.contains("supprimer")){
            typeAction = Action.TypeAction.SUPPRIMER;
        }
        if(list_mot.contains("rendez-vous")){
            nom = list_mot.get(list_mot.indexOf("rendez-vous")+1);
        }
        if(list_mot.contains("à")){
            String[] time = list_mot.get(list_mot.indexOf("à")+1).split("h");
            if(time.length > 1) horaire = time[0] + ":" + time[1];
            if(time.length == 1) horaire = time[0] + ":00";
            Log.i("REC","horaire " + horaire);

        }

        return new Action(typeAction, new Rdv(nom, horaire));

    }

}
