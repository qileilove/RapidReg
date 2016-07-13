package org.unicef.rapidreg.childcase;

import android.support.annotation.NonNull;

import org.unicef.rapidreg.base.RecordRegisterFragment;
import org.unicef.rapidreg.base.RecordRegisterPresenter;
import org.unicef.rapidreg.model.RecordModel;

public class CaseRegisterFragment extends RecordRegisterFragment {
    @NonNull
    @Override
    public RecordRegisterPresenter createPresenter() {
        return new RecordRegisterPresenter(RecordModel.CASE);
    }
}
