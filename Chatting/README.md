# jmessage-android-ui-components
IM SDK UI 组件

简单的聊天组件, 实现了单聊和群聊功能. 

###用法:

- 复制chatting文件夹下的文件到你的项目(utils中的可以根据自己需求修改).

- 配置AndroidManifest, 将所需的权限及Receiver, Service等拷贝到你的AndroidManifest中:
```
<permission
        android:name="${applicationId}.permission.JPUSH_MESSAGE"
        android:protectionLevel="signature" />
    <!--Required 一些系统要求的权限，如访问网络等-->
    <uses-permission android:name="${applicationId}.permission.JPUSH_MESSAGE" />
    <uses-permission android:name="android.permission.RECEIVE_USER_PRESENT" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.WRITE_SETTINGS" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_LOCATION_EXTRA_COMMANDS"/>
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
    <!-- JMessage Demo required for record audio-->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS"/>
```
   以下放在application下 
```
            <!--以下放在application下 -->
            <service
                android:name="cn.jpush.android.service.PushService"
                android:enabled="true"
                android:exported="false"
                android:process=":remote">
                <intent-filter>
                    <action android:name="cn.jpush.android.intent.REGISTER" />
                    <action android:name="cn.jpush.android.intent.REPORT" />
                    <action android:name="cn.jpush.android.intent.PushService" />
                    <action android:name="cn.jpush.android.intent.PUSH_TIME" />
                </intent-filter>
            </service>
            <receiver
                android:name="cn.jpush.android.service.PushReceiver"
                android:enabled="true">
                <intent-filter android:priority="1000">
                    <action android:name="cn.jpush.android.intent.NOTIFICATION_RECEIVED_PROXY" />
                    <category android:name="${applicationId}" />
                </intent-filter>
                <intent-filter>
                    <action android:name="android.intent.action.USER_PRESENT" />
                    <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
                </intent-filter>
                <!-- Optional -->
                <intent-filter>
                    <action android:name="android.intent.action.PACKAGE_ADDED" />
                    <action android:name="android.intent.action.PACKAGE_REMOVED" />
                    <data android:scheme="package" />
                </intent-filter>
            </receiver>
            <activity
                android:name="cn.jpush.android.ui.PushActivity"
                android:configChanges="orientation|keyboardHidden"
                android:theme="@android:style/Theme.Translucent.NoTitleBar">
                <intent-filter>
                    <action android:name="cn.jpush.android.ui.PushActivity" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="${applicationId}" />
                </intent-filter>
            </activity>
            <service
                android:name="cn.jpush.android.service.DownloadService"
                android:enabled="true"
                android:exported="false" />
            <!-- Required Push SDK核心功能 -->
            <receiver android:name="cn.jpush.android.service.AlarmReceiver" />
            <!-- IM Required IM SDK核心功能-->
            <receiver
                android:name="cn.jpush.im.android.helpers.IMReceiver"
                android:enabled="true"
                android:exported="false">
                <intent-filter android:priority="1000">
                    <action android:name="cn.jpush.im.android.action.IM_RESPONSE" />
                    <action android:name="cn.jpush.im.android.action.NOTIFICATION_CLICK_PROXY" />
                    <category android:name="${applicationId}" />
                </intent-filter>
            </receiver>
             <!-- option 可选项。用于同一设备中不同应用的JPush服务相互拉起的功能。 -->
             <!-- 若不启用该功能可删除该组件，将不拉起其他应用也不能被其他应用拉起 -->
             <service
                android:name="cn.jpush.android.service.DaemonService"
                android:enabled="true"
                android:exported="true">
                <intent-filter>
                    <action android:name="cn.jpush.android.intent.DaemonService" />
                    <category android:name="${applicationId}" />
                </intent-filter>
             </service>
            <meta-data
                android:name="JPUSH_CHANNEL"
                android:value="developer-default" />
            <!-- Required. AppKey copied from Portal -->
            <meta-data
                android:name="JPUSH_APPKEY"
                android:value="5fbb6030a7c7b853dc199ea0" />
    
```
别忘了配置applicationId或者替换为你的包名, AppKey也要替换为你在极光控制台上注册的应用所对应的AppKey.
配置applicationId需要在build.gradle的defaultConfig中声明:
![如图](https://github.com/KenChoi1992/jchat-android/raw/dev/JChat/screenshots/screenshot3.png)

- 复制资源文件到你的项目, 你可以自定义界面的样式

- 在XML文件中将引用路径修改为你当前的路径(红色方框部分)
![如图](https://github.com/KenChoi1992/jchat-android/raw/dev/JChat/screenshots/screenshot6.png)

- 配置用户信息, 在MainActivity这个入口Activity中配置用户信息(包括登录用户, 聊天用户及群聊id), 可以使用Intent传递到ChatActivity. 你可以调用JMessageClient.register(username, password, callback)来注册用户, 也可以使用curl的方式注册用户:

```

curl --insecure -X POST -v https://api.im.jpush.cn/v1/users -H "Content-Type: application/json" -u "your AppKey:your master secret" -d '[{"username":"user003", "password": "1111"}]'

```

注册群组, 同样可以使用JMessageClient.createGroup("", "", new CreateGroupCallback(){}), 或者使用curl:

```

curl --insecure -X POST -v https://api.im.jpush.cn/v1/groups -H "Content-Type: application/json" -u "your AppKey:your master secret" -d '{"owner_username":"user001", "name": "example group", "members_username":["user002","user003"],"desc":"example"}'

```
####项目中所使用的开源项目简单说明

- android-shape-imageview [github地址](https://github.com/siyamed/android-shape-imageview) 自定义ImageView的形状

- PhotoView [github地址](https://github.com/chrisbanes/PhotoView) 根据手势缩放图片

- DropDownListView [github地址](https://github.com/Trinea/android-common) 下拉刷新ListView

