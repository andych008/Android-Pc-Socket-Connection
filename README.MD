## Android-Pc-Socket-Connection

![](/demo.gif)

### step 1
手机安装 `app`

### step 2
run `ClientApp`，会自动启动手机app、打开socket server，然后与server建立连接

## 说明
[gavinliu/Android-Pc-Socket-Connection](https://github.com/gavinliu/Android-Pc-Socket-Connection)简洁、清晰地演示了android adb forward及socket的用法。在此基础上，我们可以做如下改进、加深理解：
1. socket server作为一个服务放在service也许更合适一些，使其不依赖ui相关的东西。在实际的生产项目中建议这样来用(见`AdbService`)。
2. 通过广播启动、停止`AdbService`(见`AdbServiceBroadcastReceiver`)
3. client连接server后，server read等待；client主动与server断开后，server read返回-1，server重新回到等待连接的状态。
4. server主动停止后，server读写线程都停止，结束`AdbService`。client read返回-1，然后结束`ClientApp`。

## 思考
1. 如果主动停止socket server？（我是通过`mSocket.close()`，然后`InputStreamReader#read()`会抛出`SocketException`,catch后退出线程。InputStreamReader#read()不会被中断，通过中断退出线程是行不通的。）
2. 被动停止client或server，通过`InputStreamReader#read()`的返回值-1应该就是最好的方法。
3. 网上面很多demo都是每增加一个client连接就增加一个thread。因为手机作为server，所以一般不会有多个client的场景。你觉得呢？
4. adb forward说白了就是接口转发，使用上就和普通的socket编程一样。这个说法不知道对不对？



## 参考
[Android-adb-forward实现PC和Android的Socket通讯](http://gavinliu.cn/2016/01/19/Android-adb-forward%E5%AE%9E%E7%8E%B0PC%E5%92%8CAndroid%E7%9A%84Socket%E9%80%9A%E8%AE%AF/)

[Android端通过usb和pc端互传json数据](https://blog.csdn.net/u013553529/article/details/80296870)
