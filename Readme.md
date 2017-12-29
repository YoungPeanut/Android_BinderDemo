

### AIDL
1 class Stub extends android.os.Binder implements cs.shawn.binderdemo.aidl.IRemoteService
存根Stub是IRemoteService的内部类，它既是IRemoteService又是Binder

2 C/S模型
Activity和Service在两个进程中，
aidl是二者交互的桥梁，对activity来说它是interface，对service来说它是IBinder。
bind方式启动服务，onBind(Intent intent)返回IBinder，onServiceConnected()得到后转为interface。
关键是aidl的Stub既是IRemoteService又是Binder。

3 data/reply  输入/输出
```

@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_registerCallback:
{
data.enforceInterface(DESCRIPTOR);
cs.shawn.binderdemo.aidl.IRemoteServiceCallback _arg0;
_arg0 = cs.shawn.binderdemo.aidl.IRemoteServiceCallback.Stub.asInterface(data.readStrongBinder());
this.registerCallback(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_unregisterCallback:
{
data.enforceInterface(DESCRIPTOR);
cs.shawn.binderdemo.aidl.IRemoteServiceCallback _arg0;
_arg0 = cs.shawn.binderdemo.aidl.IRemoteServiceCallback.Stub.asInterface(data.readStrongBinder());
this.unregisterCallback(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}

```

* code 是一个整形的唯一标识，用于区分执行哪个方法，客户端会传递此参数，告诉服务端执行哪个方法
* data客户端传递过来的参数
* replay服务器返回回去的值
* flags标明是否有返回值，0为有（双向），1为没有（单向）

4
```

            Intent intent = new Intent(Binding.this, RemoteService.class);
            intent.setAction(IRemoteService.class.getName());
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            intent.setAction(ISecondary.class.getName());
            bindService(intent, mSecondaryConnection, Context.BIND_AUTO_CREATE);

```
onCreate 1次
onBind 2次  没有onRebind
onUnbind 2次
kill的时候才会 onDestroy

### Messenger
1
远程和本地的Messenger互发Message进行通信，各自的handler处理message。
通信的数据类型受限于bundle支持的类型。
