package ru.ppr.cppkupdater;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.Socket;

/**
 * Created by michael on 07/10/16.
 */
public class ShellCommand {

    public static String TAG = "ShellCommand";

    public static String GetAppUid(){
        String getIdResult = executeShellCommand("id");
        int indexOfGid = getIdResult.indexOf("gid");
        if(indexOfGid<1){
            return "";
        }
        String temp = getIdResult.substring(indexOfGid);

        int indexOfEnd = getIdResult.indexOf("(");
        if(indexOfEnd<1){
            return "";
        }

        return (temp.substring(4,indexOfEnd));

    }

    public static String executeShellCommand(String command) {
        String res = "";
        DataOutputStream outputStream = null;
        InputStream response = null;
        try{
            Process cmdProcess = Runtime.getRuntime().exec(command);
            response = cmdProcess.getInputStream();
            try {
                cmdProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            res = readFully(response);
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            closeSilently(response);
        }
        return res;
     }

    public static String readFully(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toString("UTF-8");
    }

    public static void closeSilently(Object... xs) {
        // Note: on Android API levels prior to 19 Socket does not implement Closeable
        for (Object x : xs) {
            if (x != null) {
                try {
                    Log.d(TAG, "closing: "+x);
                    if (x instanceof Closeable) {
                        ((Closeable)x).close();
                    } else if (x instanceof Socket) {
                        ((Socket)x).close();
                    } else if (x instanceof DatagramSocket) {
                        ((DatagramSocket)x).close();
                    } else {
                        Log.d(TAG, "cannot close: "+x);
                        throw new RuntimeException("cannot close "+x);
                    }
                } catch (Throwable e) {
                    Log.e(TAG, e.toString());
                }
            }
        }
    }

}
