package com.ids.idsuserapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.ids.idsuserapp.authentication.AutenticationFragment;
import com.ids.idsuserapp.db.entity.Tronco;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ids.idsuserapp.authentication.RegistrationActivity;
import com.ids.idsuserapp.entityhandlers.ArcoDataHandler;
import com.ids.idsuserapp.entityhandlers.BeaconDataHandler;
import com.ids.idsuserapp.entityhandlers.DataRetriever;
import com.ids.idsuserapp.entityhandlers.MappaDataHandler;
import com.ids.idsuserapp.percorso.BaseFragment;
import com.ids.idsuserapp.percorso.HomeFragment;
import com.ids.idsuserapp.services.LocatorService;
import com.ids.idsuserapp.threads.LocatorThread;
import com.ids.idsuserapp.percorso.Tasks.TaskListener;
import com.ids.idsuserapp.services.LocatorService;
import com.ids.idsuserapp.threads.LocatorThread;
import com.ids.idsuserapp.utils.ConnectionChecker;
import com.ids.idsuserapp.utils.PermissionsUtil;
import com.ids.idsuserapp.viewmodel.ArcoViewModel;
import com.ids.idsuserapp.viewmodel.BeaconViewModel;
import com.ids.idsuserapp.viewmodel.MappaViewModel;

public class HomeActivity extends AppCompatActivity implements DataRetriever {
    public static final String TAG = HomeActivity.class.getSimpleName();
    private MappaViewModel mappaViewModel;
    private BeaconViewModel beaconViewModel;
    private ArcoViewModel arcoViewModel;
    private BeaconDataHandler beaconDataHandler;
    private MappaDataHandler mappaDataHandler;
    private ArcoDataHandler arcoDataHandler;
    private PermissionsUtil permissionsUtil;
    private LocatorThread locatorThread;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 124;
    private static final int BT_ENABLED = 1;
    private boolean offline;
    public static final String OFFLINE_USAGE = "offline_usage";


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if (savedInstanceState == null) {
            HomeFragment homeFragment = new HomeFragment();
            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.fragment_layout, homeFragment, AutenticationFragment.TAG)
                    .commit();
        }
        permissionsUtil = new PermissionsUtil(this);
        setupMessageReception(savedInstanceState);
        setupViewModels();
        setupDataHandlers();
//        startLocatorThread();

        //controlla se la connessione ad internet è attiva dato l application context,
        //se si allora viene pulita la lista dei beacon e viene aggiornato il dataset
        if (ConnectionChecker.getInstance().isNetworkAvailable(getApplicationContext())) {
            getDatasetFromServer();
            permissionsUtil = new PermissionsUtil(this);
            if (permissionsUtil.requestEnableBt())
                startLocatorService(LocatorThread.STANDARD_MODE);
        }


    }

    private void setupDataHandlers() {
        beaconDataHandler = new BeaconDataHandler(this, beaconViewModel);
        mappaDataHandler = new MappaDataHandler(this, mappaViewModel);
        arcoDataHandler = new ArcoDataHandler(this, arcoViewModel, beaconViewModel);
    }

    private void setupViewModels() {
        beaconViewModel = new BeaconViewModel(getApplication());
        mappaViewModel = new MappaViewModel(getApplication());
        arcoViewModel = new ArcoViewModel(getApplication());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        locatorThread.interrupt();
    }

    private void getDatasetFromServer() {
        cleanDb();
        mappaDataHandler.retrieveMappeDataset();
    }

    private void cleanDb() {
        beaconViewModel.deleteAll();
        arcoViewModel.deleteAll();
        mappaViewModel.deleteAll();
    }

    @Override
    public void retrieveBeacons() {
        beaconDataHandler.retrieveBeaconDataset();
    }

    @Override
    public void retrieveArchi() {
        arcoDataHandler.retrieveArchiDataset();
    }

    private void setupMessageReception(Bundle savedInstanceState) {
        offline = true;

         /* if (!offline) {
          // Handle deviceToken for pushNotification
            // [START handle_device_token]
            SaveDeviceTokenTask task = new SaveDeviceTokenTask(this, new TaskListener<Void>() {
                @Override
                public void onTaskSuccess(Void aVoid) {
                    Log.d(TAG, "Device key save succesfully");
                }

                @Override
                public void onTaskError(Exception e) {
                    Log.e(TAG, "Save deviceKey error", e);
                }

                @Override
                public void onTaskComplete() {
                }

                @Override
                public void onTaskCancelled() {
                }
            });
            task.execute();
            // [END handle_device_token]
        }*/
    }

    /**
     * Cambia il fragment
     *
     * @param fragment
     */
    public void changeFragment(BaseFragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.navigation_content_pane, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(fragment.TAG)
                .commit();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BT_ENABLED:
                if (resultCode == RESULT_OK)
                    startLocatorService(LocatorThread.STANDARD_MODE);
                else
                    permissionsUtil.btAlert();

        }
    }

    private void startLocatorService(int mode) {
        Intent serviceIntent = new Intent(this, LocatorService.class);
        serviceIntent.setAction(Integer.toString(mode));
        startService(serviceIntent);
    }


    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setMessage("Sei sicuro di voler uscire?");
        builder.setCancelable(true);
        builder.setPositiveButton("Sì", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}







