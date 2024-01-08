package com.aldrineeinsteen.fun.options;

public abstract class UtilityTemplate implements Runnable {

    @Override
    public void run() {
        logStart();
        runUtility();
    }

    protected abstract void logStart();

    protected abstract void runUtility();
}
