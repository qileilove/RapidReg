package org.unicef.rapidreg.base.view;

import com.hannesdorfmann.mosby.mvp.MvpView;

import org.unicef.rapidreg.childcase.RecordRegisterAdapter;

public interface RecordRegisterView extends MvpView {

    void initView(RecordRegisterAdapter adapter);
}
