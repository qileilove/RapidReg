package org.unicef.rapidreg.tracing;

import android.support.v4.app.Fragment;

import org.unicef.rapidreg.R;

public enum TracingRequestFeature {
    LIST(R.string.tracing_requests, TracingRequestListFragment.class),
    ADD(R.string.new_tracing, TracingRequestRegisterWrapperFragment.class),
    EDIT(R.string.edit, TracingRequestRegisterWrapperFragment.class),
    DETAILS(R.string.tracing_details, TracingRequestRegisterWrapperFragment.class),
    SEARCH(R.string.search, TracingRequestSearchFragment.class);

    private int titleId;
    private Class clz;


    TracingRequestFeature(int titleId, Class clz) {
        this.titleId = titleId;
        this.clz = clz;
    }

    public int getTitleId() {
        return titleId;
    }

    public Fragment getFragment() throws IllegalAccessException, InstantiationException {
        return (Fragment) clz.newInstance();
    }

    public boolean isInEditMode() {
        return this == ADD || this == EDIT;
    }

    public boolean isInListMode() {
        return this == LIST;
    }

    public boolean isInDetailMode() {
        return this == DETAILS;
    }

    public boolean isInAddMode() {
        return this == ADD;
    }
}
