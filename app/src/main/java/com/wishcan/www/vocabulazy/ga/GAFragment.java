package com.wishcan.www.vocabulazy.ga;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.wishcan.www.vocabulazy.VLApplication;

/**
 * GAFragment is an abstract class used for sending Lifecycle event to Google Analysis.
 * All fragments extends the GAFragment will automatically sending their Lifecycle event as long as
 * they implement {@link GAFragment#getNameAsGaLabel()}
 */
public abstract class GAFragment extends Fragment {

    public interface GAFragmentLifeCycle {
        int ON_CREATE               = 0;
        int ON_CREATE_VIEW          = 1;
        int ON_VIEW_CREATED         = 2;
        int ON_START                = 3;
        int ON_RESUME               = 4;
        int ON_PAUSE                = 5;
        int ON_STOP                 = 6;
        int ON_DESTROY              = 7;
        int ON_SAVE_INSTANCE_STATE  = 8;
    }

    private static final String CATEGORY_STRING = "FragmentLiveCycle";

    private static final String ACTION_STRING_s[] = {
        "onCreate", "onCreateView", "onViewCreated", "onStart,",
        "onResume", "onPause", "onStop", "onDestroy", "onSaveInstanceState"
    };

    private Tracker mTracker;

    /**--------------------------------- Fragment Life Cycle ------------------------------------**/
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sendGAEvent(ACTION_STRING_s[GAFragmentLifeCycle.ON_CREATE], 1);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sendGAEvent(ACTION_STRING_s[GAFragmentLifeCycle.ON_CREATE_VIEW], 1);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sendGAEvent(ACTION_STRING_s[GAFragmentLifeCycle.ON_VIEW_CREATED], 1);
    }

    @Override
    public void onStart() {
        super.onStart();
        sendGAEvent(ACTION_STRING_s[GAFragmentLifeCycle.ON_START], 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        sendGAEvent(ACTION_STRING_s[GAFragmentLifeCycle.ON_RESUME], 1);
    }

    @Override
    public void onPause() {
        super.onPause();
        sendGAEvent(ACTION_STRING_s[GAFragmentLifeCycle.ON_PAUSE], 1);
    }

    @Override
    public void onStop() {
        super.onStop();
        sendGAEvent(ACTION_STRING_s[GAFragmentLifeCycle.ON_STOP], 1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendGAEvent(ACTION_STRING_s[GAFragmentLifeCycle.ON_DESTROY], 1);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        sendGAEvent(ACTION_STRING_s[GAFragmentLifeCycle.ON_SAVE_INSTANCE_STATE], 1);
    }

    private void sendGAEvent(String action, long value) {
        if (mTracker == null) {
            mTracker = ((VLApplication) getActivity().getApplication()).getDefaultTracker();
        }

        mTracker.send(new HitBuilders.EventBuilder()
            .setCategory(CATEGORY_STRING)
            .setAction(action)
            .setLabel(getNameAsGaLabel())
            .setValue(value)
            .build());
    }

    /**
     * The abstract function should be implement to specified which Fragment is being running their
     * life cycle.
     * @return The String return will be used as Label for Google Analysis used
     * */
    abstract String getNameAsGaLabel();
}
