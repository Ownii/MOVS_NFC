package eu.ownii.movsnfctagreader;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity
{

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";

    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create nfc adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // check if nfc is enabled
        if( nfcAdapter == null || !nfcAdapter.isEnabled() )
        {
            Toast.makeText(this, "NFC disabled", Toast.LENGTH_LONG).show();
        }

        handleTag(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // handle incoming nfc message
        handleTag(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        // catch an incoming intent to prevent from starting a new activity
        final Intent intent = new Intent(this.getApplicationContext(), this.getClass());
        final PendingIntent pendingIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, intent, 0);

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    protected void onPause() {
        // stop checking for nfc tag
        nfcAdapter.disableForegroundDispatch(this);

        super.onPause();
    }

    private void handleTag(Intent intent){
        // cehck if action is a nfc tag
        String action = intent.getAction();
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)){

            // read data from nfc tag
            Toast.makeText(this, "NFC Tag detected", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Tag found: " + tag.toString());
            Log.d(TAG, "Id: " + bytesToHex(tag.getId()));
            for (String tech: tag.getTechList()) {
                Log.d(TAG, "Tech: " + tech);
            }
            NfcA nfca = NfcA.get(tag);
            try{
                nfca.connect();
                Short s = nfca.getSak();
                byte[] a = nfca.getAtqa();
                Log.d(TAG, "SAK = "+s+"\nATQA = "+bytesToHex(a));
                nfca.close();
            }
            catch(Exception e){
                Log.e(TAG, "Error when reading tag");
                Log.d(TAG, "handleTag: error");
            }
        }
    }

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
