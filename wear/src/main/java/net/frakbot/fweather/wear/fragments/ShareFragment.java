/*
 * Copyright 2014 Sebastiano Poggi and Francesco Pontillo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.frakbot.fweather.wear.fragments;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.activity.ConfirmationActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import net.frakbot.fweather.wear.R;

public class ShareFragment extends Fragment implements View.OnClickListener {

    private View rootView;
    private OnShareClickListener shareClickListener;

    public interface OnShareClickListener{
        void onShareSelected();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        shareClickListener = (OnShareClickListener) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_share, null);

        ImageButton iconView = (ImageButton) rootView.findViewById(R.id.action_icon);
        iconView.setOnClickListener(this);
        TextView textView = (TextView) rootView.findViewById(R.id.action_label);
        textView.setText("Share");
        return rootView;
    }

    @Override
    public void onClick(View v) {
        shareClickListener.onShareSelected();
        showConfirmation();
    }

    private void showConfirmation() {
        Activity activity = getActivity();
        Intent intent = new Intent(activity, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
        Bundle options = ActivityOptions.makeCustomAnimation(activity, 0, 0).toBundle();
        activity.startActivity(intent, options);
    }

}