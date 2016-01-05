package cn.jmessage.android.uikit.browser;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import cn.jmessage.android.uikit.R;

public class AlbumListView extends LinearLayout {

    private ListView mListView;

    public AlbumListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void initModule() {
        mListView = (ListView) findViewById(R.id.pick_picture_total_list_view);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mListView.setOnItemClickListener(listener);
    }

    public void setAdapter(AlbumListAdapter adapter) {
        mListView.setAdapter(adapter);
    }

    public ListView getListView() {
        return mListView;
    }
}
