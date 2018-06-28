package com.ids.idsuserapp.autenticazione;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toolbar;

import com.ids.idsuserapp.HomeActivity;
import com.ids.idsuserapp.LogoActivity;
import com.ids.idsuserapp.R;
import com.ids.idsuserapp.percorso.BaseFragment;
import com.ids.idsuserapp.percorso.HomeFragment;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement thegit
 * Use the {@link AutenticationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AutenticationFragment extends Fragment {

    public static final String TAG = AutenticationFragment.class.getSimpleName();
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private AutenticationFragment.ViewHolder holder;

    public AutenticationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AutenticationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AutenticationFragment newInstance(String param1, String param2) {
        AutenticationFragment fragment = new AutenticationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_autentication, container, false);
        holder = new ViewHolder(view);
        return view;
    }




    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }


    /**
     * Classe wrapper degli elementi della vista
     */
    public class ViewHolder extends BaseFragment.ViewHolder {

       //public final Toolbar toolbar;
       public final Button offlineButton;
       public final Button loginButton;
       public final Button registerButton;


       public ViewHolder(View v){

         //  toolbar = v.findViewById((R.id.navigation_toolbar));
           offlineButton = v.findViewById(R.id.offline_btn);
           loginButton= v.findViewById(R.id.login_btn);
           registerButton=v.findViewById(R.id.register_btn);



          offlineButton.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {


                   Intent intent = new Intent(getActivity(), HomeActivity.class);
                   startActivity(intent);
                   }
           });




          loginButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {


                  Intent intent = new Intent(getActivity(), LoginActivity.class);
                  startActivity(intent);
              }

          });


          registerButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Intent intent = new Intent(getActivity(),RegistrationActivity.class);
                  startActivity(intent);
              }
          });


       }



    }


}
