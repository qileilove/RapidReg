package org.unicef.rapidreg.childcase;

import android.support.v4.app.Fragment;

import org.unicef.rapidreg.R;
import org.unicef.rapidreg.exception.FragmentSwitchException;

public enum Feature {
    LIST(R.string.cases, ListFragment.class),
    ADD(R.string.new_case, RegisterWrapperFragment.class),
    EDIT(R.string.edit, RegisterWrapperFragment.class),
    DETAILS(R.string.case_details, RegisterWrapperFragment.class),
    SEARCH(R.string.search, SearchFragment.class);

    private int titleId;
    private Class clz;

    Feature(int titleId, Class clz) {
        this.titleId = titleId;
        this.clz = clz;
    }

    public int getTitleId() {
        return titleId;
    }

    public Fragment getFragment() throws FragmentSwitchException {
        try {
            return (Fragment) clz.newInstance();
        } catch (InstantiationException e) {
            throw new FragmentSwitchException("The constructor is not accessible", e);
        } catch (IllegalAccessException e) {
            throw new FragmentSwitchException("The method or field is not accessible", e);
        }
    }

    public boolean isEditMode() {
        return this == ADD || this == EDIT;
    }

    public boolean isListMode() {
        return this == LIST;
    }

    public boolean isDetailMode() {
        return this == DETAILS;
    }

    public boolean isInAddMode() {
        return this == ADD;
    }
}
