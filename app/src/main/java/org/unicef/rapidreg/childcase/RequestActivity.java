package org.unicef.rapidreg.childcase;

import android.app.Activity;
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

import org.greenrobot.eventbus.EventBus;
import org.unicef.rapidreg.R;
import org.unicef.rapidreg.base.view.BaseActivity;
import org.unicef.rapidreg.childcase.config.CasePhotoConfig;
import org.unicef.rapidreg.event.UpdateImageEvent;
import org.unicef.rapidreg.utils.ImageCompressUtil;
import org.unicef.rapidreg.widgets.viewholder.PhotoUploadViewHolder;

import java.io.File;
import java.io.IOException;

public abstract class RequestActivity extends BaseActivity {
    protected DetailState textAreaState = DetailState.VISIBILITY;
    protected Feature currentFeature;

    protected MenuItem showHideMenu;
    protected MenuItem saveMenu;
    protected MenuItem searchMenu;

    private String imagePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initToolbar();
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

    private void onSelectFromGalleryResult(Intent data) {
        Uri uri = data.getData();
        if (!TextUtils.isEmpty(uri.getAuthority())) {
            Cursor cursor = getContentResolver().query(uri,
                    new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            cursor.moveToFirst();
            imagePath = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
            postSelectedImagePath();
        }
    }

    public Feature getCurrentFeature() {
        return currentFeature;
    }

    public void turnToFeature(Feature feature, Bundle args) {
        currentFeature = feature;
        changeToolbarTitle(feature.getTitleId());
        changeToolbarIcon(feature);
        try {
            Fragment fragment = feature.getFragment();
            if (args != null) {
                fragment.setArguments(args);
            }
            navToFragment(fragment);
        } catch (Exception e) {
            throw new RuntimeException("Fragment navigation error", e);
        }
    }

    private String getOutputMediaFilePath() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + File.separator + getApplicationContext().getPackageName());
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
        }
        return mediaStorageDir.getPath() + File.separator + System.currentTimeMillis() + ".jpg";
    }

    private void onCaptureImageResult() {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(CasePhotoConfig.MEDIA_PATH_FOR_CAMERA);
            imagePath = getOutputMediaFilePath();
            ImageCompressUtil.storeImage(bitmap, imagePath);
            bitmap.recycle();
            postSelectedImagePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void postSelectedImagePath() {
        UpdateImageEvent event = new UpdateImageEvent();
        event.setImagePath(imagePath);
        EventBus.getDefault().postSticky(event);
    }

    private void initToolbar() {
        toolbar.inflateMenu(R.menu.toolbar_main);
        toolbar.setOnMenuItemClickListener(new MenuItemListener());

        saveMenu = toolbar.getMenu().findItem(R.id.save_case);
        searchMenu = toolbar.getMenu().findItem(R.id.search);
        showHideMenu = toolbar.getMenu().findItem(R.id.toggle);
    }

    protected void changeToolbarTitle(int resId) {
        toolbar.setTitle(resId);
    }

    protected void changeToolbarIcon(Feature feature) {
        hideAllToolbarIcons();

        if (feature.isListMode()) {
            showHideMenu.setVisible(true);
            searchMenu.setVisible(true);
        } else if (feature.isEditMode()) {
            saveMenu.setVisible(true);
        }
    }

    private void hideAllToolbarIcons() {
        showHideMenu.setVisible(false);
        searchMenu.setVisible(false);
        saveMenu.setVisible(false);
    }

    private void navToFragment(Fragment target) {
        if (target != null) {
            String tag = target.getClass().getSimpleName();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_content, target, tag).commit();
        }
    }

    private void showHideDetail() {
        textAreaState = textAreaState.getNextState();

        showHideMenu.setIcon(textAreaState.getResId());
        ListFragment listFragment = (ListFragment) getSupportFragmentManager()
                .findFragmentByTag(ListFragment.class.getSimpleName());
        listFragment.toggleMode(textAreaState.isDetailShow());
    }

    protected abstract void save();

    private class MenuItemListener implements Toolbar.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.toggle:
                    showHideDetail();
                    return true;
                case R.id.search:
                    turnToFeature(Feature.SEARCH, null);
                    return true;
                case R.id.save_case:
                    save();
                    return true;
                default:
                    return false;
            }
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
