package cn.lingox.android.activity.imagechooser;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import cn.lingox.android.R;
import cn.lingox.android.activity.PhotoActivity;
import cn.lingox.android.entity.Photo;
import cn.lingox.android.helper.ServerHelper;
import cn.lingox.android.utils.FileUtil;
import cn.lingox.android.utils.ImageCache;

public class EditPhotoActivity extends ActionBarActivity implements View.OnClickListener {

    public static final int SELECT_PHOTOS = 101;
    private static final String LOG_TAG = "EditPhotoActivity";


    private ImageView showPhoto;
    private EditText editText;
    private Button post, cancel;
    private Photo photo;
    private Bitmap bitmap;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photos);

        if (getIntent().hasExtra("photo")) {
            photo = getIntent().getParcelableExtra("photo");
        }

        initView();
        setData();
    }

    private void initView() {
        showPhoto = (ImageView) findViewById(R.id.show_photo);
        editText = (EditText) findViewById(R.id.photo_describe);
        post = (Button) findViewById(R.id.photo_post);
        post.setOnClickListener(this);
        cancel = (Button) findViewById(R.id.edit_photo_cancel);
        cancel.setOnClickListener(this);
    }

    private void setData() {
        if (!photo.getDescription().isEmpty()) {
            editText.setText(photo.getDescription());
        }
        String url = photo.getUrl();
        if (ImageCache.getInstance().get(url) != null) {
            bitmap = ImageCache.getInstance().get(photo.getUrl());
            showPhoto.setImageBitmap(bitmap);
        }
        if (FileUtil.getImg(url) != null) {
            bitmap = FileUtil.getImg(photo.getUrl());
            showPhoto.setImageBitmap(bitmap);
        } else {
            Picasso.with(this).load(url).into(showPhoto);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_photo_cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.photo_post:
                new PostPhoto().execute();
                break;
        }
    }

    //    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case SELECT_PHOTOS:
//                if (resultCode == PhotoDialog.RESULT_OK) {
//                    if (data.hasExtra(PhotoDialog.SELECTED_IMAGE_LIST)) {
//                        ArrayList<String> imageURLs =
//                                data.getStringArrayListExtra(PhotoDialog.SELECTED_IMAGE_LIST);
//                        photo.setUrl(imageURLs.get(0));
//                    } else if (data.hasExtra(PhotoDialog.SELECTED_SINGLE_IMAGE)) {
//                        Uri uri = data.getParcelableExtra(PhotoDialog.SELECTED_SINGLE_IMAGE);
//                        photo.setDescription(editText.getText().toString());
//                        photo.setUrl(uri.getPath());
//                    } else {
//                        Log.e(LOG_TAG, "onActivityResult(): SELECT_PHOTOS: Data did not have the expected extra");
//                    }
//                    //展示图片
//                    Picasso.with(this).load("file://" + photo.getUrl()).into(showPhoto);
//                } else if (resultCode == 111) {
//                    finish();
//                } else {
//                    Log.e(LOG_TAG, "onActivityResult(): SELECT_PHOTOS: not RESULT_OK");
//                }
//                break;
//        }
//    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class PostPhoto extends AsyncTask<Void, Void, Boolean> {
        final ProgressDialog pd = new ProgressDialog(EditPhotoActivity.this);
        boolean isSuccess = false;

        @Override
        protected void onPreExecute() {
            pd.setCanceledOnTouchOutside(false);
            pd.show();
            pd.setMessage("Uploading Image");
            post.setClickable(false);
            photo.setDescription(editText.getText().toString().trim());
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                photo = ServerHelper.getInstance().editPhoto(photo);
                isSuccess = true;
            } catch (Exception e) {
                Log.e(LOG_TAG, e.toString());
                isSuccess = false;
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            post.setClickable(true);
            pd.dismiss();
            if (success) {
                Toast.makeText(EditPhotoActivity.this, "Uploaded successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra(PhotoActivity.EDITED_PHOTO, photo);
                setResult(PhotoActivity.RESULT_PHOTO_EDITED, intent);
                finish();
            } else {
                Toast.makeText(EditPhotoActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}