package org.unicef.rapidreg.widgets.viewholder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.unicef.rapidreg.R;
import org.unicef.rapidreg.childcase.CaseListAdapter;
import org.unicef.rapidreg.forms.Field;
import org.unicef.rapidreg.service.cache.FieldValueCache;
import org.unicef.rapidreg.service.cache.FieldValueCache.CaseProfile;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MiniFormProfileViewHolder extends BaseViewHolder<Field> {

    public static final String TAG = MiniFormProfileViewHolder.class.getSimpleName();

    @BindView(R.id.id_normal_state)
    TextView idView;

    @BindView(R.id.gender_badge)
    ImageView genderBadge;

    @BindView(R.id.gender_name)
    TextView genderName;

    @BindView(R.id.age)
    TextView age;

    @BindView(R.id.registration_date)
    TextView registrationDate;

    public MiniFormProfileViewHolder(Context context, View itemView) {
        super(context, itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void setValue(Field field) {
        idView.setText(FieldValueCache.getProfileValue(CaseProfile.ID_NORMAL_STATE));
        CaseListAdapter.Gender gender;
        if (FieldValueCache.getProfileValue(CaseProfile.SEX) != null) {
            gender = CaseListAdapter.Gender.valueOf(FieldValueCache.getProfileValue(CaseProfile.SEX).toUpperCase());
        } else {
            gender = CaseListAdapter.Gender.UNKNOWN;
        }
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), gender.getGenderId(), null);
        genderBadge.setImageDrawable(drawable);
        genderName.setText(gender.getName());
        genderName.setTextColor(ContextCompat.getColor(context, gender.getColorId()));
        age.setText(FieldValueCache.getProfileValue(CaseProfile.AGE));
        registrationDate.setText(FieldValueCache.getProfileValue(CaseProfile.REGISTRATION_DATE));
    }

    @Override
    public void setOnClickListener(final Field field) {

    }

    @Override
    protected String getResult() {
        return null;
    }

    @Override
    public void setFieldEditable(boolean editable) {

    }
}
