package org.lucas.task.support;

import java.util.concurrent.ExecutionException;

public class TaskExecutionException extends ExecutionException {
    private static final long serialVersionUID = 7830266012832686185L;

    public TaskExecutionException() { }

    public TaskExecutionException(String message) {
        super(message);
    }

    public TaskExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskExecutionException(Throwable cause) {
        super(cause);
    }
}
