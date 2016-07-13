package org.unicef.rapidreg.tracing;

import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.AdapterView;

import org.unicef.rapidreg.R;
import org.unicef.rapidreg.base.RecordListAdapter;
import org.unicef.rapidreg.base.RecordListFragment;
import org.unicef.rapidreg.base.RecordListPresenter;
import org.unicef.rapidreg.model.RecordModel;
import org.unicef.rapidreg.service.TracingService;

import java.util.Arrays;
import java.util.List;

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
    protected void initListContainer(RecordListAdapter adapter) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listContainer.setLayoutManager(layoutManager);
        listContainer.setAdapter(adapter);

        List<? extends RecordModel> recordList = TracingService.getInstance().getTracingList();
        int index = recordList.isEmpty() ? HAVE_NO_RESULT : HAVE_RESULT_LIST;
        viewSwitcher.setDisplayedChild(index);
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
                TracingService tracingService = TracingService.getInstance();
                switch (SPINNER_STATES[position]) {
                    case DATE_ASC:
                        adapter.setRecordList(tracingService.getTracingListOrderByDateASC());
                        break;
                    case DATE_DES:
                        adapter.setRecordList(tracingService.getTracingListOrderByDateDES());
                        break;
                    default:
                        break;
                }
            }
        });
    }
}
