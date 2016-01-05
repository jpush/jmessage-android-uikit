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

- Tips

在实际运用中, 进入选择图片的AlbumListActivity时可以通过startActivityForResult()的方式来进入,这样可以在选择完图片后使用
setResult的方式将所选的图片路径返回. 可以参考从PickPictureActivity进入BrowserViewPagerActivity的方式,以及从
BrowserViewPagerActivity得到所选的图片后返回PickPictureActivity的方式.

- 使用的开源项目:

- PhotoView [github地址](https://github.com/chrisbanes/PhotoView) 根据手势缩放图片

- NativeImageLoader [blog](http://blog.csdn.net/xiaanming/article/details/18730223) 扫描手机中的图片

