package com.example.jduong321.funclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import com.example.jduong321.funcenter.FunInterface;

import static com.example.jduong321.funclient.R.id.listView;


public class FunClient extends AppCompatActivity {

    private boolean isBound; // marks whether service has been bound or not
    private Button playButton;
    private Button pauseButton;
    private Button resumeButton;
    private Button stopButton;
    private Button displayButton;
    private Button clearButton;
    private ImageView display;
    private EditText input;
    private ListView trackList;
    private Bitmap mBitmap;

    private FunInterface FunService; // client's reference
    // to the service object

    private ArrayList<String> clientTransactions;    // list of user requests
    private ArrayAdapter clientTransactionsAdapter;  // adapter for the above list

    public class ReadPageTask extends AsyncTask<String,Integer,Bitmap> {

            @Override
            protected Bitmap doInBackground(String... strings) {
                URL aUrl = null ;
                Bitmap result = null ;

                try {
                    aUrl = new URL(strings[0]);
                    result = BitmapFactory.decodeStream((InputStream) aUrl.getContent()) ;
                    ;}
                catch (Exception e) { System.out.println("Could not read web site: " + strings[0] + e  + ".") ; } ;



                return result;
            }



            // This method is executed in the UI thread after doInBackground() has returned
            protected void onPostExecute(Bitmap bitmap) {

                display.setImageBitmap(bitmap) ;
                Toast.makeText(getApplicationContext(),
                        "Displayed",
                        Toast.LENGTH_SHORT).show();
            }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fun_client);

        playButton = (Button) findViewById(R.id.playButton);
        pauseButton = (Button) findViewById(R.id.pauseButton);
        resumeButton = (Button) findViewById(R.id.resumeButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        displayButton = (Button) findViewById(R.id.displayButton);
        clearButton = (Button) findViewById(R.id.clearButton);



        display = (ImageView) findViewById((R.id.image));
        input = (EditText) findViewById(R.id.input);

        trackList = (ListView) findViewById(listView);


        //see if there is a file and update the listview
        FileInputStream fis;
        try {
            fis = openFileInput("listview.txt");
            ObjectInputStream ois = new ObjectInputStream(fis);
            clientTransactions = (ArrayList<String>) ois.readObject();
            clientTransactionsAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, clientTransactions);
            trackList.setAdapter(clientTransactionsAdapter);
            ois.close();
        } catch (FileNotFoundException e) {
            Log.i(FunClient.class.getSimpleName(),"FILE1 NOT FOUND");
        } catch (IOException e) {
            Log.i(FunClient.class.getSimpleName(),"FILE2 NOT FOUND");
        } catch (ClassNotFoundException e) {
            Log.i(FunClient.class.getSimpleName(),"FILE3 NOT FOUND");
        }


        displayButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v){
                int num = Integer.parseInt(input.getText().toString());
                if(num <1 || num > 3)
                {
                    Toast.makeText(getApplicationContext(),
                            "Enter a value from 1 to 3",
                            Toast.LENGTH_SHORT).show();
                    clientTransactions.add("Picture could not show #");
                    clientTransactionsAdapter.notifyDataSetChanged();
                    return;
                }
                else {
                    try {
                        String url = FunService.display(num);
                        new ReadPageTask().execute(url);
                        clientTransactions.add("Picture #"+num);
                        clientTransactionsAdapter.notifyDataSetChanged();
                    } catch (RemoteException ex) {
                        Toast.makeText(getApplicationContext(),
                                "Remote Exception occurred",
                                Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v){
                clientTransactions.clear();
                clientTransactionsAdapter.notifyDataSetChanged();
            }
        });


        // play track1 when this button is pressed and add the transaction to the list
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int num = Integer.parseInt(input.getText().toString());
                if (num < 1 || num > 3)     // boundary checking
                {
                    Toast.makeText(getApplicationContext(),
                            "Enter a value from 1 to 3",
                            Toast.LENGTH_SHORT).show();
                    clientTransactions.add("MediaPlayer could not play #");
                    clientTransactionsAdapter.notifyDataSetChanged();
                    return;
                } else {
                    try {
                        FunService.play(num);
                    } catch (RemoteException ex) {
                        Toast.makeText(getApplicationContext(),
                                "Remote Exception occurred",
                                Toast.LENGTH_SHORT).show();
                    }

                    clientTransactions.add("Play track" + num);
                    clientTransactionsAdapter.notifyDataSetChanged();
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(!FunService.getStatus())
                    {
                        Toast.makeText(getApplicationContext(),
                                "Song not playing",
                                Toast.LENGTH_SHORT).show();
                        clientTransactions.add("MediaPlayer tried to pause");
                        clientTransactionsAdapter.notifyDataSetChanged();
                    }
                    else {
                        FunService.pause();
                        clientTransactions.add("MediaPlayer paused");
                        clientTransactionsAdapter.notifyDataSetChanged();
                    }
                } catch (RemoteException ex)
                {
                    Toast.makeText(getApplicationContext(),
                            "Remote Exception occurred",
                            Toast.LENGTH_SHORT).show();
                }


            }
        });


        // stop current track when this button is pressed and add the transaction to the list
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(!FunService.getStatus() && FunService.getPosition() >0)
                    {
                        FunService.stop();
                        clientTransactions.add("MediaPlayer stopped");
                        clientTransactionsAdapter.notifyDataSetChanged();

                    }
                    else if(!FunService.getStatus() && FunService.getPosition() ==0)
                    {
                        Toast.makeText(getApplicationContext(),
                                "Song not playing",
                                Toast.LENGTH_SHORT).show();
                        clientTransactions.add("MediaPlayer tried to stop");
                        clientTransactionsAdapter.notifyDataSetChanged();
                    }
                    else {
                        FunService.stop();
                        clientTransactions.add("MediaPlayer stopped");
                        clientTransactionsAdapter.notifyDataSetChanged();
                    }
                } catch (RemoteException ex)
                {
                    Toast.makeText(getApplicationContext(),
                            "Remote Exception occurred",
                            Toast.LENGTH_SHORT).show();
                }


            }
        });


        // resume current track when this button is pressed and add the transaction to the list
        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(FunService.getStatus())
                    {
                        Toast.makeText(getApplicationContext(),
                                "Song already playing",
                                Toast.LENGTH_SHORT).show();
                        clientTransactions.add("MediaPlayer tried to resume");
                        clientTransactionsAdapter.notifyDataSetChanged();
                    }
                    else {

                        FunService.resume();

                        if(FunService.getStatus())
                        {
                            clientTransactions.add("MediaPlayer resumed");
                            clientTransactionsAdapter.notifyDataSetChanged();
                        }
                        else
                        {
                            clientTransactions.add("MediaPlayer tried to resumed");
                            clientTransactionsAdapter.notifyDataSetChanged();
                        }


                    }
                } catch (RemoteException ex)
                {
                    Toast.makeText(getApplicationContext(),
                            "Remote Exception occurred",
                            Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    // create the ServiceConnection object to initialize the client's reference
    // to the service object
    private final ServiceConnection mConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder iservice)
        {
            FunService = FunInterface.Stub.asInterface(iservice);
            isBound = true;
        }

        public void onServiceDisconnected(ComponentName className)
        {
            FunService = null;
            isBound = false;
        }
    };

    @Override
    protected void onResume()
    {
        super.onResume();

        if (!isBound)
        {
            boolean b = false;

            // create an explicit intent and add supplemental information to it in
            // order to bind the client to the service
            Intent serviceIntent = new Intent(FunInterface.class.getName());

            // this follows the intent-filter for the service to ensure the proper
            // client to service binding
            serviceIntent.setAction("FunCenter.service");

            ResolveInfo info = getPackageManager().resolveService(serviceIntent,
                    Context.BIND_AUTO_CREATE);

            serviceIntent.setComponent(new ComponentName(info.serviceInfo.packageName,
                    info.serviceInfo.name));

            b = bindService(serviceIntent, this.mConnection, Context.BIND_AUTO_CREATE);

            if(b)
                Toast.makeText(this, "Service bound", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Service could not be bound", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onDestroy()
    {
        if (isBound) { unbindService(this.mConnection); }

        super.onDestroy();
    }

    @Override
    public void onPause()
    {
        //if app stops for any reason store listview for next time
        FileOutputStream fos;
        try {
            fos = getApplicationContext().openFileOutput("listview.txt", Context.MODE_PRIVATE);

            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(clientTransactions);
            Log.i(FunClient.class.getSimpleName(),"WOKE");
            oos.close();
        } catch (FileNotFoundException e) {
            Log.i(FunClient.class.getSimpleName(),"FILE NOT FOUND");
        }catch(IOException e){
            Log.i(FunClient.class.getSimpleName(),"FILE BROKE");
        }
        super.onPause();
    }
}
