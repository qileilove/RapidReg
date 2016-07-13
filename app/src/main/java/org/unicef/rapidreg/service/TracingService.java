package org.unicef.rapidreg.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.language.NameAlias;

import org.unicef.rapidreg.childcase.config.CasePhotoConfig;
import org.unicef.rapidreg.db.CasePhotoDao;
import org.unicef.rapidreg.db.TracingDao;
import org.unicef.rapidreg.db.impl.CasePhotoDaoImpl;
import org.unicef.rapidreg.db.impl.TracingDaoImpl;
import org.unicef.rapidreg.model.CasePhoto;
import org.unicef.rapidreg.model.RecordModel;
import org.unicef.rapidreg.model.Tracing;
import org.unicef.rapidreg.service.cache.CaseFieldValueCache;
import org.unicef.rapidreg.service.cache.SubformCache;
import org.unicef.rapidreg.utils.ImageCompressUtil;
import org.unicef.rapidreg.utils.StreamUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TracingService extends RecordService {
    public static final String TAG = CaseService.class.getSimpleName();
    public static final String TRACING_ID = "Tracing ID";

    private static final TracingService TRACING_SERVICE = new TracingService();

    private TracingDao tracingDao = new TracingDaoImpl();
    private CasePhotoDao casePhotoDao = new CasePhotoDaoImpl();

    public static TracingService getInstance() {
        return TRACING_SERVICE;
    }

    private TracingService() {
    }

    public TracingService(TracingDao tracingDao) {
        this.tracingDao = tracingDao;
    }

    public List<Tracing> getTracingList() {
        return tracingDao.getAllTracingsOrderByDate(false);
    }

    public List<Tracing> getTracingListOrderByDateASC() {
        return tracingDao.getAllTracingsOrderByDate(true);
    }

    public List<Tracing> getTracingListOrderByDateDES() {
        return tracingDao.getAllTracingsOrderByDate(false);
    }

    public Map<String, String> getCaseMapByUniqueId(String uniqueId) {
        Tracing tracing = tracingDao.getTracingByUniqueId(uniqueId);
        if (tracing == null) {
            return new HashMap<>();
        }
        String caseJson = new String(tracing.getContent().getBlob());
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> values = new Gson().fromJson(caseJson, type);

        values.put(TRACING_ID, uniqueId);

        return values;
    }

    public List<Tracing> getCasesSearchResult(String uniqueId, String name, int ageFrom, int ageTo,
                                              String caregiver, Date date) {
        ConditionGroup searchCondition = getSearchCondition(uniqueId, name, ageFrom, ageTo, caregiver, date);
        return tracingDao.getAllTracingsByConditionGroup(searchCondition);
    }

    public List<Tracing> getTracingsSearchResult(String uniqueId, String name, int ageFrom, int ageTo,
                                                 String caregiver, Date date) {
        ConditionGroup searchCondition = getSearchCondition(uniqueId, name, ageFrom, ageTo, caregiver, date);
        return tracingDao.getAllTracingsByConditionGroup(searchCondition);
    }

    private ConditionGroup getSearchCondition(String uniqueId, String name, int ageFrom, int ageTo,
                                              String caregiver, Date date) {
        ConditionGroup conditionGroup = ConditionGroup.clause();
        conditionGroup.and(Condition.column(NameAlias.builder(RecordModel.COLUMN_UNIQUE_ID).build())
                .like(getWrappedCondition(uniqueId)));
        conditionGroup.and(Condition.column(NameAlias.builder(RecordModel.COLUMN_NAME).build())
                .like(getWrappedCondition(name)));
        conditionGroup.and(Condition.column(NameAlias.builder(RecordModel.COLUMN_AGE).build())
                .between(ageFrom).and(ageTo));
        conditionGroup.and(Condition.column(NameAlias.builder(RecordModel.COLUMN_CAREGIVER).build())
                .like(getWrappedCondition(caregiver)));

        if (date != null) {
            conditionGroup.and(Condition.column(NameAlias.builder(RecordModel.COLUMN_REGISTRATION_DATE)
                    .build()).eq(date));
        }

        return conditionGroup;
    }

    public void saveOrUpdateTracing(Map<String, String> values,
                                    Map<String, List<Map<String, String>>> subformValues,
                                    List<String> photoPaths) {

        attachSubForms(values, subformValues);

        if (values.get(TRACING_ID) == null) {
            saveTracing(values, subformValues, photoPaths);
        } else {
            Log.d(TAG, "update the existing case");
            updateTracing(values, subformValues, photoPaths);
        }
    }

    public void saveTracing(Map<String, String> values,
                            Map<String, List<Map<String, String>>> subFormValues,
                            List<String> photoPath) {

        String username = UserService.getInstance().getCurrentUser().getUsername();
        values.put(MODULE, "primeromodule-cp");
        values.put(CASEWORKER_CODE, username);
        values.put(RECORD_CREATED_BY, username);
        values.put(PREVIOUS_OWNER, username);

        Gson gson = new Gson();
        Date date = new Date(Calendar.getInstance().getTimeInMillis());
        Blob caseBlob = new Blob(gson.toJson(values).getBytes());
        Blob subFormBlob = new Blob(gson.toJson(subFormValues).getBytes());
        Blob audioFileDefault = null;
        audioFileDefault = getAudioBlob(audioFileDefault);

        Tracing tracing = new Tracing();
        tracing.setUniqueId(createUniqueId());
        tracing.setCreateDate(date);
        tracing.setLastUpdatedDate(date);
        tracing.setContent(caseBlob);
        tracing.setName(getName(values));
        int age = values.get(AGE) != null ? Integer.parseInt(values.get(AGE)) : 0;
        tracing.setAge(age);
        tracing.setCaregiver(getCaregiverName(values));
        tracing.setRegistrationDate(getRegisterDate(values));
        tracing.setAudio(audioFileDefault);
        tracing.setSubform(subFormBlob);
        tracing.setCreatedBy(username);
        tracing.save();

        try {
            saveTracingPhoto(tracing, photoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        CaseFieldValueCache.clearAudioFile();
    }

    public void saveTracingPhoto(RecordModel child, List<String> photoPaths) throws IOException {
        for (int i = 0; i < photoPaths.size(); i++) {
            generateSaveTracingPhoto(child, photoPaths, i).save();
        }
    }

    public void updateTracing(Map<String, String> values,
                              Map<String, List<Map<String, String>>> subFormValues,
                              List<String> photoBitPaths) {
        Gson gson = new Gson();
        Blob caseBlob = new Blob(gson.toJson(values).getBytes());
        Blob subFormBlob = new Blob(gson.toJson(subFormValues).getBytes());
        Blob audioFileDefault = null;
        audioFileDefault = getAudioBlob(audioFileDefault);

        Tracing tracing = tracingDao.getTracingByUniqueId(values.get(TRACING_ID));
        tracing.setLastUpdatedDate(new Date(Calendar.getInstance().getTimeInMillis()));
        tracing.setContent(caseBlob);
        tracing.setName(getName(values));
        int age = values.get(AGE) != null ? Integer.parseInt(values.get(AGE)) : 0;
        tracing.setAge(age);
        tracing.setCaregiver(getCaregiverName(values));
        tracing.setRegistrationDate(getRegisterDate(values));
        tracing.setAudio(audioFileDefault);
        tracing.setSubform(subFormBlob);
        tracing.update();
        try {
            updateTracingPhoto(tracing, photoBitPaths);
        } catch (IOException e) {
            e.printStackTrace();
        }
        CaseFieldValueCache.clearAudioFile();
    }

    public void updateTracingPhoto(Tracing child, List<String> photoPaths) throws IOException {
        int previousCount = casePhotoDao.getAllCasesPhotoFlowQueryList(child.getId()).size();

        if (previousCount < photoPaths.size()) {
            for (int i = 0; i < previousCount; i++) {
                CasePhoto tracingPhoto = generateUpdateTracingPhoto(child, photoPaths, i);
                tracingPhoto.update();
            }
            for (int i = previousCount; i < photoPaths.size(); i++) {
                CasePhoto casePhoto = generateSaveTracingPhoto(child, photoPaths, i);
                if (casePhoto.getId() == 0) {
                    casePhoto.save();
                } else {
                    casePhoto.update();
                }
            }
        } else {
            for (int i = 0; i < photoPaths.size(); i++) {
                CasePhoto tracingPhoto = generateUpdateTracingPhoto(child, photoPaths, i);
                tracingPhoto.update();
            }
            for (int i = photoPaths.size(); i < previousCount; i++) {
                CasePhoto casePhoto = casePhotoDao.getSpecialOrderCasePhotoByCaseId(child.getId(), i + 1);
                casePhoto.setPhoto(null);
                casePhoto.setThumbnail(null);
                casePhoto.update();
            }
        }
    }

    private CasePhoto generateSaveTracingPhoto(RecordModel child, List<String> photoPaths, int index) throws IOException {
        CasePhoto casePhoto = casePhotoDao.getSpecialOrderCasePhotoByCaseId(child.getId(), index + 1);
        if (casePhoto == null) {
            casePhoto = new CasePhoto();
        }
        String filePath = photoPaths.get(index);
        Bitmap bitmap = preProcessImage(filePath);
        casePhoto.setThumbnail(new Blob(ImageCompressUtil.convertImageToBytes(
                ImageCompressUtil.getThumbnail(bitmap, CasePhotoConfig.THUMBNAIL_SIZE,
                        CasePhotoConfig.THUMBNAIL_SIZE))));

        casePhoto.setPhoto(new Blob(ImageCompressUtil.convertImageToBytes(bitmap)));
//        casePhoto.setCase(child);
        casePhoto.setOrder(index + 1);
        return casePhoto;
    }

    @NonNull
    private CasePhoto generateUpdateTracingPhoto(RecordModel child, List<String> photoPaths, int index)
            throws IOException {
        CasePhoto casePhoto;
        String filePath = photoPaths.get(index);
        Blob photo;
        try {
            long photoId = Long.parseLong(filePath);
            casePhoto = casePhotoDao.getCasePhotoById(photoId);
        } catch (NumberFormatException e) {
            Bitmap bitmap = preProcessImage(filePath);
            photo = new Blob(ImageCompressUtil.convertImageToBytes(bitmap));
            casePhoto = new CasePhoto();
            casePhoto.setThumbnail(new Blob(ImageCompressUtil.convertImageToBytes(
                    ImageCompressUtil.getThumbnail(bitmap, CasePhotoConfig.THUMBNAIL_SIZE,
                            CasePhotoConfig.THUMBNAIL_SIZE))));
//            casePhoto.setCase(child);
            casePhoto.setPhoto(photo);
        }
        casePhoto.setId(casePhotoDao.getSpecialOrderCasePhotoByCaseId(child.getId(), index + 1).getId());
        casePhoto.setOrder(index + 1);
        return casePhoto;
    }

    private Bitmap preProcessImage(String filePath) throws IOException {
        if (new File(filePath).length() <= 1024 * 1024 * 1) {
            return BitmapFactory.decodeFile(filePath);
        }
        return ImageCompressUtil.compressImage(filePath,
                CasePhotoConfig.MAX_WIDTH, CasePhotoConfig.MAX_HEIGHT);
    }

    public void clearCaseCache() {
        CaseFieldValueCache.clear();
        SubformCache.clear();
    }

    public String createUniqueId() {
        return UUID.randomUUID().toString();
    }

    private Blob getAudioBlob(Blob blob) {
        try {
            blob = new Blob(StreamUtil.readFile(CaseFieldValueCache.AUDIO_FILE_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return blob;
    }

    private Date getCurrentDate() {
        return new Date(Calendar.getInstance().getTimeInMillis());
    }

    private Date getRegisterDate(Map<String, String> values) {
        if (values.containsKey(REGISTRATION_DATE)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
            try {
                java.util.Date date = simpleDateFormat.parse(values.get(REGISTRATION_DATE));
                return new Date(date.getTime());
            } catch (ParseException e) {
                Log.e(TAG, "date format error");
            }
        }
        return getCurrentDate();
    }

    private String getName(Map<String, String> values) {
        return values.get(FULL_NAME) + " "
                + values.get(FIRST_NAME) + " "
                + values.get(MIDDLE_NAME) + " "
                + values.get(SURNAME) + " "
                + values.get(NICKNAME) + " "
                + values.get(OTHER_NAME);
    }

    private String getCaregiverName(Map<String, String> values) {
        return "" + values.get(CAREGIVER_NAME);
    }

    private String getWrappedCondition(String queryStr) {
        return "%" + queryStr + "%";
    }

    private void attachSubForms(Map<String, String> values, Map<String, List<Map<String, String>>> subFormValues) {
        Gson gson = new Gson();
        for (String key : subFormValues.keySet()) {
            values.put(key, gson.toJson(subFormValues.get(key)));
        }
    }
}
