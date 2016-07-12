package org.unicef.rapidreg.base;

import android.content.Context;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;

import org.unicef.rapidreg.forms.Field;
import org.unicef.rapidreg.forms.RecordForm;
import org.unicef.rapidreg.service.CaseFormService;

import java.util.List;

public class RecordRegisterPresenter extends MvpBasePresenter<RecordRegisterView> {

    public void initContext(Context context, int position) {
        if (isViewAttached()) {
            RecordForm form;
            form = CaseFormService.getInstance().getCurrentForm();
            if (form != null) {
                List<Field> fields = form.getSections().get(position).getFields();
                RecordRegisterAdapter adapter = new RecordRegisterAdapter(context, fields, false);
                getView().initView(adapter);
            }
        }
    }
}
