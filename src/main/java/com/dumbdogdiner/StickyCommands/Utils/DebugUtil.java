package com.dumbdogdiner.StickyCommands.Utils;

import com.dumbdogdiner.StickyCommands.Main;

public class DebugUtil {
    private static Main self = Main.getPlugin(Main.class);

    /**
     * @return The line number of the code that ran this method
     * @author Brian_Entei
     */
    public static int getLineNumber() {
        return ___8drrd3148796d_Xaf();
    }

    /**
     * This methods name is ridiculous on purpose to prevent any other method names
     * in the stack trace from potentially matching this one.
     * 
     * @return The line number of the code that called the method that called this
     *         method(Should only be called by getLineNumber()).
     * @author Brian_Entei
     */
    private static int ___8drrd3148796d_Xaf() {
        boolean thisOne = false;
        int thisOneCountDown = 1;
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : elements) {
            String methodName = element.getMethodName();
            int lineNum = element.getLineNumber();
            if (thisOne && (thisOneCountDown == 0)) {
                return lineNum;
            } else if (thisOne) {
                thisOneCountDown--;
            }
            if (methodName.equals("___8drrd3148796d_Xaf")) {
                thisOne = true;
            }
        }
        return -1;
    }

    /**
     * Prints a debug message
     * @param message The debug message to print
     * @param javaClass The class that it's printing in
     * @param line The line number
    */
    public static void sendDebug(String message, Class<?> javaClass, Integer line) {
        if (self.getConfig().getBoolean("debug") == true) {
            System.out.println("["+javaClass.getSimpleName()+": "+line+"] " + message);
        }
    }

}