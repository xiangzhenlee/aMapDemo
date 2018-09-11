package com.yushan.amapdemo.trackdemo.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.yushan.amapdemo.R;


public final class FeatureView extends FrameLayout {

    public FeatureView(Context context) {
        super(context);

        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.feature, this);
    }

    public synchronized void setTitle(String title) {
        ((TextView) (findViewById(R.id.title))).setText(title);
    }

    public synchronized void setDescription(String description) {
        if (TextUtils.isEmpty(description)) {
            ((TextView) (findViewById(R.id.description))).setText("");
        } else {
            ((TextView) (findViewById(R.id.description))).setText(description);
        }
    }

}
