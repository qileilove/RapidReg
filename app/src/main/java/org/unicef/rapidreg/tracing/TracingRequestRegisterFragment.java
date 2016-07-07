package org.unicef.rapidreg.tracing;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hannesdorfmann.mosby.mvp.MvpFragment;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;

import org.unicef.rapidreg.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TracingRequestRegisterFragment extends MvpFragment<TracingRequestRegisterView, TracingRequestRegisterPresenter>
        implements TracingRequestRegisterView {

    @BindView(R.id.register_forms_content)
    RecyclerView fieldList;

    private TracingRequestRegisterAdapter caseRegisterAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_cases_register, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        int position = FragmentPagerItem.getPosition(getArguments());
        presenter.initContext(getActivity(), position);
    }

    @NonNull
    @Override
    public TracingRequestRegisterPresenter createPresenter() {
        return new TracingRequestRegisterPresenter();
    }

    @Override
    public void initView(TracingRequestRegisterAdapter adapter) {
        caseRegisterAdapter = adapter;
        RecyclerView.LayoutManager layout = new LinearLayoutManager(getContext());
        layout.setAutoMeasureEnabled(true);
        fieldList.setLayoutManager(layout);
        fieldList.setAdapter(caseRegisterAdapter);
    }

    public TracingRequestRegisterAdapter getCaseRegisterAdapter() {
        return caseRegisterAdapter;
    }

    public void clearFocus() {
        View focusedChild = fieldList.getFocusedChild();
        if (focusedChild != null) {
            focusedChild.clearFocus();
        }
    }
}
