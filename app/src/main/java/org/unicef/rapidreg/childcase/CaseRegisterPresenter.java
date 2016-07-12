package org.unicef.rapidreg.childcase;

import android.content.Context;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;

import org.unicef.rapidreg.forms.CaseFormRoot;
import org.unicef.rapidreg.forms.Field;
import org.unicef.rapidreg.service.CaseFormService;

import java.util.List;

public class CaseRegisterPresenter extends MvpBasePresenter<CaseRegisterView> {

    public void initContext(Context context, int position) {
        if (isViewAttached()) {
            CaseFormRoot form = CaseFormService.getInstance().getCurrentForm();
            if (form != null) {
                List<Field> fields = form.getSections().get(position).getFields();
                CaseRegisterAdapter caseRegisterAdapter = new CaseRegisterAdapter(context, fields, false);
//                caseRegisterAdapter.setCasePhotoAdapter(new CasePhotoAdapter(context,new ArrayList<String>()));
                getView().initView(caseRegisterAdapter);
            }
        }
    }
}
