package org.unicef.rapidreg.tracing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.Toast;

import org.unicef.rapidreg.R;
import org.unicef.rapidreg.base.view.BaseActivity;
import org.unicef.rapidreg.childcase.media.CasePhotoAdapter;
import org.unicef.rapidreg.forms.Section;
import org.unicef.rapidreg.forms.TracingFormRoot;
import org.unicef.rapidreg.service.TracingFormService;
import org.unicef.rapidreg.service.TracingService;
import org.unicef.rapidreg.service.cache.CasePhotoCache;
import org.unicef.rapidreg.service.cache.FieldValueCache;
import org.unicef.rapidreg.service.cache.SubformCache;
import org.unicef.rapidreg.utils.ImageCompressUtil;
import org.unicef.rapidreg.widgets.viewholder.PhotoUploadViewHolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TracingRequestActivity extends BaseActivity {
    private DetailState textAreaState = DetailState.VISIBILITY;

    private MenuItem caseSaveMenu;
    private MenuItem caseSearchMenu;
    private MenuItem caseToggleMenu;

    private String imagePath;
    private TracingRequestFeature currentFeature;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initToolbar();

        turnToFeature(TracingRequestFeature.LIST);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (TextUtils.isEmpty(imagePath)) {
            return;
        }

        GridView photoGrid = (GridView) findViewById(R.id.photo_grid);

        List<Bitmap> previousPhotos = CasePhotoCache.getPhotosBits();
        Bitmap newPhoto = ImageCompressUtil.rotateBitmapByExif(imagePath,
                ImageCompressUtil.getThumbnail(imagePath, 160));

        previousPhotos.add(newPhoto);
        CasePhotoCache.addPhoto(newPhoto, imagePath);

        if (CasePhotoCache.isUnderLimit()) {
            previousPhotos.add(BitmapFactory.decodeResource(getResources(), R.drawable.photo_add));
        }
        photoGrid.setAdapter(new CasePhotoAdapter(this, previousPhotos));

        imagePath = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Activity.RESULT_OK != resultCode) {
            return;
        }

        if (PhotoUploadViewHolder.REQUEST_CODE_GALLERY == requestCode) {
            onSelectFromGalleryResult(data);
        } else if (PhotoUploadViewHolder.REQUEST_CODE_CAMERA == requestCode) {
            onCaptureImageResult();
        }
    }

    @Override
    public boolean isInDetailMode() {
        return currentFeature.isInDetailMode();
    }

    @Override
    public boolean isInEditMode() {
        return currentFeature.isInEditMode();
    }

    private void onSelectFromGalleryResult(Intent data) {
        Uri uri = data.getData();
        if (!TextUtils.isEmpty(uri.getAuthority())) {
            Cursor cursor = getContentResolver().query(uri,
                    new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            cursor.moveToFirst();
            imagePath = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        }
    }

    private void onCaptureImageResult() {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(CasePhotoCache.MEDIA_PATH_FOR_CAMERA);
            imagePath = getOutputMediaFilePath();
            ImageCompressUtil.storeImage(bitmap, imagePath);
            bitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void processBackButton() {
        if (currentFeature.isInListMode()) {
            logOut(this);
        } else if (currentFeature.isInEditMode()) {
            showQuitDialog(R.id.nav_tracing);
        } else {
            FieldValueCache.clearAudioFile();
            turnToFeature(TracingRequestFeature.LIST);
        }
    }

    @Override
    protected void navCaseAction() {
        if (currentFeature.isInDetailMode()) {
            showQuitDialog(R.id.nav_tracing);
        } else {
            FieldValueCache.clearAudioFile();
            intentSender.showCasesActivity(this, null, false);
        }
    }

    @Override
    protected void navTracingRequestAction() {
        if (currentFeature.isInEditMode()) {
            showQuitDialog(R.id.nav_tracing);
        } else {
            FieldValueCache.clearAudioFile();
            turnToFeature(TracingRequestFeature.LIST);
        }
    }

    @Override
    protected void navSyncAction() {
        if (currentFeature.isInEditMode()) {
            showQuitDialog(R.id.nav_sync);
        } else {
            FieldValueCache.clearAudioFile();
            intentSender.showSyncActivity(this);
        }
    }

    public void turnToFeature(TracingRequestFeature feature) {
        currentFeature = feature;
        changeToolbarTitle(feature.getTitleId());
        changeToolbarIcon(feature);
        try {
            navToFragment(feature.getFragment());
        } catch (Exception e) {
            throw new RuntimeException("Fragment navigation error", e);
        }
    }

    public TracingRequestFeature getCurrentFeature() {
        return currentFeature;
    }

    private void showQuitDialog(final int clickedButton) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.quit)
                .setMessage(R.string.quit_without_saving)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FieldValueCache.clearAudioFile();
                        switch (clickedButton) {
                            case R.id.nav_tracing:
                                turnToFeature(TracingRequestFeature.LIST);
                                break;
                            case R.id.nav_sync:
                                intentSender.showSyncActivity(TracingRequestActivity.this);
                                break;
                            default:
                                break;
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void initToolbar() {
        setNavMenuItem(R.id.nav_tracing);

        toolbar.inflateMenu(R.menu.toolbar_main);
        toolbar.setOnMenuItemClickListener(new CaseMenuItemListener());

        caseSaveMenu = toolbar.getMenu().findItem(R.id.save);
        caseSearchMenu = toolbar.getMenu().findItem(R.id.search);
        caseToggleMenu = toolbar.getMenu().findItem(R.id.toggle);
    }

    private String getOutputMediaFilePath() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + File.separator + getApplicationContext().getPackageName());
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
        }
        return mediaStorageDir.getPath() + File.separator + System.currentTimeMillis() + ".jpg";
    }


    private class CaseMenuItemListener implements Toolbar.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.toggle:
                    showHideTracingDetail();
                    return true;
                case R.id.search:
                    turnToFeature(TracingRequestFeature.SEARCH);
                    return true;
                case R.id.save:
                    return saveTracings();
                default:
                    return false;
            }
        }
    }

    private void showHideTracingDetail() {
        textAreaState = textAreaState.getNextState();

        caseToggleMenu.setIcon(textAreaState.getResId());
        TracingRequestListFragment tracingRequestListFragment = (TracingRequestListFragment) getSupportFragmentManager()
                .findFragmentByTag(TracingRequestListFragment.class.getSimpleName());
        tracingRequestListFragment.toggleMode(textAreaState.isDetailShow());
    }

    private void clearFocusToMakeLastFieldSaved() {
        TracingRequestRegisterWrapperFragment fragment =
                (TracingRequestRegisterWrapperFragment) getSupportFragmentManager()
                        .findFragmentByTag(TracingRequestRegisterWrapperFragment.class.getSimpleName());

        if (fragment != null) {
            fragment.clearFocus();
        }
    }

    private boolean saveTracings() {
        clearFocusToMakeLastFieldSaved();
        if (validateRequiredField()) {
            Map<Bitmap, String> photoBitPaths = CasePhotoCache.getPhotoBitPaths();
            TracingService.getInstance().saveOrUpdateTracing(FieldValueCache.getValues(),
                    SubformCache.getValues(),
                    photoBitPaths);
            turnToFeature(TracingRequestFeature.LIST);
        }
        return true;
    }

    private boolean validateRequiredField() {
        TracingFormRoot form = TracingFormService.getInstance().getCurrentForm();
        List<String> requiredFieldNames = new ArrayList<>();

        for (Section section : form.getSections()) {
            Collections.addAll(requiredFieldNames, TracingService.getInstance()
                    .fetchRequiredFiledNames(section.getFields()).toArray(new String[0]));
        }

        for (String field : requiredFieldNames) {
            if (TextUtils.isEmpty(FieldValueCache.getValues().get(field))) {
                Toast.makeText(TracingRequestActivity.this, R.string.required_field_is_not_filled,
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    private void hideAllToolbarIcons() {
        caseToggleMenu.setVisible(false);
        caseSearchMenu.setVisible(false);
        caseSaveMenu.setVisible(false);
    }

    private void changeToolbarIcon(TracingRequestFeature feature) {
        hideAllToolbarIcons();

        switch (feature) {
            case LIST:
                caseToggleMenu.setVisible(true);
                caseSearchMenu.setVisible(true);
                break;
            case EDIT:
            case ADD:
                caseSaveMenu.setVisible(true);
                break;
            default:
                break;
        }
    }

    private void changeToolbarTitle(int resId) {
        toolbar.setTitle(resId);
    }

    private void navToFragment(Fragment target) {
        if (target != null) {
            String tag = target.getClass().getSimpleName();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_content, target, tag).commit();
        }
    }

    public enum DetailState {
        VISIBILITY(R.drawable.visible, true),
        INVISIBILITY(R.drawable.invisible, false);

        private final int resId;
        private final boolean isDetailShow;

        DetailState(int resId, boolean isDetailShow) {
            this.resId = resId;
            this.isDetailShow = isDetailShow;
        }

        public DetailState getNextState() {
            return this == VISIBILITY ? INVISIBILITY : VISIBILITY;
        }

        public int getResId() {
            return resId;
        }

        public boolean isDetailShow() {
            return isDetailShow;
        }
    }
}
