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
import org.unicef.rapidreg.forms.childcase.CaseField;
import org.unicef.rapidreg.service.cache.ItemValues;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MiniFormProfileViewHolder extends BaseViewHolder<CaseField> {

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

    public MiniFormProfileViewHolder(Context context, View itemView, ItemValues itemValues) {
        super(context, itemView, itemValues);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void setValue(CaseField field) {
        idView.setText(itemValues.getAsString(ItemValues.CaseProfile.ID_NORMAL_STATE));
        CaseListAdapter.Gender gender;
        if (itemValues.getAsString("Sex") != null) {
            gender = CaseListAdapter.Gender.valueOf(itemValues.getAsString("Sex").toUpperCase());
        } else {
            gender = CaseListAdapter.Gender.UNKNOWN;
        }
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), gender.getGenderId(), null);
        genderBadge.setImageDrawable(drawable);
        genderName.setText(gender.getName());
        genderName.setTextColor(ContextCompat.getColor(context, gender.getColorId()));
        age.setText(itemValues.getAsString("age"));
        registrationDate.setText(itemValues.getAsString(ItemValues.CaseProfile.REGISTRATION_DATE));
    }

    @Override
    public void setOnClickListener(final CaseField field) {

    }

    @Override
    public void setFieldEditable(boolean editable) {

    }
}
