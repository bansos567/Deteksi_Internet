package com.tamoda.network;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

import java.net.HttpURLConnection;
import java.net.URL;

@DesignerComponent(
    version = 1,
    description = "Tamoda Koneksi Elite: Deteksi Akurat Kuota Habis & WiFi Mati (Senyap, Tanpa Toast).",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = ""
)
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.ACCESS_NETWORK_STATE")
public class TamodaKoneksi extends AndroidNonvisibleComponent {

    private Context context;
    private Handler uiHandler;

    public TamodaKoneksi(ComponentContainer container) {
        super(container.$form());
        this.context = container.$context();
        this.uiHandler = new Handler(Looper.getMainLooper());
    }

    @SimpleFunction(description = "Mulai proses pengecekan internet asli secara senyap.")
    public void CekInternet() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean hasInternet = false;
                String message = "Data & WiFi Mati";

                try {
                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

                    if (activeNetwork != null && activeNetwork.isConnected()) {
                        HttpURLConnection urlc = (HttpURLConnection) (new URL("https://clients3.google.com/generate_204").openConnection());
                        urlc.setRequestProperty("User-Agent", "Android");
                        urlc.setRequestProperty("Connection", "close");
                        urlc.setConnectTimeout(2000); 
                        urlc.setReadTimeout(2000);    
                        urlc.connect();
                        
                        if (urlc.getResponseCode() == 204 && urlc.getContentLength() == 0) {
                            hasInternet = true;
                            message = "Internet Lancar";
                        } else {
                            message = "Logo Nyala, Tapi Kuota Habis / WiFi Perlu Login";
                        }
                        urlc.disconnect(); 
                    }
                } catch (Exception e) {
                    message = "Koneksi Gagal: Sinyal Lemah atau Kuota Habis";
                }

                final boolean finalHasInternet = hasInternet;
                final String finalMessage = message;

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        SetelahDicek(finalHasInternet, finalMessage);
                    }
                });
            }
        }).start();
    }

    @SimpleEvent(description = "Event ini terpicu senyap setelah proses CekInternet selesai.")
    public void SetelahDicek(boolean AdaInternet, String PesanStatus) {
        EventDispatcher.dispatchEvent(this, "SetelahDicek", AdaInternet, PesanStatus);
    }
}
