package com.example.moham.whereyouare1;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PICK_CONTACT = 0;
    PendingIntent pi_sent, pi_delivered;
    BroadcastReceiver sent_receiver, delivered_receiver;
    EditText editText;
    Button button_send;
    TextView text_view;
    private String message = "";
    ProgressDialog dialog;
    LocationManager locationManager;
    private Button button_ask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button_send = (Button) findViewById(R.id.btn_id);
        button_ask = (Button) findViewById(R.id.askforloc_btn_id);
        editText = (EditText) findViewById(R.id.edittxt_id);
        text_view = (TextView) findViewById(R.id.click_to_showcontacts_txt_id);
        dialog = new ProgressDialog(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }

        try {

            String sender_phone = (String) getIntent().getExtras().get("sender_phone");
            editText.setText(sender_phone);
        } catch (Exception e) {

        }

        pi_sent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
        pi_delivered = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);


        button_send.setOnClickListener(this);
        button_ask.setOnClickListener(this);
        text_view.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        sent_receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case RESULT_OK:
                        Toast.makeText(MainActivity.this, "sent correctly", Toast.LENGTH_SHORT).show();
                        editText.setText("");
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "error in sending", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        delivered_receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case RESULT_OK:
                        Toast.makeText(MainActivity.this, "delivered correctly", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "error in delivering", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        registerReceiver(sent_receiver, new IntentFilter("SMS_SENT"));
        registerReceiver(delivered_receiver, new IntentFilter("SMS_DELIVERED"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(sent_receiver);
        unregisterReceiver(delivered_receiver);
    }

    private void sendSMS(String phoneNumber, String message) throws Exception {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, pi_sent, pi_delivered);
    }

    private void getloc() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps_getloc();
                return;
            }
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setBearingRequired(true);
                criteria.setCostAllowed(true);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                criteria.setAltitudeRequired(false);
                String bestProvider = locationManager.getBestProvider(criteria, true);
                locationManager.requestLocationUpdates(bestProvider, 2000, 10, new LocationListener() {
                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {
                    }

                    @Override
                    public void onProviderEnabled(String s) {
                    }

                    @Override
                    public void onProviderDisabled(String s) {
                    }

                    @Override
                    public void onLocationChanged(final Location location) {
                    }
                });
                Location myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                double longitude = myLocation.getLongitude();
                double latitude = myLocation.getLatitude();
                message = latitude + "-" + longitude + "-xy";
            }
        }


    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS must be enabled to continue, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void buildAlertMessageNoGps_getloc() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS must be enabled to continue, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public void onClick(View v) {
        if (v == button_send) {
            try {
                getloc();
                sendSMS(editText.getText().toString(), message);
            } catch (NullPointerException f) {
                Toast.makeText(MainActivity.this, "gps data  not provided now, try after 10 seconds ", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (v == button_ask) {
            message = "m-m-xy";
            try {
                sendSMS(editText.getText().toString(), message);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (v == text_view) {
            readcontact();

        }

    }

    public void readcontact() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            startActivityForResult(intent, PICK_CONTACT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c = getContentResolver().query(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                    null, null);
                            phones.moveToFirst();
                            editText.setText(phones.getString(phones.getColumnIndex("data1")));

                        }
                    }
                }
                break;
        }

    }
}
