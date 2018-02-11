package com.example.bhavya.younote_1;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


// Following code for embeding youtube player is referenced from https://www.youtube.com/watch?v=W4hTJybfU7s
// Need to extends YouTubeBaseActivity to use youtube API
public class YouNoteActivity extends YouTubeBaseActivity {


    private YouTubePlayerView mYouTubePlayerView;
    private Button btnPlay, btnTakeNote, btnInitialize, btnRegister;
    private YouTubePlayer.OnInitializedListener mOnInitializedListener;
    private YouTubePlayer player;
    private ListView lvNotes;
    private ArrayAdapter<String> lvNotesItemAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPlay = (Button) findViewById(R.id.btnplay);
        btnTakeNote = (Button) findViewById(R.id.btnTakeNote);
        btnInitialize = (Button) findViewById(R.id.btnInitialize);
        mYouTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtubeplayer);

        lvNotes = (ListView) findViewById(R.id.lvNotes);
        //sets the layout for each item of the list view
        lvNotesItemAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        //binds data from the Adapter to the ListView
        lvNotes.setAdapter(lvNotesItemAdapter);
        //set onItemClickListener to the lvNotes to allow user going back to the time point when the
        //note was taken
        //This is referenced from: https://www.youtube.com/watch?v=XyxT8IQoZkc
        lvNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //TODO: use note class object to get the actual elapsed time at note taken
                //below is a temporary way to calculate the note time
                String noteItem = lvNotesItemAdapter.getItem(i).trim();
                int hr = Integer.parseInt(noteItem.substring(0, 2));
                int min = Integer.parseInt(noteItem.substring(3, 5));
                int sec = Integer.parseInt(noteItem.substring(6, 8));
                int msNoteTime = (hr*3600 + min*60 + sec) * 1000;

                //play the video back to the note time
                player.seekToMillis(msNoteTime);
            }
        });

        mOnInitializedListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                //youTubePlayer.loadVideo("W4hTJybfU7s"); //load and autoplay the video

                // When youtube is initialized successfully, set player to youTubePlayer
                // Then, player can be used for playing, pausing videos, etc.
                player = youTubePlayer;
                //player.loadVideo("W4hTJybfU7s");
                player.cueVideo("W4hTJybfU7s"); //load but doesn't autoplay the video
                //youTubePlayer.loadVideo("vk1Rk5AaDO4");
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        };

        // A Initialize button is for initializing the youtube player
        btnInitialize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mYouTubePlayerView.initialize(YouNoteconfig.getApiKey(), mOnInitializedListener);
            }
        });

        // A play button for playing the video
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                player.play();
            }
        });

        // A TakeNote button automatically stops the video to allow user taking notes
        btnTakeNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.pause();

                // Take elapsed time (milliseconds) when pause and convert it to hh:mm:ss format
                long elapsedTime = player.getCurrentTimeMillis();
                int hour = (int) (elapsedTime/(1000*3600));
                int min = (int) (elapsedTime/60000) % 60;
                int second = (int) (elapsedTime/1000) % 60;
                @SuppressLint("DefaultLocale") final String elapsedTimeString = String.format("%02d:%02d:%02d", hour, min, second);


                // Opens up a EditText for taking notes
                // Codes are referenced from: https://www.youtube.com/watch?v=BXTanDpOTVU
                View view = (LayoutInflater.from(YouNoteActivity.this)).inflate(R.layout.user_input,null);


                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(YouNoteActivity.this);
                alertBuilder.setView(view);
                final EditText userNoteInput = (EditText) view.findViewById(R.id.usrNoteInput);
                final EditText userSubjectInput = (EditText) view.findViewById(R.id.subject);

                // Add the elapsed time to the TextView in the popped up note-taking view by default
                TextView timeAtPause = (TextView) view.findViewById(R.id.elapsedTime);
                timeAtPause.setText(elapsedTimeString);

                alertBuilder.setCancelable(true).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //TODO: need a note class to store every note's info. A note class object is
                        //TODO: needed to store the user input notes.

                        //String noteItemString = String.format("%s   %s", elapsedTimeString, userSubjectInput.getText().toString());
                        String noteItemString = String.format("%s   %s\nNotes: %s\n", elapsedTimeString, userSubjectInput.getText().toString(),userNoteInput.getText().toString());
                        lvNotesItemAdapter.add(noteItemString);

                    }
                });

                Dialog dialog = alertBuilder.create();
                dialog.show();
            }
        });

    }


}
