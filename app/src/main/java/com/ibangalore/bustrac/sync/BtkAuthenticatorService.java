package com.ibangalore.bustrac.sync;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by ahegde on 4/22/15.
 */
public class BtkAuthenticatorService extends Service {
    private BtkAuthenticator mAuthenticator;

    @Override
    public void onCreate(){
        // Create a new authenticator object
        mAuthenticator = new BtkAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
