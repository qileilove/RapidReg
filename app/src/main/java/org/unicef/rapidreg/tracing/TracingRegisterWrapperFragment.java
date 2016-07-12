package org.unicef.rapidreg.tracing;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicef.rapidreg.R;
import org.unicef.rapidreg.base.view.RecordRegisterWrapperFragment;
import org.unicef.rapidreg.childcase.CaseRegisterAdapter;
import org.unicef.rapidreg.event.SaveCaseEvent;
import org.unicef.rapidreg.service.RecordService;
import org.unicef.rapidreg.service.TracingFormService;
import org.unicef.rapidreg.service.cache.CaseFieldValueCache;
import org.unicef.rapidreg.service.cache.SubformCache;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

public class TracingRegisterWrapperFragment extends RecordRegisterWrapperFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        if (getArguments() != null) {
            recordId = getArguments().getLong(RecordService.TRACING_ID);
        }

        initTracingFormData();

        miniFormAdapter = new CaseRegisterAdapter(getActivity(), miniFields, true);
        miniFormAdapter.setCasePhotoAdapter(initCasePhotoAdapter());

        initFullFormContainer();
        initMiniFormContainer();

        return view;
    }

    @OnClick(R.id.edit_case)
    public void onCaseEditClicked() {
        ((TracingActivity) getActivity()).turnToDetailOrEditPage(TracingFeature.EDIT, recordId);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void saveCase(SaveCaseEvent event) {
        List<String> photoPaths = casePhotoAdapter.getAllItems();
        RecordService.getInstance().saveOrUpdateCase(CaseFieldValueCache.getValues(),
                SubformCache.getValues(),
                photoPaths);
    }

    private void initTracingFormData() {
        form = TracingFormService.getInstance().getCurrentForm();
        sections = form.getSections();
        miniFields = new ArrayList<>();
        if (form != null) {
            getMiniFields();
        }
    }
}
