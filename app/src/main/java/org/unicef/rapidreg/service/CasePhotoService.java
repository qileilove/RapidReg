package org.unicef.rapidreg.service;

import org.unicef.rapidreg.db.CasePhotoDao;
import org.unicef.rapidreg.db.impl.CasePhotoDaoImpl;
import org.unicef.rapidreg.model.CasePhoto;

import java.util.List;

public class CasePhotoService {
    public static final String TAG = CasePhotoService.class.getSimpleName();

    private static final CasePhotoService CASE_SERVICE = new CasePhotoService(new CasePhotoDaoImpl());


    private CasePhotoDao casePhotoDao;

    public static CasePhotoService getInstance() {
        return CASE_SERVICE;
    }

    public CasePhotoService(CasePhotoDao caseDao) {
        this.casePhotoDao = caseDao;
    }

    public List<CasePhoto> getAllCasePhotos(long caseId){
        return casePhotoDao.getAllCasesPhoto(caseId);
    }
}
