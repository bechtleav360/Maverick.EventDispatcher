package org.av360.maverick.eventdispatcher.subscriptions.grpc;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.util.Timestamps;
import org.av360.maverick.eventdispatcher.shared.GuavaAdapter;
import org.av360.maverick.eventdispatcher.shared.domain.Subscription;
import org.av360.maverick.eventdispatcher.shared.grpc.*;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;


@Service
public class GrpcClientService {
    Logger log = LoggerFactory.getLogger(GrpcClientService.class);

    @GrpcClient("event-dispatcher")
    private SubscriptionServiceGrpc.SubscriptionServiceFutureStub subscriptionServiceFutureStub;

    @GrpcClient("event-dispatcher")
    private SubscriptionServiceGrpc.SubscriptionServiceStub subscriptionServiceStub;

    private SubscriptionMessage map(Subscription subscription) {
        return SubscriptionMessage.newBuilder()
                .setId(subscription.getId())
                .setCreatedAt(Timestamps.fromMillis(subscription.getCreationDate().toInstant(ZoneOffset.UTC).toEpochMilli()))
                .setSubscriberUri(subscription.getAddressable())
                .putAllFilters(subscription.getFilters())
                .build();
    }


    public Mono<SubscriptionId> sendSubscription(Subscription subscription) {
        ListenableFuture<SubscriptionId> future = this.subscriptionServiceFutureStub.newSubscription(map(subscription));
        return new GuavaAdapter<SubscriptionId>().asMono(future);
    }

    public Flux<Long> sendSubscriptions(Flux<Subscription> publisher) {
        return Flux.create(sink -> {
            StreamObserver<SubscriptionId> responseObserver = new StreamObserver<SubscriptionId>() {
                @Override
                public void onNext(SubscriptionId subscriptionId) {
                    sink.next(subscriptionId.getId());
                }
                @Override
                public void onError(Throwable throwable) {
                   sink.error(throwable);
                }

                @Override
                public void onCompleted() {
                    sink.complete();
                }
            };
            StreamObserver<SubscriptionMessage> requestObserver = this.subscriptionServiceStub.syncSubscriptions(responseObserver);
            publisher.map(this::map).doOnNext(requestObserver::onNext).doOnComplete(requestObserver::onCompleted).subscribe();
        });
    }

    public Mono<Void> deleteSubscription(Long id) {
        ListenableFuture<SubscriptionId> future = this.subscriptionServiceFutureStub.deleteSubscription(SubscriptionId.newBuilder().setId(id.intValue()).build());
        return new GuavaAdapter<SubscriptionId>().asMono(future).then(Mono.empty());
    }

    public Flux<Long> deleteSubscriptions(Flux<Long> publisher) {
        return Flux.create(sink -> {
            StreamObserver<SubscriptionId> responseObserver = new StreamObserver<SubscriptionId>() {
                @Override
                public void onNext(SubscriptionId subscriptionId) {
                    sink.next(subscriptionId.getId());
                }
                @Override
                public void onError(Throwable throwable) {
                    sink.error(throwable);
                }

                @Override
                public void onCompleted() {
                    sink.complete();
                }
            };
            StreamObserver<SubscriptionId> requestObserver = this.subscriptionServiceStub.deleteSubscriptions(responseObserver);
            publisher.map(id -> SubscriptionId.newBuilder().setId(id).build()).doOnNext(requestObserver::onNext).doOnComplete(requestObserver::onCompleted).subscribe();
        });
    }

}
