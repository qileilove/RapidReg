package org.unicef.rapidreg.service;

import org.unicef.rapidreg.forms.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecordService {
    public static final String AGE = "Age";
    public static final String FULL_NAME = "Full Name";
    public static final String FIRST_NAME = "First Name";
    public static final String MIDDLE_NAME = "Middle Name";
    public static final String SURNAME = "Surname";
    public static final String NICKNAME = "Nickname";
    public static final String OTHER_NAME = "Other Name";
    public static final String CAREGIVER_NAME = "Name of Current Caregiver";
    public static final String REGISTRATION_DATE = "Date of Registration or Interview";
    public static final String CASEWORKER_CODE = "Caseworker Code";
    public static final String RECORD_CREATED_BY = "Record created by";
    public static final String PREVIOUS_OWNER = "Previous Owner";
    public static final String MODULE = "Module";

    public List<String> fetchRequiredFiledNames(List<Field> fields) {
        List<String> result = new ArrayList<>();
        for (Field field : fields) {
            if (field.isRequired()) {
                result.add(field.getDisplayName().get(Locale.getDefault().getLanguage()));
            }
        }
        return result;
    }
}
