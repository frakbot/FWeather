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

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.activity.ConfirmationActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import net.frakbot.fweather.wear.R;

public class ShareFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {

    private static final String ACTION_INTENT = "ACTION_INTENT";
    private static final String ICON_RESOURCE = "ICON_RESOURCE";
    private static final String ACTION_LABEL = "ACTION_LABEL";
    private Intent actionIntent;
    private int iconResource;
    private String actionLabel;
    private View rootView;

    public static Fragment newInstance(Intent actionIntent) {
        Fragment fragment = new ShareFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ACTION_INTENT, actionIntent);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionIntent = getArguments().getParcelable(ACTION_INTENT);
        iconResource = getArguments().getInt(ICON_RESOURCE);
        actionLabel = getArguments().getString(ACTION_LABEL);
        if (actionLabel == null) {
            actionLabel = getString(android.R.string.ok);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_share, null);

        ImageButton iconView = (ImageButton) rootView.findViewById(R.id.action_icon);
        iconView.setImageResource(iconResource);
        iconView.setOnClickListener(this);
        iconView.setOnTouchListener(this);
        TextView textView = (TextView) rootView.findViewById(R.id.action_label);
        textView.setText(actionLabel);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (actionIntent != null) {
            getActivity().startService(actionIntent);
            showConfirmation();
            getActivity().finish();
        }
    }

    private void showConfirmation() {
        Intent intent = new Intent(getActivity(), ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
        Bundle options = ActivityOptions.makeCustomAnimation(getActivity(), 0, 0).toBundle();
        getActivity().startActivity(intent, options);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            rootView.setBackgroundResource(android.R.color.black);
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            rootView.setBackgroundResource(android.R.color.transparent);
        }
        return false;
    }
}