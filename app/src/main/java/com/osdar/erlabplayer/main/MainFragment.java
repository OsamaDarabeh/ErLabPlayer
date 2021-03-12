package com.osdar.erlabplayer.main;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.osdar.erlabplayer.R;
import com.osdar.erlabplayer.databinding.MainFragmentBinding;
import com.osdar.erlabplayer.erLabPlayer.ExoPlayerActivity;

public class MainFragment extends Fragment {

    public static final String VIDEO_TYPE = "videoType";
    private MainViewModel mViewModel;
    private MainFragmentBinding binding;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding =
                DataBindingUtil.inflate(inflater, R.layout.main_fragment, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        binding.setLifecycleOwner(this);
        binding.setViewmodel(mViewModel);
        observers();
    }

    private void observers() {
        mViewModel.getNavigate().observe(getViewLifecycleOwner(), o -> {
            Intent intent = new Intent(getActivity(), ExoPlayerActivity.class);
            intent.putExtra(VIDEO_TYPE, o.getContentIfNotHandled());
            startActivity(intent);
        });
    }


}