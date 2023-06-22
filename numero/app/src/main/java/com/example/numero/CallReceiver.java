package com.example.numero;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallReceiver extends BroadcastReceiver {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitude;
    private double longitude;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action != null && action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

            if (phoneNumber != null) {
                phoneNumber = phoneNumber.substring(3);
                Log.d("CallReceiver", "Número de teléfono entrante: " + phoneNumber);
                // Iniciar la verificación de la duración de la llamada después de 10 segundos
                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                Log.d("CallReceiver", "Duración1: " + state);
                Log.d("CallReceiver", "Duración2: " + (TelephonyManager.EXTRA_STATE_RINGING));
                if (state != null && state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    Log.d("CallReceiver", "Duración: " + state);
                    startCallDurationCheck(context, phoneNumber);
                }

            } else {
                Log.d("CallReceiver", "Número de teléfono entrante desconocido");
                // Realiza alguna acción alternativa cuando no se proporciona un número de teléfono
            }
        }
    }

    private void startCallDurationCheck(final Context context, final String phoneNumber) {
        CountDownTimer countDownTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                Log.d("CallReceiver", "Contador: " + secondsRemaining);
            }

            @Override
            public void onFinish() {
                Log.d("CallReceiver", "Contador finalizado. Enviando SMS con ubicación.");
                sendSMSWithLocation(context, phoneNumber);
            }
        };

        countDownTimer.start();
    }

    private void sendSMSWithLocation(Context context, String phoneNumber) {
        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Log.d("CallReceiver", "Coordenadas: " + latitude + ", " + longitude);
                    sendLocationSMS(context, phoneNumber, latitude, longitude);
                    locationManager.removeUpdates(this);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {}
            };

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    private void sendLocationSMS(Context context, String phoneNumber, double latitude, double longitude) {
        SmsManager smsManager = SmsManager.getDefault();
        String message = "https://www.google.com/maps/search/" + latitude + "," + longitude;
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        Log.d("CallReceiver", "Mensaje enviado con ubicación: " + message);
    }
}
