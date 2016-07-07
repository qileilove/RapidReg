package org.unicef.rapidreg.tracing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.unicef.rapidreg.R;
import org.unicef.rapidreg.model.CasePhoto;
import org.unicef.rapidreg.model.Tracing;
import org.unicef.rapidreg.service.CasePhotoService;
import org.unicef.rapidreg.service.CaseService;
import org.unicef.rapidreg.service.cache.CasePhotoCache;
import org.unicef.rapidreg.service.cache.FieldValueCache;
import org.unicef.rapidreg.service.cache.SubformCache;
import org.unicef.rapidreg.utils.StreamUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class TracingRequestListAdapter extends RecyclerView.Adapter<TracingRequestListAdapter.CaseListHolder> {
    public static final String TAG = TracingRequestListAdapter.class.getSimpleName();
    private static final int TEXT_AREA_SHOWED_STATE = 0;
    private static final int TEXT_AREA_HIDDEN_STATE = 1;

    private List<Tracing> tracingList = new ArrayList<>();
    private TracingRequestActivity activity;
    private DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US);
    private boolean isDetailShow = true;

    public TracingRequestListAdapter(Context activity) {
        this.activity = (TracingRequestActivity) activity;
    }

    public void setTracingList(List<Tracing> tracingList) {
        this.tracingList = tracingList;
    }

    @Override
    public CaseListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup itemView = (ViewGroup) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.case_list, parent, false);

        return new CaseListHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CaseListHolder holder, int position) {
        final Tracing caseItem = tracingList.get(position);

        final String caseJson = new String(caseItem.getContent().getBlob());
        final String subformJson = new String(caseItem.getSubform().getBlob());
        final Type caseType = new TypeToken<Map<String, String>>() {
        }.getType();

        final Map<String, String> caseInfo = new Gson().fromJson(caseJson, caseType);
        caseInfo.put(CaseService.CASE_ID, caseItem.getUniqueId());

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
            CasePhoto caseAvatorPhoto = CasePhotoService.getInstance().getAllCasePhotos(caseItem.getId()).get(0);
            Bitmap thumbnail = CasePhotoCache.syncAvatarPhotoBitmap(caseAvatorPhoto);
            holder.caseImage.setImageBitmap(thumbnail);
        } catch (Exception e) {
            holder.caseImage.setImageDrawable(activity.getResources().getDrawable(gender.getAvatarId()));
        }
        final String shortUUID = getShortUUID(caseItem.getUniqueId());

        holder.idNormalState.setText(shortUUID);
        holder.idHiddenState.setText(shortUUID);
        holder.genderBadge.setImageDrawable(getDefaultGenderBadge(gender.getGenderId()));
        holder.genderName.setText(gender.getName());
        holder.genderName.setTextColor(ContextCompat.getColor(activity, gender.getColorId()));
        String age = caseInfo.get("age");
        holder.age.setText(isValidAge(age) ? age : "");
        holder.registrationDate.setText(dateFormat.format(caseItem.getRegistrationDate()));

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CaseService.getInstance().clearCaseCache();

                setProfileForMiniForm(caseItem, caseInfo, shortUUID);
                FieldValueCache.setValues(caseInfo);
                SubformCache.setValues(subformInfo);
                List<CasePhoto> casePhotos = CasePhotoService.getInstance().getAllCasePhotos(caseItem.getId());

                CasePhotoCache.syncPhotosPaths(casePhotos);

                activity.turnToFeature(TracingRequestFeature.DETAILS);

                try {
                    FieldValueCache.clearAudioFile();
                    if (caseItem.getAudio() != null) {
                        StreamUtil.writeFile(caseItem.getAudio().getBlob(), FieldValueCache.AUDIO_FILE_PATH);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        toggleTextArea(holder);
    }

    private void setProfileForMiniForm(Tracing caseItem, Map<String, String> caseInfo, String shortUUID) {
        FieldValueCache.addProfileItem(FieldValueCache.CaseProfile.ID_NORMAL_STATE, shortUUID);
        FieldValueCache.addProfileItem(FieldValueCache.CaseProfile.SEX, caseInfo.get("Sex"));
        FieldValueCache.addProfileItem(FieldValueCache.CaseProfile.REGISTRATION_DATE,
                dateFormat.format(caseItem.getRegistrationDate()));
        FieldValueCache.addProfileItem(FieldValueCache.CaseProfile.GENDER_NAME, shortUUID);
        FieldValueCache.addProfileItem(FieldValueCache.CaseProfile.AGE, caseInfo.get("age"));
        FieldValueCache.addProfileItem(FieldValueCache.CaseProfile.ID, String.valueOf(caseItem.getId()));
    }

    @Override
    public int getItemCount() {
        return tracingList.size();
    }

    public void toggleViews(boolean isDetailShow) {
        this.isDetailShow = isDetailShow;
        this.notifyDataSetChanged();
    }

    private void toggleTextArea(CaseListHolder holder) {
        if (isDetailShow) {
            holder.viewSwitcher.setDisplayedChild(TEXT_AREA_SHOWED_STATE);
        } else {
            holder.viewSwitcher.setDisplayedChild(TEXT_AREA_HIDDEN_STATE);
        }
    }

    private Drawable getDefaultGenderBadge(int genderId) {
        return ResourcesCompat.getDrawable(activity.getResources(), genderId, null);
    }

    private String getShortUUID(String uuid) {
        int length = uuid.length();
        return length > 7 ? uuid.substring(length - 7) : uuid;
    }

    private boolean isValidAge(String value) {
        if (value == null) {
            return false;
        }

        return Integer.valueOf(value) > 0;
    }

    public static class CaseListHolder extends RecyclerView.ViewHolder {
        protected TextView idNormalState;
        protected TextView idHiddenState;
        protected ImageView genderBadge;
        protected TextView genderName;
        protected TextView age;
        protected TextView registrationDate;
        protected ImageView caseImage;
        protected View view;
        protected ViewSwitcher viewSwitcher;

        public CaseListHolder(View itemView) {
            super(itemView);
            view = itemView;
            idNormalState = (TextView) itemView.findViewById(R.id.id_normal_state);
            idHiddenState = (TextView) itemView.findViewById(R.id.id_on_hidden_state);
            genderBadge = (ImageView) itemView.findViewById(R.id.gender_badge);
            genderName = (TextView) itemView.findViewById(R.id.gender_name);
            age = (TextView) itemView.findViewById(R.id.age);
            registrationDate = (TextView) itemView.findViewById(R.id.registration_date);
            caseImage = (CircleImageView) itemView.findViewById(R.id.case_image);

            viewSwitcher = (ViewSwitcher) itemView.findViewById(R.id.view_switcher);
        }
    }

    public enum Gender {
        MALE("BOY", R.drawable.avatar_placeholder, R.drawable.boy, R.color.boy_blue),
        FEMALE("GIRL", R.drawable.avatar_placeholder, R.drawable.girl, R.color.girl_red),
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
