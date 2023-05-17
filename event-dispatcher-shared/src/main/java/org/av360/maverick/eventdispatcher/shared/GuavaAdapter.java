package org.av360.maverick.eventdispatcher.shared;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import reactor.core.publisher.Mono;

import java.util.concurrent.Executors;

public class GuavaAdapter<T> {


    public Mono<T> asMono(ListenableFuture<T> listenableFuture) {
        return Mono.create(sink -> {
            Futures.addCallback(listenableFuture, new FutureCallback<T>() {
                public void onSuccess(T result) {
                    sink.success(result);
                }

                public void onFailure(Throwable throwable) {
                    sink.error(throwable);
                }
            }, Executors.newSingleThreadExecutor());
        });
    }

}