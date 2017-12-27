
1 class Stub extends android.os.Binder implements cs.shawn.binderdemo.aidl.IRemoteService
存根Stub是IRemoteService的内部类，它既是IRemoteService又是Binder

2 C/S模型
Activity和Service在两个进程中，
aidl是二者交互的桥梁，对activity来说它是interface，对service来说它是IBinder。
bind方式启动服务，onBind(Intent intent)返回IBinder，onServiceConnected()得到后转为interface。
关键是aidl的Stub既是IRemoteService又是Binder。
