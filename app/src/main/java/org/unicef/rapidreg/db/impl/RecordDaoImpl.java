package org.unicef.rapidreg.db.impl;

import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.unicef.rapidreg.db.RecordDao;
import org.unicef.rapidreg.model.RecordModel;
import org.unicef.rapidreg.model.RecordModel_Table;

import java.util.List;

public class RecordDaoImpl implements RecordDao {
    @Override
    public RecordModel getCaseByUniqueId(String uniqueId) {
        return SQLite.select().from(RecordModel.class)
                .where(RecordModel_Table.unique_id.eq(uniqueId))
                .and(RecordModel_Table.type.eq(RecordModel.CASE))
                .querySingle();
    }

    @Override
    public List<RecordModel> getAllCasesOrderByDate(boolean isASC) {
        return isASC ? getCasesByDateASC() : getCasesByDateDES();
    }

    @Override
    public List<RecordModel> getAllCasesOrderByAge(boolean isASC) {
        return isASC ? getCasesByAgeASC() : getCasesByAgeDES();

    }

    @Override
    public List<RecordModel> getAllCasesByConditionGroup(ConditionGroup conditionGroup) {
        return SQLite.select().from(RecordModel.class)
                .where(RecordModel_Table.type.eq(RecordModel.CASE))
                .and(conditionGroup)
                .orderBy(RecordModel_Table.registration_date, false)
                .queryList();
    }

    @Override
    public RecordModel getTracingByUniqueId(String uniqueId) {
        return SQLite.select().from(RecordModel.class)
                .where(RecordModel_Table.unique_id.eq(uniqueId))
                .and(RecordModel_Table.type.eq(RecordModel.TRACING))
                .querySingle();
    }

    @Override
    public List<RecordModel> getAllTracingsOrderByDate(boolean isASC) {
        return isASC ? getTracingsByDateASC() : getTracingsByDateDES();
    }

    @Override
    public List<RecordModel> getAllTracingsByConditionGroup(ConditionGroup conditionGroup) {
        return SQLite.select().from(RecordModel.class)
                .where(RecordModel_Table.type.eq(RecordModel.TRACING))
                .and(conditionGroup)
                .orderBy(RecordModel_Table.registration_date, false)
                .queryList();
    }

    private List<RecordModel> getCasesByAgeASC() {
        return SQLite.select().from(RecordModel.class).orderBy(RecordModel_Table.age, true).queryList();
    }

    private List<RecordModel> getCasesByAgeDES() {
        return SQLite.select().from(RecordModel.class).orderBy(RecordModel_Table.age, false).queryList();
    }

    private List<RecordModel> getCasesByDateASC() {
        return SQLite.select().from(RecordModel.class)
                .where(RecordModel_Table.type.eq(RecordModel.CASE))
                .orderBy(RecordModel_Table.registration_date, true).queryList();
    }

    private List<RecordModel> getCasesByDateDES() {
        return SQLite.select().from(RecordModel.class)
                .where(RecordModel_Table.type.eq(RecordModel.CASE))
                .orderBy(RecordModel_Table.registration_date, false).queryList();
    }

    private List<RecordModel> getTracingsByDateASC() {
        return SQLite.select().from(RecordModel.class)
                .where(RecordModel_Table.type.eq(RecordModel.TRACING))
                .orderBy(RecordModel_Table.registration_date, true).queryList();
    }

    private List<RecordModel> getTracingsByDateDES() {
        return SQLite.select().from(RecordModel.class)
                .where(RecordModel_Table.type.eq(RecordModel.TRACING))
                .orderBy(RecordModel_Table.registration_date, false).queryList();
    }
}
