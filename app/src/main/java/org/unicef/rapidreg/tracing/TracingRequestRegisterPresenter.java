package org.unicef.rapidreg.tracing;

import android.content.Context;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;

import org.unicef.rapidreg.forms.Field;
import org.unicef.rapidreg.forms.TracingFormRoot;
import org.unicef.rapidreg.service.TracingFormService;

import java.util.List;

public class TracingRequestRegisterPresenter extends MvpBasePresenter<TracingRequestRegisterView> {

    public void initContext(Context context, int position) {
        if (isViewAttached()) {
            TracingFormRoot form = TracingFormService.getInstance().getCurrentForm();
            if (form != null) {
                List<Field> fields = form.getSections().get(position).getFields();
                getView().initView(new TracingRequestRegisterAdapter(context, fields, false));
            }
        }
    }
}
