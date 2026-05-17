package com.arkaly.desktop.utils;

import javafx.application.Platform;

public final class AsyncTask {
    private AsyncTask() {}

    public static void ejecutar(TareaAsync enBackground, Runnable despues) {
        new Thread(() -> {
            try {
                enBackground.run();
                Platform.runLater(despues);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FunctionalInterface
    public interface TareaAsync {
        void run() throws Exception;
    }
}