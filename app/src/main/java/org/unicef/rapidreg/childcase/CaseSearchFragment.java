package org.unicef.rapidreg.childcase;

import android.text.TextUtils;

import org.unicef.rapidreg.base.RecordSearchFragment;
import org.unicef.rapidreg.model.RecordModel;
import org.unicef.rapidreg.service.RecordService;

import java.util.List;
import java.util.Map;

public class CaseSearchFragment extends RecordSearchFragment {

    @Override
    protected List<RecordModel> getSearchResult(Map<String, String> filters) {
        String id = filters.get(ID);
        String name = filters.get(NAME);
        String from = filters.get(AGE_FROM);
        int ageFrom = TextUtils.isEmpty(from) ? RecordModel.MIN_AGE : Integer.valueOf(from);
        String to = filters.get(AGE_TO);
        int ageTo = TextUtils.isEmpty(to) ? RecordModel.MAX_AGE : Integer.valueOf(to);
        String caregiver = filters.get(CAREGIVER);
        String registrationDate = filters.get(REGISTRATION_DATE);

        return RecordService.getInstance().getCasesSearchResult(id, name, ageFrom, ageTo,
                caregiver, getDate(registrationDate));
    }
}
