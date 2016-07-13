package org.unicef.rapidreg.childcase;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicef.rapidreg.R;
import org.unicef.rapidreg.base.RecordRegisterAdapter;
import org.unicef.rapidreg.base.RecordRegisterWrapperFragment;
import org.unicef.rapidreg.event.SaveCaseEvent;
import org.unicef.rapidreg.forms.Section;
import org.unicef.rapidreg.service.CaseFormService;
import org.unicef.rapidreg.service.CaseService;
import org.unicef.rapidreg.service.cache.CaseFieldValueCache;
import org.unicef.rapidreg.service.cache.SubformCache;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

public class CaseRegisterWrapperFragment extends RecordRegisterWrapperFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        if (getArguments() != null) {
            recordId = getArguments().getLong(CaseService.CASE_ID);
        }

        initCaseFormData();

        miniFormAdapter = new RecordRegisterAdapter(getActivity(), miniFields, true);
        miniFormAdapter.setCasePhotoAdapter(initCasePhotoAdapter());

        initFullFormContainer();
        initMiniFormContainer();

        return view;
    }

    @NonNull
    protected FragmentPagerItems getPages() {
        FragmentPagerItems pages = new FragmentPagerItems(getActivity());
        for (Section section : sections) {
            String[] values = section.getName().values().toArray(new String[0]);
            Bundle bundle = new Bundle();

            bundle.putStringArrayList("case_photos",
                    (ArrayList<String>) casePhotoAdapter.getAllItems());

            pages.add(FragmentPagerItem.of(values[0], CaseRegisterFragment.class, bundle));
        }
        return pages;
    }

    @OnClick(R.id.edit_case)
    public void onCaseEditClicked() {
        ((CaseActivity) getActivity()).turnToDetailOrEditPage(CaseFeature.EDIT, recordId);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void saveCase(SaveCaseEvent event) {
        List<String> photoPaths = casePhotoAdapter.getAllItems();
        CaseService.getInstance().saveOrUpdateCase(CaseFieldValueCache.getValues(),
                SubformCache.getValues(),
                photoPaths);
    }

    private void initCaseFormData() {
        form = CaseFormService.getInstance().getCurrentForm();
        sections = form.getSections();
        miniFields = new ArrayList<>();
        if (form != null) {
            getMiniFields();
        }
    }
}
