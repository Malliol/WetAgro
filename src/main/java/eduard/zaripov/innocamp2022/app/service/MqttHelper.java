package eduard.zaripov.innocamp2022.app.service;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.android.service.MqttTraceHandler;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Callable;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import eduard.zaripov.innocamp2022.R;
import eduard.zaripov.innocamp2022.model.Thing;


/**
 * Сюда не лезьте)
 */

public class MqttHelper {

    private static MqttAndroidClient mqttAndroidClient;
    private static String serverUri = "ssl://mqtt.cloud.yandex.net:8883";
    private static String TAG = "Yandex IoTCore Demo";
    private static final String clientId = "YandexIoTCoreAndroidTextClient";
    private static String[] subscribeTopic = {"$devices/are9gnqohp4npug37mbs/events/raw","$devices/are1suqff6jhlala2bsh/events/raw", "$devices/areg5dfne7179n4o24q2/events/raw"};
    private static String mqttUserName = "aresmv64htqk8lkmqr61";
    private static String mqttPassword = "ICLinnocamp2022";
    private static final int connectionTimeout = 60;
    private static final int keepAliveInterval = 60;

    private static String content = "";

    private static SSLSocketFactory getSocketFactory(final InputStream caCrtFile) throws Exception {

        // Load CA certificate
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate caCert = (X509Certificate) cf.generateCertificate(caCrtFile);

        // CA certificate is used to authenticate server
        KeyStore serverCaKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        serverCaKeyStore.load(null, null);
        serverCaKeyStore.setCertificateEntry("ca", caCert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(serverCaKeyStore);


        return new AdditionalKeyStoresSSLSocketFactory(null, serverCaKeyStore);
    }

    public static String getContent() {
        return content;
    }

    public static void connect(Context applicationContext, Runnable callback) {

        // Create mqttClient
        mqttAndroidClient = new MqttAndroidClient(applicationContext, serverUri, clientId);

        // Configure connection options
        MqttConnectOptions options = new MqttConnectOptions();
        options.setConnectionTimeout(connectionTimeout);
        options.setKeepAliveInterval(keepAliveInterval);

        SSLSocketFactory sslSocketFactory = null;


        // Use this to connect using username and password

        options.setUserName(mqttUserName);
        options.setPassword(mqttPassword.toCharArray());

        try {
            sslSocketFactory = getSocketFactory(
                    applicationContext.getResources().openRawResource(R.raw.root_ca));
        } catch (Exception e) {
            e.printStackTrace();
        }


        options.setSocketFactory(sslSocketFactory);

        // Enable mqtt client trace
        mqttAndroidClient.setTraceEnabled(true);
        mqttAndroidClient.setTraceCallback(new MqttTraceHandler() {
            @Override
            public void traceDebug(String tag, String message) {
                Log.d(tag, message);
            }

            @Override
            public void traceError(String tag, String message) {
                Log.e(tag, message);
            }

            @Override
            public void traceException(String tag, String message, Exception e) {
                Log.e(tag, message);
            }
        });

        // Set mqtt client callbacks
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "Connection lost: " + cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                String str = new String(message.getPayload(), "UTF-8");

                content = str;
                callback.run();
                Log.i(TAG, "Received new message: " + str);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "Delivery complete");
            }
        });


        // Connect to the server
        Log.i(TAG, "Starting connect the server...");

        try {
            mqttAndroidClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    Log.i(TAG, "Connected");

                    int[] qos = {1,1,1};

                    IMqttToken subToken = null;

                    Log.i(TAG, "Subscribe to " + subscribeTopic);
                    // Subscribe in case off success connection
                    try {
                        subToken = mqttAndroidClient.subscribe(subscribeTopic, qos);

                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                Log.i(TAG, "Subscribe complete");
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken,
                                                  Throwable exception) {
                                // The subscription could not be performed, maybe the user was not
                                // authorized to subscribe on the specified topic e.g. using wildcards
                                Log.i(TAG, "Failed to subscribe to: " + subscribeTopic + " " + exception.getMessage());

                            }
                        });

                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "Failed to connect to " + serverUri + " " + exception.getMessage());
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, "Mqtt exception");
        }
    }
}
