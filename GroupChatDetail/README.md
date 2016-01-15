# jmessage-android-ui-components
IM SDK UI 组件


简单的群聊详情组件, 实现了添加删除群成员功能. 

###用法:

- 复制groupchatdetail文件夹下的文件到你的项目.

- 配置AndroidManifest, 详情参考[JChat的AndroidManifest配置](https://github.com/jpush/jchat-android/blob/master/README.md)

- 复制资源文件到你的项目, 你可以自定义界面的样式

- 在XML文件中将引用路径修改为你当前的路径, 如图:
  ![如图](https://github.com/KenChoi1992/jchat-android/raw/dev/JChat/screenshots/screenshot6.png)

- 配置用户信息, 在MainActivity中设置登录用户和群组id,然后传递到GroupDetailActivity.

- 关于注册用户和创建群组

你可以调用JMessageClient.register(username, password, callback)来注册用户, 也可以使用curl的方式注册用户:

```

curl --insecure -X POST -v https://api.im.jpush.cn/v1/users -H "Content-Type: application/json" -u "your AppKey:your master secret" -d '[{"username":"user003", "password": "1111"}]'

```

注册群组, 同样可以使用JMessageClient.createGroup("", "", new CreateGroupCallback(){}), 或者使用curl:

```

curl --insecure -X POST -v https://api.im.jpush.cn/v1/groups -H "Content-Type: application/json" -u "your AppKey:your master secret" -d '{"owner_username":"user001", "name": "example group", "members_username":["user002","user003"],"desc":"example"}'

```

