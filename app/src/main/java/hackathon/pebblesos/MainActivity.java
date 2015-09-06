package hackathon.pebblesos;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;


public class MainActivity extends ActionBarActivity {

    static final UUID uuid = UUID.fromString("264d708e-e2ec-4e47-9ffa-36c15e0ad2f1");
    private PebbleKit.PebbleDataReceiver sportsDataHandler = null;


    private void createNotif(String text){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("New Pebble Message")
                        .setContentText(text);
        //Vibration
        mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
        //LED
        mBuilder.setLights(Color.RED, 3000, 3000);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        int mId = 0;
        mNotificationManager.notify(mId, mBuilder.build());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView messages = (TextView)findViewById(R.id.text_messages);
        messages.setMovementMethod(new ScrollingMovementMethod());

        Button sendButton = (Button)findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();

                boolean isConnected = PebbleKit.isWatchConnected(context);

                if(isConnected) {

                    EditText textIn = (EditText)findViewById(R.id.text_input);

                    PebbleDictionary data = new PebbleDictionary();
                    // Add a key of 0, and a uint8_t (byte) of value 42.
                    data.addUint8(0, (byte) 42);
                    // Add a key of 1, and a string value.
                    data.addString(1, "boop");
                    String txt = textIn.getText().toString().trim();
                    data.addInt32(2, txt.length() > 0 ? txt.length() : -1);
                    data.addString(3, txt.toString());
                    PebbleKit.sendDataToPebble(getApplicationContext(), uuid, data);

                    TextView tv = (TextView)findViewById(R.id.text_messages);
                    tv.append("You: " + txt + "\n");

                    textIn.setText("");

                    //Toast.makeText(context, txt, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Watch is not connected!", Toast.LENGTH_LONG).show();
                }
            }
        });

        Button launchButton = (Button)findViewById(R.id.launch_button);
        launchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();

                boolean isConnected = PebbleKit.isWatchConnected(context);

                if(isConnected) {
                    // Launch the app
                    PebbleKit.startAppOnPebble(context, uuid);

                    Toast.makeText(context, "Launching...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Watch is not connected!", Toast.LENGTH_LONG).show();
                }
            }

        });

        PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(uuid) {

            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
                //Toast.makeText(context, "Received value=" + data.getString(1) + " for key: 1", Toast.LENGTH_SHORT).show();
                Log.i(getLocalClassName(), "Received value=" + data.getString(1) + " for key: 1");
                PebbleKit.sendAckToPebble(getApplicationContext(), transactionId);

                TextView tv = (TextView)findViewById(R.id.text_messages);
                if (data.getString(1) != null) {
                    tv.append("Pebble: " + data.getString(1) + "\n");
                    createNotif(data.getString(1));
                }
            }

        });
    }


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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume() {
        super.onResume();

        // Construct output String
        StringBuilder builder = new StringBuilder();
        builder.append("Pebble Info\n\n");

        // Is the watch connected?
        boolean isConnected = PebbleKit.isWatchConnected(this);
        builder.append("Watch connected: " + (isConnected ? "true" : "false")).append("\n");

        // What is the firmware version?
        PebbleKit.FirmwareVersionInfo info = PebbleKit.getWatchFWVersion(this);
        builder.append("Firmware version: ");
        builder.append(info.getMajor()).append(".");
        builder.append(info.getMinor()).append("\n");

        // Is AppMesage supported?
        boolean appMessageSupported = PebbleKit.areAppMessagesSupported(this);
        builder.append("AppMessage supported: " + (appMessageSupported ? "true" : "false"));

        TextView textView = (TextView)findViewById(R.id.text_view);
        //textView.setText(builder.toString());
    }



}
