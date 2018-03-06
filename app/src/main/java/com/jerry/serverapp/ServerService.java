package com.jerry.serverapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 服务端Service
 * <p>
 * Created by xujierui on 2018/3/5.
 */

public class ServerService extends Service {
    private static final String TAG = ServerService.class.getSimpleName();

    private CopyOnWriteArrayList<Book> bookList = new CopyOnWriteArrayList<>();
    private Binder serverBinder = new IBookManager.Stub() {

        @Override
        public List<Book> getBookList() throws RemoteException {
            Log.e(TAG, "getBookList: bookListSize = " + bookList.size());
            return bookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            if (book != null) {
                Log.e(TAG, "addBook: book = " + book.toString());
                bookList.add(book);
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serverBinder;
    }
}
