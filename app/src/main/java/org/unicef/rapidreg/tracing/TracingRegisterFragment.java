package org.unicef.rapidreg.tracing;

import android.support.annotation.NonNull;

import org.unicef.rapidreg.base.RecordRegisterFragment;
import org.unicef.rapidreg.base.RecordRegisterPresenter;
import org.unicef.rapidreg.model.RecordModel;

public class TracingRegisterFragment extends RecordRegisterFragment {
    @NonNull
    @Override
    public RecordRegisterPresenter createPresenter() {
        return new RecordRegisterPresenter(RecordModel.TRACING);
    }
}
