package org.unicef.rapidreg.db;

import com.raizlabs.android.dbflow.sql.language.ConditionGroup;

import org.unicef.rapidreg.model.RecordModel;

import java.util.List;

public interface RecordDao {
    RecordModel getCaseByUniqueId(String id);

    List<RecordModel> getAllCasesOrderByDate(boolean isASC);

    List<RecordModel> getAllCasesOrderByAge(boolean isASC);

    List<RecordModel> getAllCasesByConditionGroup(ConditionGroup conditionGroup);

    RecordModel getTracingByUniqueId(String id);

    List<RecordModel> getAllTracingsOrderByDate(boolean isASC);

    List<RecordModel> getAllTracingsByConditionGroup(ConditionGroup conditionGroup);
}
