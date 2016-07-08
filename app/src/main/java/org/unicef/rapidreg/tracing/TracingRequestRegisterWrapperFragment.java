package org.unicef.rapidreg.tracing;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentStatePagerItemAdapter;

import org.unicef.rapidreg.R;
import org.unicef.rapidreg.base.view.SwipeChangeLayout;
import org.unicef.rapidreg.forms.Field;
import org.unicef.rapidreg.forms.Section;
import org.unicef.rapidreg.forms.TracingFormRoot;
import org.unicef.rapidreg.service.TracingFormService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TracingRequestRegisterWrapperFragment extends Fragment {

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.viewpagertab)
    SmartTabLayout viewPagerTab;

    @BindView(R.id.mini_form_layout)
    RelativeLayout miniFormLayout;

    @BindView(R.id.full_form_layout)
    RelativeLayout fullFormLayout;

    @BindView(R.id.full_form_swipe_layout)
    SwipeChangeLayout fullFormSwipeLayout;

    @BindView(R.id.mini_form_swipe_layout)
    SwipeChangeLayout miniFormSwipeLayout;

    @BindView(R.id.mini_form_container)
    RecyclerView miniFormContainer;

    @BindView(R.id.edit)
    FloatingActionButton editButton;

    private TracingFormRoot tracingForm;
    private List<Section> sections;
    private List<Field> miniFields;
    private TracingRequestRegisterAdapter miniFormAdapter;
    private TracingRequestRegisterAdapter fullFormAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_cases_register_wrapper, container, false);
        ButterKnife.bind(this, view);
        initFormData();
        initFloatingActionButton();
        miniFormAdapter = new TracingRequestRegisterAdapter(getActivity(), miniFields, true);
        initFullFormContainer();
        initMiniFormContainer();
        return view;
    }

    @OnClick(R.id.edit)
    public void onCaseEditClicked() {
        ((TracingRequestActivity) getActivity()).turnToFeature(TracingRequestFeature.EDIT);
    }

    private void initFloatingActionButton() {
        if (((TracingRequestActivity) getActivity()).getCurrentFeature() == TracingRequestFeature.DETAILS) {
            editButton.setVisibility(View.VISIBLE);
        } else {
            editButton.setVisibility(View.GONE);
        }
    }

    private void initMiniFormContainer() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        if (!miniFields.isEmpty()) {
            miniFormContainer.setLayoutManager(layoutManager);
            miniFormContainer.setAdapter(miniFormAdapter);
            miniFormSwipeLayout.setDragEdge(SwipeChangeLayout.DragEdge.BOTTOM);
            miniFormSwipeLayout.setShouldGoneContainer(miniFormLayout);
            miniFormSwipeLayout.setShouldShowContainer(fullFormLayout);
            miniFormSwipeLayout.setScrollChild(miniFormContainer);
            miniFormSwipeLayout.setOnSwipeBackListener(new SwipeChangeLayout.SwipeBackListener() {
                @Override
                public void onViewPositionChanged(float fractionAnchor, float fractionScreen) {
                    if (fullFormAdapter != null) {
                        fullFormAdapter.notifyDataSetChanged();
                    }
                }
            });
        } else {
            miniFormSwipeLayout.setEnableFlingBack(false);
            miniFormLayout.setVisibility(View.GONE);
            fullFormLayout.setVisibility(View.VISIBLE);
            fullFormSwipeLayout.setEnableFlingBack(false);
        }
    }

    private void initFullFormContainer() {
        final FragmentStatePagerItemAdapter adapter = new FragmentStatePagerItemAdapter(
                getActivity().getSupportFragmentManager(), getPages());
        viewPager.setAdapter(adapter);
        viewPagerTab.setViewPager(viewPager);

        viewPagerTab.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                fullFormSwipeLayout.setScrollChild(
                        adapter.getPage(position).getView()
                                .findViewById(R.id.register_forms_content));
                fullFormAdapter = ((TracingRequestRegisterFragment) adapter.getPage(position)).getCaseRegisterAdapter();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (miniFields.size() != 0) {
            fullFormSwipeLayout.setDragEdge(SwipeChangeLayout.DragEdge.TOP);
            fullFormSwipeLayout.setShouldGoneContainer(fullFormLayout);
            fullFormSwipeLayout.setShouldShowContainer(miniFormLayout);
            fullFormSwipeLayout.setOnSwipeBackListener(new SwipeChangeLayout.SwipeBackListener() {
                @Override
                public void onViewPositionChanged(float fractionAnchor, float fractionScreen) {
                    miniFormAdapter.notifyDataSetChanged();
                }
            });
        } else {
            fullFormSwipeLayout.setEnableFlingBack(false);
        }
    }

    private void initFormData() {
        tracingForm = TracingFormService.getInstance().getCurrentForm();
        sections = tracingForm.getSections();
        miniFields = new ArrayList<>();
        if (tracingForm != null) {
            getMiniFields();
        }
    }

    private void getMiniFields() {
        for (Section section : sections) {
            for (Field caseField : section.getFields()) {
                if (caseField.isShowOnMiniForm()) {
                    if (caseField.isPhotoUploadBox()) {
                        miniFields.add(0, caseField);
                    } else {
                        miniFields.add(caseField);
                    }
                }
            }
        }
        if (!miniFields.isEmpty()) {
            addProfileFieldForDetailsPage();
        }
    }

    private void addProfileFieldForDetailsPage() {
        if (((TracingRequestActivity) getActivity()).getCurrentFeature() == TracingRequestFeature.DETAILS) {
            Field tracingField = new Field();
            tracingField.setType(Field.TYPE_MINI_FORM_PROFILE);
            try {
                miniFields.add(1, tracingField);
            } catch (Exception e) {
                miniFields.add(tracingField);
            }
        }
    }

    @NonNull
    private FragmentPagerItems getPages() {
        FragmentPagerItems pages = new FragmentPagerItems(getActivity());
        for (Section section : sections) {
            String[] values = section.getName().values().toArray(new String[0]);
            pages.add(FragmentPagerItem.of(values[0], TracingRequestRegisterFragment.class));
        }
        return pages;
    }

    public void clearFocus() {
        View focusedChild = miniFormContainer.getFocusedChild();
        if (focusedChild != null) {
            focusedChild.clearFocus();
        }

        FragmentStatePagerItemAdapter adapter =
                (FragmentStatePagerItemAdapter) viewPager.getAdapter();
        TracingRequestRegisterFragment fragment = (TracingRequestRegisterFragment) adapter
                .getPage(viewPager.getCurrentItem());
        if (fragment != null) {
            fragment.clearFocus();
        }
    }
}
