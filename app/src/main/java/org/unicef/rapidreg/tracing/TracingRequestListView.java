package org.unicef.rapidreg.tracing;

import com.hannesdorfmann.mosby.mvp.MvpView;

public interface TracingRequestListView extends MvpView {
    void initView(TracingRequestListAdapter adapter);
}
