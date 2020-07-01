package ru.ppr.cppk.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import ru.ppr.logger.Logger;

/**
 * @author Kevin Kowalewski
 */
public class RootTools {

    private static String LOG_TAG = RootTools.class.getName();

    public static enum SHELL_CMD {
        check_su_binary(new String[]{"/system/xbin/which", "su"});

        String[] command;

        SHELL_CMD(String[] command) {
            this.command = command;
        }
    }

    public boolean isDeviceRooted() {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
    }

    public boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    public boolean checkRootMethod2() {
        try {
            File file = new File("/system/app/Superuser.apk");
            return file.exists();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean checkRootMethod3() {
        return new ExecShell().executeCommand(SHELL_CMD.check_su_binary) != null;
    }

    /**
     * @author Kevin Kowalewski
     */
    public class ExecShell {

        public ArrayList<String> executeCommand(SHELL_CMD shellCmd) {
            String line = null;
            ArrayList<String> fullResponse = new ArrayList<String>();
            Process localProcess = null;
            try {
                localProcess = Runtime.getRuntime().exec(shellCmd.command);
            } catch (Exception e) {
                return null;
            }
            // BufferedWriter out = new BufferedWriter(new
            // OutputStreamWriter(localProcess.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
            try {
                while ((line = in.readLine()) != null) {
                    Logger.trace(LOG_TAG, "--> Line received: " + line);
                    fullResponse.add(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Logger.trace(LOG_TAG, "--> Full response was: " + fullResponse);
            return fullResponse;
        }
    }

}
