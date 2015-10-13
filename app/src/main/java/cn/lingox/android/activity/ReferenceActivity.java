package cn.lingox.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

import cn.lingox.android.R;
import cn.lingox.android.adapter.ReferenceAdapter;
import cn.lingox.android.app.LingoXApplication;
import cn.lingox.android.entity.Reference;
import cn.lingox.android.entity.User;
import cn.lingox.android.helper.CacheHelper;
import cn.lingox.android.helper.ServerHelper;

public class ReferenceActivity extends Activity implements OnClickListener {
    public static final String INTENT_USER_REFERENCE = LingoXApplication.PACKAGE_NAME + ".USER_REFERENCE";
    // Intent Extras
    public static final String INTENT_TARGET_USER_ID = LingoXApplication.PACKAGE_NAME + ".TARGET_USER_ID";
    public static final String INTENT_TARGET_USER_NAME = LingoXApplication.PACKAGE_NAME + ".TARGET_USER_NAME";
    public static final String INTENT_REFERENCE = LingoXApplication.PACKAGE_NAME + ".REFERENCE";
    public static final String INTENT_REQUEST_CODE = LingoXApplication.PACKAGE_NAME + ".REQUEST_CODE";
    // Request code
    static final int ADD_REFERENCE = 1;
    static final int EDIT_REFERENCE = 2;
    static final int VIEW_REFERENCE = 3;
    private static final String LOG_TAG = "ReferenceActivity";
    // Data Elements
    private boolean ownReferencesPage;
    private ArrayList<Reference> referenceList;
    private String userId;
    private String userName;

    private boolean isBothFollowed = false;

    // UI Elements
    private ImageView addReference;
    private LinearLayout back, add;
    private ListView listView;
    private ReferenceAdapter arrayAdapter;

    private ImageView anim;
    private AnimationDrawable animationDrawable;

    private ProgressBar pb;

    private int addRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reference);
        initView();
        Intent intent = getIntent();
        addRef = intent.getIntExtra("addReference", 0);
        userId = intent.getStringExtra(UserInfoFragment.TARGET_USER_ID);
        userName = intent.getStringExtra(UserInfoFragment.TARGET_USER_NAME);
        if (intent.hasExtra(UserInfoFragment.REFERENCES)) {
            referenceList = intent.getParcelableArrayListExtra(UserInfoFragment.REFERENCES);
            initData();
        } else {
            referenceList = new ArrayList<>();
            pb.setVisibility(View.VISIBLE);
            new LoadUserReferences().execute(userId);
        }

        ownReferencesPage = CacheHelper.getInstance().getSelfInfo().getId().equals(userId);
    }

    private void initView() {

        anim = (ImageView) findViewById(R.id.anim);
        animationDrawable = (AnimationDrawable) anim.getBackground();

        pb = (ProgressBar) findViewById(R.id.progress);

        addReference = (ImageView) findViewById(R.id.iv_add_reference);

        // If we are viewing our own references
        // TODO implement reference managing for own reference page
        if (ownReferencesPage) {
            addReference.setVisibility(View.INVISIBLE);
        } else {
            addReference.setVisibility(View.VISIBLE);
        }
        addReference.setOnClickListener(this);

        back = (LinearLayout) findViewById(R.id.layout_back);
        back.setOnClickListener(this);

        add = (LinearLayout) findViewById(R.id.layout_add);
        add.setOnClickListener(this);

        listView = (ListView) findViewById(R.id.list);
//        listView.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Reference selectedReference = referenceList.get(position);
//                boolean ownReference =
//                        CacheHelper.getInstance().getSelfInfo().getId().equals(selectedReference.getUserSrcId());
//                if (ownReference) {//自己对别人的评价
//                    Intent intent = new Intent(ReferenceActivity.this, ReferenceDialog.class);
//                    intent.putExtra(INTENT_REFERENCE, selectedReference);
//                    intent.putExtra(INTENT_TARGET_USER_ID, userId);
//                    intent.putExtra(INTENT_TARGET_USER_NAME, userName);
//                    intent.putExtra(INTENT_REQUEST_CODE, EDIT_REFERENCE);
//                    startActivityForResult(intent, EDIT_REFERENCE);
//                } else {//别人对自己评价
//                    Intent userInfoIntent = new Intent(ReferenceActivity.this, UserInfoActivity.class);
//                    userInfoIntent.putExtra(UserInfoActivity.INTENT_USER_ID, selectedReference.getUserSrcId());
//                    startActivity(userInfoIntent);
//                }
//            }
//        });

        if (addRef == 1) {
            Intent intent = new Intent(this, ReferenceDialog.class);
            intent.putExtra(INTENT_TARGET_USER_ID, userId);
            intent.putExtra(INTENT_TARGET_USER_NAME, userName);
            intent.putExtra(INTENT_REQUEST_CODE, ADD_REFERENCE);
            startActivityForResult(intent, ADD_REFERENCE);
        }
    }

    private void initData() {
        if (referenceList.size() == 0) {
            startAnim();
            listView.setVisibility(View.GONE);
        } else {
            stopAnim();
            listView.setVisibility(View.VISIBLE);
            arrayAdapter = new ReferenceAdapter(this, referenceList);
            listView.setAdapter(arrayAdapter);
            updateList();
        }
    }

    private void updateList() {
        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_add_reference:
//                if (getIntent().hasExtra(UserInfoFragment.REFERENCES)) {
//                    new GetBothFollowed().execute();
//                }else{
                Intent intent = new Intent(this, ReferenceDialog.class);
                intent.putExtra(INTENT_TARGET_USER_ID, userId);
                intent.putExtra(INTENT_TARGET_USER_NAME, userName);
                intent.putExtra(INTENT_REQUEST_CODE, ADD_REFERENCE);
                startActivityForResult(intent, ADD_REFERENCE);
//                }
                break;
            case R.id.layout_back:
                ReferenceActivity.this.finish();
                break;
        }
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra(INTENT_USER_REFERENCE, referenceList);
        setResult(RESULT_OK, intent);
        super.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == ReferenceDialog.FAILURE) {
            if (data != null)
                if (data.hasExtra("remark"))
                    Toast.makeText(this, data.getStringExtra("remark"),
                            Toast.LENGTH_LONG).show();
        } else {
            switch (requestCode) {
                case ADD_REFERENCE:
                    if (resultCode == ReferenceDialog.SUCCESS_ADD) {
                        referenceList.add((Reference) data
                                .getParcelableExtra(ReferenceDialog.ADDED_REFERENCE));
                        updateList();
                    }
                    break;
                case EDIT_REFERENCE:
                    if (resultCode == ReferenceDialog.SUCCESS_EDIT) {
                        int referenceIndex = findReferenceInList(referenceList, (Reference) data.getParcelableExtra(ReferenceDialog.REFERENCE_BEFORE_EDIT));
                        referenceList.set(referenceIndex, (Reference) data.getParcelableExtra(ReferenceDialog.REFERENCE_AFTER_EDIT));
                    } else if (resultCode == ReferenceDialog.SUCCESS_DELETE) {
                        int referenceIndex = findReferenceInList(referenceList, (Reference) data.getParcelableExtra(ReferenceDialog.DELETED_REFERENCE));
                        referenceList.remove(referenceIndex);
                    }
                    updateList();
                    break;
                case VIEW_REFERENCE:
                    // Do nothing
                    break;
            }
        }
    }

    // Helper methods
    private int findReferenceInList(ArrayList<Reference> list, Reference ref) {
        for (Reference refs : list) {
            if (refs.getId().equals(ref.getId())) {
                return list.indexOf(refs);
            }
        }
        return -1;
    }

    @Override
    protected void onResume() {
        MobclickAgent.onResume(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(this);
        super.onPause();
    }

    private void startAnim() {
        if (!animationDrawable.isRunning()) {
            anim.setVisibility(View.VISIBLE);
            animationDrawable.start();
        }
    }

    private void stopAnim() {
        if (animationDrawable.isRunning()) {
            anim.setVisibility(View.GONE);
            animationDrawable.stop();
        }
    }

    //获取双方是否相互
//    private class GetBothFollowed extends AsyncTask<Void,Void,Boolean>{
//        @Override
//        protected Boolean doInBackground(Void... params) {
//            try {
//                isBothFollowed=ServerHelper.getInstance().getBothFollowed(CacheHelper.getInstance().getSelfInfo().getId()
//                ,
//                        userId);
//            }catch (Exception e){
//                Log.e(LOG_TAG,e.getMessage());
//            }
//            return isBothFollowed;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean aBoolean) {
//            if (aBoolean){
//                //相互follow
//                Intent intent = new Intent(ReferenceActivity.this, ReferenceDialog.class);
//                intent.putExtra(INTENT_TARGET_USER_ID, userId);
//                intent.putExtra(INTENT_TARGET_USER_NAME, userName);
//                intent.putExtra(INTENT_REQUEST_CODE, ADD_REFERENCE);
//                startActivityForResult(intent, ADD_REFERENCE);
//            }else{
//                new AlertDialog.Builder(ReferenceActivity.this)
//                        .setMessage("相互关注才能评论")
//                        .create().show();
//            }
//        }
//    }


    //下载评论
    private class LoadUserReferences extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            referenceList.clear();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean success = false;
            try {
                referenceList.addAll(ServerHelper.getInstance().getUsersReferences(params[0]));
                success = true;
                for (int i = 0; i < referenceList.size(); i++) {
                    try {
                        User user = ServerHelper.getInstance().getUserInfo(referenceList.get(i).getUserSrcId());
                        CacheHelper.getInstance().addUserInfo(user);
                    } catch (Exception e2) {
                        Log.e(LOG_TAG, "Inner Exception caught: " + e2.toString());
                    }
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception caught: " + e.toString());
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            pb.setVisibility(View.INVISIBLE);
            if (success) {
                initData();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to get User's References", Toast.LENGTH_LONG).show();
            }
        }
    }
}
