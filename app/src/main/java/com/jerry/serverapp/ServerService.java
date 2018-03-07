package com.jerry.serverapp;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jerry.clientapp.IOnBookAddedListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 服务端Service
 * <p>
 * Created by xujierui on 2018/3/5.
 */

public class ServerService extends Service {
    private static final String TAG = ServerService.class.getSimpleName();

    private CopyOnWriteArrayList<Book> bookList = new CopyOnWriteArrayList<>();
    private RemoteCallbackList<IOnBookAddedListener> listenerList = new RemoteCallbackList<>();
    private AtomicBoolean isAlive = new AtomicBoolean(true);

    private Binder serverBinder = new IBookManager.Stub() {

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            // 权限检测
            int permission = checkCallingPermission("com.jerry.serverapp.permission.ACCESS_BOOK_SERVICE");
            if (permission == PackageManager.PERMISSION_DENIED) {
                Log.e(TAG, "onTransact: Access Denied");
                return false;
            }

            return super.onTransact(code, data, reply, flags);
        }

        @Override
        public List<Book> getBookList() throws RemoteException {
            Log.e(TAG, "getBookList: bookListSize = " + bookList.size());
            return bookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            if (book != null) {
                ServerService.this.addBook(book);
            }
        }

        @Override
        public void registerListener(IOnBookAddedListener listener) throws RemoteException {
            if (listener == null) {
                return;
            }

            listenerList.register(listener);
        }

        @Override
        public void unregisterListener(IOnBookAddedListener listener) throws RemoteException {
            if (listener == null) {
                return;
            }

            listenerList.unregister(listener);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e(TAG, "onCreate: ");

        new Thread(new TestThread()).start();
    }

    private class TestThread implements Runnable {
        @Override
        public void run() {
            while (isAlive.get()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.e(TAG, "listenerList.size = " + listenerList.getRegisteredCallbackCount());

                if (listenerList.getRegisteredCallbackCount() <= 0) {
                    continue;
                }

                Book newBook = new Book("New Book", "New Book Author", 10.6f);
                addBook(newBook);
            }
        }
    }

    private void addBook(Book newBook) {
        Log.e(TAG, "addBook: book = " + newBook.toString());
        bookList.add(newBook);

        final int num = listenerList.beginBroadcast();
        for (int i = 0; i < num; i++) {
            IOnBookAddedListener singleListener = listenerList.getBroadcastItem(i);
            if (singleListener == null) {
                continue;
            }

            try {
                singleListener.onBookAdded(newBook);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        listenerList.finishBroadcast();
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: ");
        isAlive.set(false);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serverBinder;
    }
}
