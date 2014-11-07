package io.reach.reachiodemo.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.reach.reachiodemo.R;

/**
 * Created by Jin on 11/7/14.
 */
public class SecondFragment extends android.support.v4.app.Fragment {
    Activity mActivity;

    @Override
    public void onAttach(Activity act) {
        super.onAttach(act);
        mActivity = act;
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second, container, false);


        return view;
    }
}
