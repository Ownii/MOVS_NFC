package eu.ownii.movsnfc;

import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static android.nfc.NdefRecord.createMime;

public class MainActivity extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback
{

    public static final String TAG = "MainDebug";

    NfcAdapter mNfcAdapter;
    private EditText editText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.etText);
        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // check if NFC is enabled
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // Register callback which gets called when android beam gets activated
        mNfcAdapter.setNdefPushMessageCallback(this, this);
        //mNfcAdapter.setNdefPushMessage(createNdefMessage(), this, this);
    }

    // Need for sending
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String text = (editText.getText().toString());
        // Create Ndef message which can get received
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { createMime(
                        "application/vnd.eu.ownii.movsnfc", text.getBytes())
                });
        return msg;
    }

    // Needed to receive
    @Override
    public void onResume() {
        super.onResume();
        // detects if activity got called by ndef/nfc
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    // needed to receive
    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    /**
     * proceed the ndef message
     * @param intent the intent with the ndef message
     */
    void processIntent(Intent intent) {
        // get msg from intent
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        String message = new String(msg.getRecords()[0].getPayload());
        // check if received message is an url
        if (URLUtil.isValidUrl(message))
        {
            // open this url
            Intent i = new Intent();
            i.setAction(Intent.ACTION_VIEW);
            i.setData(Uri.parse(message));
            startActivity(i);
            finish();
        }
        else
        {
            // show message
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
}
