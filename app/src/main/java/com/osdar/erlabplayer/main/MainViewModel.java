package com.osdar.erlabplayer.main;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.osdar.erlabplayer.utils.Const;
import com.osdar.erlabplayer.utils.Event;
import com.osdar.erlabplayer.utils.SingleLiveEvent;

public class MainViewModel extends ViewModel {

    // Create a LiveData with a String
    private SingleLiveEvent<Event<Const>> navigate;

    public SingleLiveEvent<Event<Const>> getNavigate() {
        if (navigate == null) {
            navigate = new SingleLiveEvent<>();
        }
        return navigate;
    }


    public void openHlsVideo(Const type) {
        Log.e("view", "openHlsVideo");
        navigate.postValue(new Event<>(type));
    }

}