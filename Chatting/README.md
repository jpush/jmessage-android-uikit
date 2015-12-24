# jmessage-android-ui-components
IM SDK UI 组件

##简单的聊天组件, 实现了单聊和群聊功能. 

用法:

1、复制chatting文件夹下的文件到你的项目.

2、配置AndroidManifest, 更换包名, AppKey和applicationId.

3、复制资源文件到你的项目, 你可以自定义界面的样式

4、在XML文件中引用这些组件, 比如:

```
<cn.jmessage.android.uicomponents.chatting.ChatView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/chat_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    xmlns:listViewAttr="http://schemas.android.com/apk/res-auto"
    android:fitsSystemWindows="true"
    android:orientation="vertical">
    </cn.jmessage.android.uicomponents.chatting.ChatView>

```
别忘了更改为你的包名

5、配置用户信息, 使用UserConfig提供的方法更改登录用户信息.你可以调用JMessageClient.register(username, password, callback)

来注册用户, 也可以使用curl的方式注册用户:

```

curl --insecure -X POST -v https://api.im.jpush.cn/v1/users -H "Content-Type: application/json" -u "your AppKey:your master secret" -d '[{"username":"user003", "password": "1111"}]'

```

注册群组, 同样可以使用JMessageClient.createGroup("", "", new CreateGroupCallback(){}), 或者使用curl:

```

curl --insecure -X POST -v https://api.im.jpush.cn/v1/groups -H "Content-Type: application/json" -u "your AppKey:your master secret" -d '{"owner_username":"user001", "name": "example group", "members_username":["user002","user003"],"desc":"example"}'

```

##This demo implements functions of single chat and group chat.

Usage:

1.copy the files in chatting folder to your project.

2.Config AndroidManifest. Use demo's package name and AppKey is OK, or you can replace with your

package name and AppKey. When you change package name, you should change the applicationId in 

build.gradle file, too.

3.copy the resources that this demo needed to your project. You can custom your views by changing

styles and resources.

4.Quote <ChatView> in your XML file, like

```
<cn.jmessage.android.uicomponents.chatting.ChatView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/chat_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    xmlns:listViewAttr="http://schemas.android.com/apk/res-auto"
    android:fitsSystemWindows="true"
    android:orientation="vertical">

</cn.jmessage.android.uicomponents.chatting.ChatView>
    
```
Don't forget to replace the package name to yours.

5.Config user information. In file UserConfig, you can set your username and password, or groupId,

but, first if you change the package name and AppKey, you need to make sure if the user or group has

been registered.

To register user, you can call JMessageClient.register(username, password, callback) or use terminal

command "curl" like this:

```

curl --insecure -X POST -v https://api.im.jpush.cn/v1/users -H "Content-Type: application/json" -u "your AppKey:your master secret" -d '[{"username":"user003", "password": "1111"}]'

```

To create group, you can call JMessageClient.createGroup("", "", new CreateGroupCallback() {}) or use

terminal command like this:

```

curl --insecure -X POST -v https://api.im.jpush.cn/v1/groups -H "Content-Type: application/json" -u "your AppKey:your master secret" -d '{"owner_username":"user001", "name": "example group", "members_username":["user002","user003"],"desc":"example"}'

```
