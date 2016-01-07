package cn.jmessage.android.uikit.multiselectphotos;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.jmessage.android.uikit.R;


public class AlbumListActivity extends BaseActivity implements AdapterView.OnItemClickListener{

    public static final int REQUEST_CODE_SELECT_PICTURE = 6;
    public static final int RESULT_CODE_SELECT_PICTURE = 8;
    private AlbumListView mAlbumView;
    private AlbumListAdapter adapter;
    private HashMap<String, List<String>> mGruopMap = new HashMap<String, List<String>>();
    private List<ImageBean> list = new ArrayList<ImageBean>();
    private final static int SCAN_OK = 1;
    private final static int SCAN_ERROR = 2;
    private ProgressDialog mProgressDialog;
    private final MyHandler myHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jmui_activity_album_list);
        mAlbumView = (AlbumListView) findViewById(R.id.album_view);
        mAlbumView.initModule();
        mAlbumView.setOnItemClickListener(this);
        getImages();
    }

    /**
     * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中
     */
    private void getImages() {
        //显示进度条
        mProgressDialog = ProgressDialog.show(this, null, this.getString(R.string.loading));

        new Thread(new Runnable() {

            @Override
            public void run() {
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = getContentResolver();

                //只查询jpeg和png的图片
                Cursor mCursor = mContentResolver.query(mImageUri, null,
                        MediaStore.Images.Media.MIME_TYPE + "=? or "
                                + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[] { "image/jpeg", "image/png" }, MediaStore.Images.Media.DATE_MODIFIED);
                if (mCursor == null || mCursor.getCount() == 0) {
                    myHandler.sendEmptyMessage(SCAN_ERROR);
                }else {
                    while (mCursor.moveToNext()) {
                        //获取图片的路径
                        String path = mCursor.getString(mCursor
                                .getColumnIndex(MediaStore.Images.Media.DATA));

                        try{
                            //获取该图片的父路径名
                            String parentName = new File(path).getParentFile().getName();
                            //根据父路径名将图片放入到mGruopMap中
                            if (!mGruopMap.containsKey(parentName)) {
                                List<String> chileList = new ArrayList<String>();
                                chileList.add(path);
                                mGruopMap.put(parentName, chileList);
                            } else {
                                mGruopMap.get(parentName).add(path);
                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mCursor.close();
                    //通知Handler扫描图片完成
                    myHandler.sendEmptyMessage(SCAN_OK);
                }
            }
        }).start();

    }

    @Override
    protected void onPause() {
        mProgressDialog.dismiss();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        if (resultCode == RESULT_CODE_SELECT_PICTURE) {
            setResult(RESULT_CODE_SELECT_PICTURE, data);
            finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        List<String> childList = mGruopMap.get(list.get(position).getFolderName());
        Intent intent = new Intent();
        intent.setClass(AlbumListActivity.this, PickPictureActivity.class);
        intent.putExtra("albumName", list.get(position).getFolderName());
        intent.putStringArrayListExtra("data", (ArrayList<String>) childList);
        startActivityForResult(intent, REQUEST_CODE_SELECT_PICTURE);
    }

    private static class MyHandler extends Handler {
        private final WeakReference<AlbumListActivity> mActivity;

        public MyHandler(AlbumListActivity activity) {
            mActivity = new WeakReference<AlbumListActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            AlbumListActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case SCAN_OK:
                        activity.mProgressDialog.dismiss();
                        activity.adapter = new AlbumListAdapter(activity, activity.list = activity
                                .subGroupOfImage(activity.mGruopMap), activity.mAlbumView.getListView(),
                                activity.mDensity);
                        activity.mAlbumView.setAdapter(activity.adapter);
                        break;
                    case SCAN_ERROR:
                        activity.mProgressDialog.dismiss();
                        Toast.makeText(activity, activity.getString(R.string.sdcard_not_prepare_toast), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }

    /**
     * 组装分组界面GridView的数据源，因为我们扫描手机的时候将图片信息放在HashMap中
     * 所以需要遍历HashMap将数据组装成List
     *
     * @param mGruopMap 相册HashMap
     * @return List<ImageBean>
     */
    private List<ImageBean> subGroupOfImage(HashMap<String, List<String>> mGruopMap) {
        if (mGruopMap.size() == 0) {
            return null;
        }
        List<ImageBean> list = new ArrayList<ImageBean>();

        Iterator<Map.Entry<String, List<String>>> it = mGruopMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<String>> entry = it.next();
            ImageBean mImageBean = new ImageBean();
            String key = entry.getKey();
            List<String> value = entry.getValue();
            SortPictureList sortList = new SortPictureList();
            Collections.sort(value, sortList);
            mImageBean.setFolderName(key);
            mImageBean.setImageCounts(value.size());
            mImageBean.setTopImagePath(value.get(0));//获取该组的第一张图片

            list.add(mImageBean);
        }

        //对相册进行排序，最近修改的相册放在最前面
        SortImageBeanComparator sortComparator = new SortImageBeanComparator(list);
        Collections.sort(list, sortComparator);

        return list;

    }

    static class SortImageBeanComparator implements Comparator<ImageBean> {

        List<ImageBean> list;

        public SortImageBeanComparator(List<ImageBean> list){
            this.list = list;
        }

        //根据相册的第一张图片进行排序，最近修改的放在前面
        public int compare(ImageBean arg0, ImageBean arg1) {
            String path1 = arg0.getTopImagePath();
            String path2 = arg1.getTopImagePath();
            File f1 = new File(path1);
            File f2 = new File(path2);
            if (f1.lastModified() < f2.lastModified()) {
                return 1;
            }else {
                return -1;
            }
        }
    }
}
