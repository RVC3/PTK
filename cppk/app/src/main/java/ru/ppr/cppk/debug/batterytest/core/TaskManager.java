package ru.ppr.cppk.debug.batterytest.core;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nevolin on 11.07.2016.
 */
public enum TaskManager {

    INSTANCE;

    public interface OnTasksEndedListener {
        void onTasksEnded();
    }

    private final List<Task.Builder> tasksBuilders;
    private final ExecutorService tasksThreadPool;

    TaskManager() {
        tasksBuilders = new CopyOnWriteArrayList<>();
        tasksThreadPool = Executors.newSingleThreadExecutor();
    }

    public void set(List<Task.Builder> tasksBuilders) {
        this.tasksBuilders.clear();
        this.tasksBuilders.addAll(tasksBuilders);
    }

    public void execute(OnTasksEndedListener onTasksEndedListener) {
        tasksThreadPool.execute(() -> {
            for(Task.Builder taskBuilder : tasksBuilders) {
                if(tasksBuilders.size() == 0)
                    break;

                Task task = taskBuilder.build();

                task.execute();
            }

            tasksBuilders.clear();

            onTasksEndedListener.onTasksEnded();
        });
    }

    public boolean stillExecuting() {
        return tasksBuilders.size() > 0;
    }

    public void terminate() {
        tasksBuilders.clear();
    }

}
