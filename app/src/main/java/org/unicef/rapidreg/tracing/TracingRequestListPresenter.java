package org.unicef.rapidreg.tracing;

import android.content.Context;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;

public class TracingRequestListPresenter extends MvpBasePresenter<TracingRequestListView> {
    public void initView(Context context) {
        if (isViewAttached()) {
            getView().initView(new TracingRequestListAdapter(context));
        }
    }
}
