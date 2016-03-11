package com.opencredo.concourse.domain.commands.dispatching;

import com.opencredo.concourse.domain.commands.Command;
import com.opencredo.concourse.domain.commands.CommandException;
import com.opencredo.concourse.domain.commands.CommandResult;
import com.opencredo.concourse.domain.functional.UnsafeFunction;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public interface CommandBus extends Function<Command, CompletableFuture<CommandResult>> {

    default CompletableFuture<Optional<Object>> dispatchAsync(Command command) {
        return apply(command).thenApply(UnsafeFunction.of(result -> {
            if (result.succeeded()) {
                return result.getResultValue();
            } else {
                throw result.getException();
            }
        }));
    }

    default Optional<Object> dispatch(Command command) {
        try {
            CommandResult result = apply(command).get();
            if (result.succeeded()) {
                return result.getResultValue();
            }
            throw new CommandException(result.getException());
        } catch (InterruptedException e) {
            throw new CommandException(e);
        } catch (ExecutionException e) {
            throw new CommandException(e.getCause());
        }
    }

}
