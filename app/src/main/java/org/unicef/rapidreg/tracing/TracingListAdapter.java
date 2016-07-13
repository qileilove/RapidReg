package org.unicef.rapidreg.tracing;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.unicef.rapidreg.R;
import org.unicef.rapidreg.base.RecordListAdapter;
import org.unicef.rapidreg.childcase.CaseFeature;
import org.unicef.rapidreg.model.CasePhoto;
import org.unicef.rapidreg.model.RecordModel;
import org.unicef.rapidreg.service.CasePhotoService;
import org.unicef.rapidreg.service.TracingService;
import org.unicef.rapidreg.service.cache.CaseFieldValueCache;
import org.unicef.rapidreg.service.cache.SubformCache;
import org.unicef.rapidreg.utils.StreamUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class TracingListAdapter extends RecordListAdapter {

    public TracingListAdapter(Context activity) {
        super(activity);
    }

    @Override
    public void onBindViewHolder(RecordListAdapter.RecordListHolder holder, int position) {
        final RecordModel record = recordList.get(position);

        final String caseJson = new String(record.getContent().getBlob());
        final String subformJson = new String(record.getSubform().getBlob());
        final Type caseType = new TypeToken<Map<String, String>>() {
        }.getType();

        final Map<String, String> caseInfo = new Gson().fromJson(caseJson, caseType);
        caseInfo.put(TracingService.TRACING_ID, record.getUniqueId());

        final Type subformType = new TypeToken<Map<String, List<Map<String, String>>>>() {
        }.getType();

        final Map<String, List<Map<String, String>>> subformInfo
                = new Gson().fromJson(subformJson, subformType);

        Gender gender;

        if (caseInfo.get("Sex") != null) {
            gender = Gender.valueOf(caseInfo.get("Sex").toUpperCase());
        } else {
            gender = Gender.UNKNOWN;
        }
        try {
            CasePhoto caseAvatarPhoto = CasePhotoService.getInstance().getCaseFirstThumbnail(record.getId());
            Glide.with(holder.caseImage.getContext()).
                    load((caseAvatarPhoto.getThumbnail().getBlob())).into(holder.caseImage);
        } catch (Exception e) {
            holder.caseImage.setImageDrawable(activity.getResources().getDrawable(gender.getAvatarId()));
        }

        final String shortUUID = getShortUUID(record.getUniqueId());

        holder.idNormalState.setText(shortUUID);
        holder.idHiddenState.setText(shortUUID);
        holder.genderBadge.setImageDrawable(getDefaultGenderBadge(gender.getGenderId()));
        holder.genderName.setText(gender.getName());
        holder.genderName.setTextColor(ContextCompat.getColor(activity, gender.getColorId()));
        String age = caseInfo.get("age");
        holder.age.setText(isValidAge(age) ? age : "");
        holder.registrationDate.setText(dateFormat.format(record.getRegistrationDate()));

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TracingService.getInstance().clearCaseCache();

                setProfileForMiniForm(record, caseInfo, shortUUID);
                CaseFieldValueCache.setValues(caseInfo);
                SubformCache.setValues(subformInfo);

                CasePhotoService.getInstance().setCaseId(record.getId());

                activity.turnToDetailOrEditPage(CaseFeature.DETAILS, record.getId());

                try {
                    CaseFieldValueCache.clearAudioFile();
                    if (record.getAudio() != null) {
                        StreamUtil.writeFile(record.getAudio().getBlob(), CaseFieldValueCache.AUDIO_FILE_PATH);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        toggleTextArea(holder);
    }

    private void setProfileForMiniForm(RecordModel caseItem, Map<String, String> caseInfo, String shortUUID) {
        CaseFieldValueCache.addProfileItem(CaseFieldValueCache.CaseProfile.ID_NORMAL_STATE, shortUUID);
        CaseFieldValueCache.addProfileItem(CaseFieldValueCache.CaseProfile.SEX, caseInfo.get("Sex"));
        CaseFieldValueCache.addProfileItem(CaseFieldValueCache.CaseProfile.REGISTRATION_DATE,
                dateFormat.format(caseItem.getRegistrationDate()));
        CaseFieldValueCache.addProfileItem(CaseFieldValueCache.CaseProfile.GENDER_NAME, shortUUID);
        CaseFieldValueCache.addProfileItem(CaseFieldValueCache.CaseProfile.AGE, caseInfo.get("age"));
        CaseFieldValueCache.addProfileItem(CaseFieldValueCache.CaseProfile.ID, String.valueOf(caseItem.getId()));
    }

    public enum Gender {
        MALE("Male", R.drawable.avatar_placeholder, R.drawable.boy, R.color.boy_blue),
        FEMALE("Female", R.drawable.avatar_placeholder, R.drawable.girl, R.color.girl_red),
        UNKNOWN(null, R.drawable.avatar_placeholder, R.drawable.gender_default, R.color.transparent);

        private String name;
        private int avatarId;
        private int genderId;
        private int colorId;

        Gender(String name, int avatarId, int genderId, int colorId) {
            this.name = name;
            this.avatarId = avatarId;
            this.genderId = genderId;
            this.colorId = colorId;
        }

        public String getName() {
            return name;
        }

        public int getAvatarId() {
            return avatarId;
        }

        public int getGenderId() {
            return genderId;
        }

        public int getColorId() {
            return colorId;
        }
    }
}
