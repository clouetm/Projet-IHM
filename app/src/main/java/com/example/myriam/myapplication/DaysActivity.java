package com.example.myriam.myapplication;

import android.content.ActivityNotFoundException;
import android.content.Context;
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
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import static android.speech.RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS;
import static android.speech.RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS;
import static android.speech.RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS;

public class DaysActivity extends AppCompatActivity {
    /**
     * liste des rdvs
     */
    ArrayList<Rdv> rdvArrayList = new ArrayList<>();

    /**
     * Date associée à l'activité
     */
    Calendar day;



    private final int REQ_CODE_SPEECH_INPUT = 100;
    private SpeechRec speechRec;
    private TextToSpeech txtToSpeech;

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


        speechRec = new SpeechRec(day);


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
                saisieNewRdv(view, null, null, false);
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
        afficherRdvs();

    }


    /**
     * Affiche un popup pour la saisi d'un rdv
     * @param view la vue attachée au popup
     */
    public void saisieNewRdv(View view, final String currhoraire, final String currnom, final boolean isModifying){

        LayoutInflater factory = LayoutInflater.from(this);

        //text_entry is an Layout XML file containing two text field to display in alert dialog
        final View textEntryView = factory.inflate(R.layout.text_entry, null);

        final EditText inputNom = (EditText) textEntryView.findViewById(R.id.EditText2);
        final EditText inputHoraire = (EditText) textEntryView.findViewById(R.id.EditText1);

        // valeurs pré-remplies pour la saisie des informations du RDV à ajouter

        if(currnom!=null){
            inputNom.setText(currnom, TextView.BufferType.EDITABLE);
        }
        else {
            inputNom.setText("RDV", TextView.BufferType.EDITABLE);
        }

        DateFormat horaire = new SimpleDateFormat("HH:mm");
        if(currhoraire!=null) {
            horaire = new SimpleDateFormat(currhoraire);
        }

        day = Calendar.getInstance();
        inputHoraire.setText(horaire.format(day.getTime()), TextView.BufferType.EDITABLE);

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setIcon(R.drawable.baseline_add_black_18dp).setTitle("Saisir un rendez-vous :").setView(textEntryView).setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {

                        Log.i("AlertDialog","TextEntry 1 Entered "+inputNom.getText().toString());
                        Log.i("AlertDialog","TextEntry 2 Entered "+inputHoraire.getText().toString());
                        /* User clicked OK so do some stuff */

                        // If the user was modifying the rdv, we delete it first, then add it with the changes
                        if(isModifying)
                        {
                            rdvArrayList.remove(new Rdv(currnom, currhoraire));
                        }
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
        Log.i("ADD","add rdv "+ nom + " à " + horaire);
        rdvArrayList.add(new Rdv(nom, horaire));
        rdvArrayList = triRdvs(rdvArrayList);
        afficherRdvs();
        Toast.makeText(getApplicationContext(), "Le rendez-vous " + nom + " a été ajouté.",Toast.LENGTH_LONG).show();
    }


    /**
     * Tri un un tableau de Rdv
     * @param tab le tableau à trier
     * @return le tableau trier
     */
    public ArrayList<Rdv> triRdvs(ArrayList<Rdv> tab){
        Collections.sort(tab);
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
     */
    public void afficherRdvs(){
        // On créé un Adapter
        RdvListViewAdapter adapter = new RdvListViewAdapter(this, rdvArrayList);
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

    public void lecture(String text){
        txtToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
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

                    //String action = reconnaissanceAction(result.get(0));
                    Action actionRec = speechRec.reconnaissanceAction(result.get(0));
                    String actionMessage= "";
                    if (actionRec.typeAction == Action.TypeAction.AJOUTER){
                        actionMessage = "Le rendez-vous " + actionRec.rdv.getNom() + " prévu à " + actionRec.rdv.getHoraire() + " a été ajouté.";
                        addRdv(actionRec.rdv.getNom(), actionRec.rdv.getHoraire());
                    }
                    if(actionRec.typeAction == Action.TypeAction.NO_REC){
                        actionMessage= "Je n'ai pas compris. Veuillez essayer à nouveau.";
                    }
                    if(actionRec.typeAction == Action.TypeAction.DEPLACER){
                        actionMessage ="";
                        boolean rdvFound = false;

                        for (Rdv rdv : rdvArrayList) {
                            if (rdv.getNom().equals(actionRec.rdv.getNom())) {
                                String tmpHor = rdv.getHoraire();
                                rdvFound = true;
                                rdvArrayList.remove(new Rdv(rdv.getNom(), tmpHor));
                            }
                        }
                        if(!rdvFound) {
                            actionMessage = "Je n'ai pas trouvé le rendez-vous à modifier. \n Veuillez essayer à nouveau.";
                        }
                        else {
                            addRdv(actionRec.rdv.getNom(), actionRec.rdv.getHoraire());
                        }
                    }

                    if(actionRec.typeAction == Action.TypeAction.SUPPRIMER){
                        actionMessage ="";
                        if(rdvArrayList.indexOf(actionRec.rdv) >= 0){
                            confirmSuppression(actionRec.rdv.getHoraire(), actionRec.rdv.getNom());
                        }
                        else {
                            actionMessage = "Je n'ai pas trouvé le rendez-vous à supprimer. \n Veuillez essayer à nouveau.";
                        }

                    }
                    lecture(actionMessage);
                    Toast.makeText(getApplicationContext(),
                            actionMessage,
                            Toast.LENGTH_SHORT).show();

                }
                break;
            }

        }
    }




    /**
     * Affiche un popup pour demander la confirmation pour la suppression d'un rdv
     *
     * @param horaire horaire du rdv à supprimer
     * @param nom     nom du rdv à supprimer
     */
    public void confirmSuppression(final String horaire, final String nom) {

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        String phrase_confimation = "Supprimer le rendez-vous  "+ nom + " prévu à  "+ horaire +" ?";
        // définition du popup pour confirmer la suppression du rdv
        alert.setTitle("Supprimer RDV").setMessage(phrase_confimation).setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        Log.i("AlertDialog","supprimer");
                        //Log.i("AlertDialog","TextEntry 2 Entered "+inputHoraire.getText().toString());
                        /* User clicked OK so do some stuff */
                        supprimerRdv(horaire, nom);
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
        // retour audio sur la confirmation de la suppression
        lecture(phrase_confimation);

        alert.show();
    }

    /**
     * Modifier un rdv de la liste des rdv
     *
     * @param horaire horaire du rdv a modifier
     * @param nom     nom du rdv a modifier
     */
    public void modifierRdv(View view, String horaire, String nom) {

        //Popup saisie rdv

        saisieNewRdv(view, horaire, nom, true);

        String phrase_confimation = "Le rendez-vous a été modifié.";
        lecture(phrase_confimation);
        // mise à jour de l'affichage de la liste des rdvs
        afficherRdvs();
    }

    /**
     * supprime un rdv de la liste des rdv et affiche la nouvelle liste
     *
     * @param horaire horaire du rdv a supprimer
     * @param nom     nom du rdv a supprimer
     */
    public void supprimerRdv(String horaire, String nom) {
        /*
        Toast.makeText(getApplicationContext(),
                "Suppression du rendez-vous : " + nom + "prévu à " + horaire,
                Toast.LENGTH_SHORT).show();
        */

        // suppression du rdv dans la liste
        rdvArrayList.remove(new Rdv(nom, horaire));

        // retour audio sur la suppression du rdv
        String phrase_confimation = "Le rendez-vous a été supprimé. " ;
        lecture(phrase_confimation);

        // mise à jour de l'affichage de la liste des rdvs
        afficherRdvs();
    }


    /**
     * Classe pour la définition de l'interface pour l'affichage de la liste de rdv
     * Les objets affichés et les fonctions associées aux éléments de la liste
     */
    public class RdvListViewAdapter extends BaseAdapter {
        Context mContext;
        LayoutInflater inflater;


        public RdvListViewAdapter(Context context, ArrayList<Rdv> objects) {
            this.mContext = context;
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return rdvArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return rdvArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int pos, View inView, ViewGroup parent) {
            final RdvViewHolder holder;

            // Si la ligne n'éxiste pas, on créé la ligne
            if (inView == null) {
                inView = this.inflater.inflate(R.layout.rdv_view, parent, false);
                holder = new RdvViewHolder();
                // On affecte les views
                holder.horaire = (TextView) inView.findViewById(R.id.horaire);
                holder.nom = (TextView) inView.findViewById(R.id.nom);
                holder.delete = (ImageButton) inView.findViewById(R.id.delete);
                inView.setTag(holder);
            }
            // Sinon on récupère la ligne qui est en mémoire
            else
                holder = (RdvViewHolder) inView.getTag();

            // On récupère l'objet courant
            final Rdv rdv = rdvArrayList.get(pos);

            // On met à jour nos views
            final String currhorr = rdv.getHoraire();
            final String currnom = rdv.getNom();

            holder.horaire.setText(currhorr);
            holder.horaire.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    modifierRdv(v,currhorr,currnom);
                }
            });
            holder.nom.setText(rdv.getNom());
            holder.nom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    modifierRdv(v,currhorr,currnom);
                }
            });
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmSuppression(holder.horaire.getText().toString(), holder.nom.getText().toString());
                }
            });

            return (inView);
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }


        /**
         * Affiche un popup pour demander la confirmation pour la suppression d'un rdv
         *
         * @param horaire horaire du rdv à supprimer
         * @param nom     nom du rdv à supprimer
         */
        public void confirmSuppression(final String horaire, final String nom) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
            alert.setTitle("Supprimer Rendez-vous").setMessage("Voulez-vous supprimer le rendez-vous : \n" + nom + " prévu à " + horaire).setPositiveButton("Suppr",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            supprimerRdv(horaire, nom);
                        }
                    }).setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {

                        }
                    });
            alert.show();
        }


        /**
         * supprime un rdv de la liste des rdv et affiche la nouvelle liste
         *
         * @param horaire horaire du rdv a supprimer
         * @param nom     nom du rdv a supprimer
         */
        public void supprimerRdv(String horaire, String nom) {
            Toast.makeText(mContext,
                    "Suppression du rendez-vous : " + nom + "prévu à " + horaire,
                    Toast.LENGTH_SHORT).show();

            rdvArrayList.remove(new Rdv(nom, horaire));
            afficherRdvs();
        }
    }


    }
