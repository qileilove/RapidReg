package org.unicef.rapidreg.tracing;

import android.view.View;
import android.widget.AdapterView;

import org.unicef.rapidreg.R;
import org.unicef.rapidreg.base.RecordListAdapter;
import org.unicef.rapidreg.base.RecordListFragment;
import org.unicef.rapidreg.base.RecordListPresenter;
import org.unicef.rapidreg.model.RecordModel;
import org.unicef.rapidreg.service.RecordService;

import java.util.Arrays;

public class TracingListFragment extends RecordListFragment {
    protected static final RecordListFragment.SpinnerState[] SPINNER_STATES = {
            RecordListFragment.SpinnerState.DATE_ASC,
            RecordListFragment.SpinnerState.DATE_DES};

    protected static final int DEFAULT_SPINNER_STATE_POSITION =
            Arrays.asList(SPINNER_STATES).indexOf(RecordListFragment.SpinnerState.DATE_DES);

    @Override
    public RecordListPresenter createPresenter() {
        return new RecordListPresenter(RecordModel.TRACING);
    }

    @Override
    protected void initOrderSpinner(final RecordListAdapter adapter) {
        orderSpinner.setAdapter(new RecordListFragment.SpinnerAdapter(getActivity(),
                R.layout.case_list_spinner_opened, Arrays.asList(SPINNER_STATES)));
        orderSpinner.setSelection(DEFAULT_SPINNER_STATE_POSITION);
        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                handleItemSelection(position);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

            private void handleItemSelection(int position) {
                RecordService recordService = RecordService.getInstance();
                switch (SPINNER_STATES[position]) {
                    case DATE_ASC:
                        adapter.setRecordList(recordService.getTracingListOrderByDateASC());
                        break;
                    case DATE_DES:
                        adapter.setRecordList(recordService.getTracingListOrderByDateDES());
                        break;
                    default:
                        break;
                }
            }
        });
    }
}
