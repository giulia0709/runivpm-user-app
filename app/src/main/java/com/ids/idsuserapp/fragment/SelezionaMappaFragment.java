package com.ids.idsuserapp.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ids.idsuserapp.HomeActivity;
import com.ids.idsuserapp.R;
import com.ids.idsuserapp.adapters.BeaconRecyclerAdapter;
import com.ids.idsuserapp.db.entity.Beacon;
import com.ids.idsuserapp.db.entity.Mappa;
import com.ids.idsuserapp.db.entity.Position;
import com.ids.idsuserapp.percorso.BaseFragment;
import com.ids.idsuserapp.percorso.HomeFragment;
import com.ids.idsuserapp.percorso.Tasks.MappaStaticaTask;
import com.ids.idsuserapp.percorso.Tasks.SelectablePointsTask;
import com.ids.idsuserapp.percorso.Tasks.TaskListener;
import com.ids.idsuserapp.percorso.views.MapPin;
import com.ids.idsuserapp.percorso.views.PinView;
import com.ids.idsuserapp.utils.UnitConverter;
import com.ids.idsuserapp.viewmodel.BeaconViewModel;

import org.apache.commons.lang3.SerializationUtils;


public class SelezionaMappaFragment extends BaseFragment {

    public static final int STARTING_FLOOR = 155;
    public static final int ORIGIN_SELECTION_REQUEST_CODE = 200;
    public static final int DESTINATION_SELECTION_REQUEST_CODE = 201;

    public static final int POSITION_ACQUIRED = 1;
    public static final int POSITION_NOT_ACQUIRED = 0;
    public static final String TAG = SelezionaMappaFragment.class.getSimpleName();
    private static final int SEARCH_RADIUS_IN_DP = 100;
    public static final String OFFLINE_USAGE = "offline_usage";
    private AsyncTask<Integer, Void, Boolean> mapsTask;
    private ViewHolder holder;
    private static final String SELECTION_REQUEST_CODE = "selection_request_code";
    private static final String ALREADY_SELECTED_NODE = "already_selected_node";
    private int currentFloor = STARTING_FLOOR;
    private Beacon selectedNode = null;
    private boolean offline;
    private BeaconViewModel mBeaconViewModel;
    private BeaconRecyclerAdapter beaconRecyclerAdapter;

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     *
     * @return A new instance of fragment ResetPasswordFragment.
     */
    public static SelezionaMappaFragment newInstance(int requestCode, Beacon alreadySelectedBeacon, boolean offline) {

        SelezionaMappaFragment fragment = new SelezionaMappaFragment();
        Bundle args = new Bundle();
        args.putInt(SELECTION_REQUEST_CODE, requestCode);
        args.putSerializable(ALREADY_SELECTED_NODE, alreadySelectedBeacon);
        args.putSerializable(OFFLINE_USAGE, offline);
        fragment.setArguments(args);


        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seleziona_mappa, container, false);
        holder = new ViewHolder(view);
        offline = getArguments().getBoolean(OFFLINE_USAGE);

        switch (getTargetRequestCode()) {
            case ORIGIN_SELECTION_REQUEST_CODE:
                holder.toolbarTitle.setText("Seleziona Origine");
                break;
            case DESTINATION_SELECTION_REQUEST_CODE:
                holder.toolbarTitle.setText("Seleziona Destinazione");
                break;
        }

        toggleConfirmButtonState();

        if(offline) {
            mapsTask = new MappaStaticaTask(getContext(), new MapsDownloaderListener());
        } else {
            mapsTask = new MappaStaticaTask(getContext(), new MapsDownloaderListener());
        }

        mapsTask.execute(STARTING_FLOOR);
        holder.floor155Button.setTextColor(color(R.color.linkText));

        holder.floor155Button.setOnClickListener(new FloorButtonListener());
        holder.floor150Button.setOnClickListener(new FloorButtonListener());
        holder.floor145Button.setOnClickListener(new FloorButtonListener());

        // Setup back button
        holder.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        holder.confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPositionConfirm(selectedNode);
            }
        });

        return view;
    }

    private void toggleConfirmButtonState() {
        if (selectedNode == null) {
            disableConfirmButtonState();
        } else {
            holder.confirmButton.setEnabled(true);
            holder.confirmButton.setTextColor(color(R.color.linkText));
        }
    }

    public void onPositionConfirm(Beacon node) {
        Intent data = new Intent(getContext(), HomeActivity.class);
        data.putExtra(HomeFragment.INTENT_KEY_POSITION, SerializationUtils.serialize(node));
        getTargetFragment().onActivityResult(getTargetRequestCode(), POSITION_ACQUIRED, data);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack();
        }

    private void disableConfirmButtonState() {
        holder.confirmButton.setEnabled(false);
        holder.confirmButton.setTextColor(color(R.color.disabledText));
    }

    private class FloorButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Button button = (Button) v;
            holder.floor155Button.setTextColor(color(R.color.colorWhite));
            holder.floor150Button.setTextColor(color(R.color.colorWhite));
            holder.floor145Button.setTextColor(color(R.color.colorWhite));
            button.setTextColor(color(R.color.linkText));
            int floor = Integer.parseInt(button.getText().toString().substring(6));

            currentFloor = floor;

            if (mapsTask == null) {
                if(offline) {
                    mapsTask = new MappaStaticaTask(getContext(), new MapsDownloaderListener());
                } else {
                    //mapsTask = new MapsDownloaderTask(getContext(), new MapsDownloaderListener());
                }
                mapsTask.execute(floor);
            }
        }
    }

    private class MapsDownloaderListener implements TaskListener<Mappa> {

        @Override
        public void onTaskSuccess(Mappa map) {
            int floor = Integer.parseInt(map.getName().substring(6));
            Bitmap bitmap;
            switch (floor) {
                case 145:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.floor_145);
                    break;
                case 150:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.floor_150);
                    break;
                case 155:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.floor_155);
                    break;
                default:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.floor_155);
                    break;
            }

            holder.mapView.setImage(bitmap);
            holder.mapView.setMinimumDpi(40);
            holder.mapView.resetPins();
            disableConfirmButtonState();

            final GestureDetector gestureDetector = new GestureDetector(getActivity(), new MapGestureDetector());

            holder.mapView.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });

        }

        private class MapGestureDetector extends GestureDetector.SimpleOnGestureListener {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (holder.mapView.isReady()) {
                    PointF tappedCoordinates = holder.mapView.viewToSourceCoord(e.getX(), e.getY());
                    Position tappedPosition = new Position(tappedCoordinates.x, tappedCoordinates.y, currentFloor);
                    mBeaconViewModel = ViewModelProviders.of(SelezionaMappaFragment.this).get(BeaconViewModel.class);

                    SelectablePointsTask selectablePointsTask = new SelectablePointsTask(
                            new SelectablePointsListener(),
                            (int) UnitConverter.convertDpToPixel(SEARCH_RADIUS_IN_DP, getContext()), mBeaconViewModel, getContext());
                    selectablePointsTask.execute(tappedPosition);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Image is not ready",
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        }

        private class SelectablePointsListener implements TaskListener<Beacon> {

            @Override
            public void onTaskSuccess(Beacon node) {
                selectedNode = node;
                holder.selectedNode.setText(node.getNome());
                holder.mapView.setSinglePin(new MapPin((float) node.getX(), (float) node.getY(), 0));
                toggleConfirmButtonState();

                Toast.makeText(getActivity().getApplicationContext(), "Hai selezionato il nodo: " + node.getNome(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTaskError(Exception e) {
                if (e == null) {
                    Toast.makeText(getActivity().getApplicationContext(), "Non puoi selezionare questo nodo",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Errore di selezione", e);
                }
            }

            @Override
            public void onTaskComplete() {

            }

            @Override
            public void onTaskCancelled() {

            }
        }

        @Override
        public void onTaskError(Exception e) {
            Toast.makeText(getContext(), "Ci sono stati degli errori nel download dell'immagine", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        @Override
        public void onTaskComplete() {
            mapsTask = null;
        }

        @Override
        public void onTaskCancelled() {
            mapsTask = null;
        }
    }


    public class ViewHolder {
        public final PinView mapView;
        public final Button floor155Button;
        public final Button floor150Button;
        public final Button floor145Button;
        public final View actionButtonsContainer;
        public final Button backButton;
        public final Button confirmButton;
        public final TextView toolbarTitle;
        public final TextView selectedNode;

        @SuppressWarnings("unchecked")
        public <T extends View> T find(View view, int id) {
            View resultView = view.findViewById(id);
            try {
                return (T) resultView;
            } catch (ClassCastException e) {
                return null;
            }
        }

        public ViewHolder(View v) {
            mapView = find(v, R.id.navigation_map_image);
            floor155Button = find(v, R.id.floor_button_155);
            floor150Button = find(v, R.id.floor_button_150);
            floor145Button = find(v, R.id.floor_button_145);
            actionButtonsContainer = find(v, R.id.action_buttons_container);
            backButton = find(v, R.id.back_button);
            confirmButton = find(v, R.id.confirm_button);
            toolbarTitle = find(v, R.id.toolbar_title);
            selectedNode = find(v, R.id.node);

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
