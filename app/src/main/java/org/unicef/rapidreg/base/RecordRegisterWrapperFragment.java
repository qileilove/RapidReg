package org.unicef.rapidreg.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentStatePagerItemAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicef.rapidreg.R;
import org.unicef.rapidreg.childcase.media.CasePhotoAdapter;
import org.unicef.rapidreg.event.UpdateImageEvent;
import org.unicef.rapidreg.forms.Field;
import org.unicef.rapidreg.forms.RecordForm;
import org.unicef.rapidreg.forms.Section;
import org.unicef.rapidreg.model.CasePhoto;
import org.unicef.rapidreg.service.CasePhotoService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class RecordRegisterWrapperFragment extends Fragment {
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

    @BindView(R.id.edit_case)
    FloatingActionButton editCaseButton;

    protected RecordForm form;
    protected List<Section> sections;
    protected List<Field> miniFields;
    protected RecordRegisterAdapter miniFormAdapter;
    protected RecordRegisterAdapter fullFormAdapter;
    protected CasePhotoAdapter casePhotoAdapter;

    protected long recordId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_cases_register_wrapper, container, false);
        ButterKnife.bind(this, view);

        initFloatingActionButton();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void initFloatingActionButton() {
        if (((RecordActivity) getActivity()).getCurrentFeature().isDetailMode()) {
            editCaseButton.setVisibility(View.VISIBLE);
        } else {
            editCaseButton.setVisibility(View.GONE);
        }
    }

    protected void initFullFormContainer() {
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
                fullFormAdapter = ((RecordRegisterFragment) adapter.getPage(position)).getRegisterAdapter();
                fullFormAdapter.setCasePhotoAdapter(casePhotoAdapter);
                fullFormAdapter.notifyDataSetChanged();
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

    protected void initMiniFormContainer() {
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
                        fullFormAdapter.setCasePhotoAdapter(casePhotoAdapter);
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

    protected CasePhotoAdapter initCasePhotoAdapter() {
        casePhotoAdapter = new CasePhotoAdapter(getContext(), new ArrayList<String>());

        List<CasePhoto> casesPhotoFlowQueryList = CasePhotoService.getInstance().getAllCasesPhotoFlowQueryList(recordId);
        for (int i = 0; i < casesPhotoFlowQueryList.size(); i++) {
            casePhotoAdapter.addItem(casesPhotoFlowQueryList.get(i).getId());
        }
        return casePhotoAdapter;
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true, priority = 1)
    public void updateImageAdapter(UpdateImageEvent event) {
        casePhotoAdapter.addItem(event.getImagePath());
        ImageButton view = (ImageButton) getActivity().findViewById(R.id.add_image_button);

        if (!casePhotoAdapter.isEmpty()) {
            view.setImageResource(R.drawable.photo_add);
        }
        if (casePhotoAdapter.isFull()) {
            view.setVisibility(View.GONE);
        }

        casePhotoAdapter.notifyDataSetChanged();
        EventBus.getDefault().removeStickyEvent(event);

    }

    protected void getMiniFields() {
        for (Section section : sections) {
            for (Field field : section.getFields()) {
                if (field.isShowOnMiniForm()) {
                    if (field.isPhotoUploadBox()) {
                        miniFields.add(0, field);
                    } else {
                        miniFields.add(field);
                    }
                }
            }
        }
        addProfileFieldForDetailsPage();
    }

    private void addProfileFieldForDetailsPage() {
        if (((RecordActivity) getActivity()).getCurrentFeature().isDetailMode()) {
            Field field = new Field();
            field.setType(Field.TYPE_MINI_FORM_PROFILE);
            try {
                miniFields.add(1, field);
            } catch (Exception e) {
                miniFields.add(field);
            }
        }
    }

    public void clearFocus() {
        View focusedChild = miniFormContainer.getFocusedChild();
        if (focusedChild != null) {
            focusedChild.clearFocus();
        }

        FragmentStatePagerItemAdapter adapter =
                (FragmentStatePagerItemAdapter) viewPager.getAdapter();
        RecordRegisterFragment fragment = (RecordRegisterFragment) adapter
                .getPage(viewPager.getCurrentItem());
        if (fragment != null) {
            fragment.clearFocus();
        }
    }

    protected abstract FragmentPagerItems getPages();
}
