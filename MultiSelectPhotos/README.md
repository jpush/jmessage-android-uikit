# jmessage-android-ui-components
IM SDK UI 组件

浏览、多选本地图片的组件

###用法

- 拷贝browser文件夹下的文件到你的项目
- 拷贝相关资源文件到你的项目（你也可以自定义样式）
- 在XML中将类似的引用路径
```
    <cn.jmessage.android.uikit.browser.ImgBrowserViewPager
        android:id="@+id/img_browser_viewpager"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
    
```
修改为你当前的路径

- 进入选择图片的AlbumListActivity时通过startActivityForResult()的方式来进入,重写onActivityResult()方法,如:
```
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //得到从选择图片返回的Intent
        if (resultCode == RESULT_CODE_SELECT_PICTURE) {
            //得到图片路径
            ArrayList<String> pathList = data.getStringArrayListExtra(PICTURE_PATH);
            for (String path : pathList) {
                Log.d("DemoActivity", "path : " + path);
            }
        }
    }
    
```
这样就能得到发送图片时所选中的图片路径

####使用的开源项目:

- PhotoView [github地址](https://github.com/chrisbanes/PhotoView) 根据手势缩放图片

- NativeImageLoader [blog地址](http://blog.csdn.net/xiaanming/article/details/18730223) 扫描手机中的图片

