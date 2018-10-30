package com.example.myriam.myapplication;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DaysActivity extends AppCompatActivity {
    /**
     * liste des rdvs
     */
    ArrayList<Rdv> rdvArrayList = new ArrayList<>();

    /**
     * objet permettant la lecture de texte
     */
    TextToSpeech txtToSpeech;

    /**
     * Date associée à l'activité
     */
    Calendar day;


    SpeechRecognizer speechRec;

    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_days);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Modification du titre suivant l'action de l'utilisateur
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //---------Récupère informations------------
            // si l'utilisateur à sélectionné le jour depuis le calendrier
            String title = extras.getString("title");
            setTitle(title);

        }
        else{
            day = Calendar.getInstance();
            DateFormat dateFormat = new SimpleDateFormat("dd/MM");
            // si page d'acceuil : aujourd'hui
            setTitle(dateFormat.format(day.getTime()));
        }


        // initialisation du lecteur de texte
        txtToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                txtToSpeech.setLanguage(Locale.FRENCH);
            }
        });



        // bouton flottant + : pour ajouter un rdv
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saisieNewRdv(view);
            }
        });


        // bouton flottant micro : pour reconnaissance vocale
        FloatingActionButton fabMicro = (FloatingActionButton) findViewById(R.id.micro);
        fabMicro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptSpeechInput();
            }
        });


        // affichage des rdvs
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ArrayStringFromArrayRdv(rdvArrayList));
        ListView cl = (ListView) findViewById(R.id.planning);
        cl.setAdapter(adapter);
    }


    /**
     * Affiche un popup pour la saisi d'un rdv
     * @param view la vue attachée au popup
     */
    public void saisieNewRdv(View view){
        LayoutInflater factory = LayoutInflater.from(this);

        //text_entry is an Layout XML file containing two text field to display in alert dialog
        final View textEntryView = factory.inflate(R.layout.text_entry, null);

        final EditText inputNom = (EditText) textEntryView.findViewById(R.id.EditText1);
        final EditText inputHoraire = (EditText) textEntryView.findViewById(R.id.EditText2);

        // valeurs pré-remplies pour la saisie des informations du RDV à ajouter
        inputNom.setText("RDV", TextView.BufferType.EDITABLE);
        DateFormat horaire = new SimpleDateFormat("HH:mm");
        inputHoraire.setText(horaire.format(day.getTime()), TextView.BufferType.EDITABLE);

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setIcon(R.drawable.baseline_add_black_18dp).setTitle("EntertheText:").setView(textEntryView).setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {

                        Log.i("AlertDialog","TextEntry 1 Entered "+inputNom.getText().toString());
                        Log.i("AlertDialog","TextEntry 2 Entered "+inputHoraire.getText().toString());
                        /* User clicked OK so do some stuff */
                        addRdv(inputNom.getText().toString(), inputHoraire.getText().toString());
                    }
                }).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        /*
                         * User clicked cancel so do some stuff
                         */
                    }
                });
        alert.show();
    }


    /**
     * Ajoute un rdv dans la liste des rdvs
     * @param nom l'intitulé du rdv
     * @param horaire l'horaire du rdv
     */
    public void addRdv(String nom, String horaire){
        rdvArrayList.add(new Rdv(nom, horaire));
        rdvArrayList = triRdvs(rdvArrayList);
        afficherRdvs(ArrayStringFromArrayRdv(rdvArrayList));
        Toast.makeText(getApplicationContext(), "Le rendez-vous " + nom + " a été ajouté.",Toast.LENGTH_LONG).show();
    }


    /**
     * Tri un un tableau de Rdv
     * @param tab le tableau à trier
     * @return le tableau trier
     * // TODO : not implemented yet
     */
    public ArrayList<Rdv> triRdvs(ArrayList<Rdv> tab){
        // TODO : implémenter un méthode pour trier les rdv suivant leur horaires
        return tab;

    }

    /**
     * Permet de récupérer un ensemble de String correspondant à un ensemble de rdvs
     * @param tab ensemble de rdvs
     * @return
     */
    public ArrayList<String> ArrayStringFromArrayRdv(ArrayList<Rdv> tab){
        ArrayList<String> tab2 = new ArrayList<>();
        for(int i=0 ; i<tab.size(); i++){
            tab2.add(tab.get(i).toString());
        }
        return tab2;
    }

    /**
     * Affiche une liste de rdvs dans la page
     * @param tab l'ensemble des rdvs à afficher
     */
    public void afficherRdvs(ArrayList<String> tab){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tab);
        ListView cl = (ListView) findViewById(R.id.planning);
        cl.setAdapter(adapter);
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){

            case R.id.read :
                lecture();
                return true;

            default: return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Lis la liste des rdv de la page
     */
    public void lecture(){
        txtToSpeech.speak(textALire(), TextToSpeech.QUEUE_FLUSH, null);
    }

    /**
     * Créer le texte à lire à partir des rdvs de la page
     * @return le texte à lire
     */
    public String textALire(){
        String text = "";
        if(rdvArrayList.size() == 0){
            text = "Vous n'avez pas de rendez-vous aujourd'hui";
        }
        else{
            text = "Vous avez " + rdvArrayList.size() + " rendez-vous aujourd'hui.";
            text += " Votre prochain rendez-vous est à " + rdvArrayList.get(0).getHoraire();
        }
        return text;
    }



    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Vous pouvez parler");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "La reconnaissance vocale n'est pas supportée sur cet appareil",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    String action = reconnaissanceAction(result.get(0));

                    Toast.makeText(getApplicationContext(),
                            action,
                            Toast.LENGTH_SHORT).show();

                }
                break;
            }

        }
    }


    /**
     * analyse de la phrase entendue pour reconnaitre l'action à effectuer
     * @param phrase phrase entendue
     * @return une String correspondant à l'action reconnue
     */
    public String reconnaissanceAction(String phrase){
        String[] mots = phrase.split("\\s+");

        String action = "" ;
        String nom = "rendez-vous";
        DateFormat horaireFormatHeures = new SimpleDateFormat("HH");
        DateFormat horaireFormatMinutes = new SimpleDateFormat("mm");
        String horaire = horaireFormatHeures.format(day.getTime()) +"h"+ horaireFormatMinutes.format(day.getTime());

        ArrayList<String> list_mot = new ArrayList<String>();

        for(int i=0; i < mots.length; i++){
            list_mot.add(mots[i]);
        }
        if(list_mot.contains("ajouter")){
            action = "ajouter";
            if(list_mot.contains("rendez-vous")){
                if(list_mot.get(list_mot.indexOf("rendez-vous")+1) != "a" &&
                        list_mot.get(list_mot.indexOf("rendez-vous")+1) != "à"){
                            nom = list_mot.get(list_mot.indexOf("rendez-vous")+1);

                }
                if(list_mot.contains("à")){
                    horaire = list_mot.get(list_mot.indexOf("à")+1);
                }
            }
        }
        addRdv(nom, horaire);
        return action + " :" + nom + " à " + horaire ;
    }
}
