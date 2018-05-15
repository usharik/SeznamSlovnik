package com.usharik.seznamslovnik;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.internal.functions.Functions;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() throws Exception {
        System.out.println(StringUtils.stripAccents("čekát"));
        System.out.println(StringUtils.stripAccents("aa проду́кт"));
        System.out.println(StringUtils.stripAccents("aa привет"));

        assertEquals(4, 2 + 2);
    }

    @Test
    public void testTest() {

        Completable
                .create(e -> {
                    Thread.sleep(100);
                    e.onComplete();
                })
                .toObservable()
                .subscribe(val -> {
                    Assert.assertEquals("hello", val);
                });

    }

    @Test
    public void testError() {
        Observable.fromCallable(() -> {
            throw new Exception("Test Exception");
        })
                .subscribe(Functions.emptyConsumer(), thr -> System.out.println(thr.getMessage()));
    }

    @Test
    public void testChainOfCompletable() {
        Completable
                .fromAction(() -> System.out.println("Completable 1"))
                .andThen(Completable.fromAction(() -> System.out.println("Completable 2")))
                .andThen(Completable.fromAction(() -> System.out.println("Completable 3")))
                .andThen(Completable.fromAction(() -> System.out.println("Completable 4")))
                .andThen(Completable.fromAction(() -> System.out.println("Completable 5")))
                .subscribe();
    }

    @Test
    public void testCompletableToObservable() {
        Completable.create(e -> {
            System.out.println("Action");
            e.onComplete();
        })
                .andThen(Observable.just("Observable"))
                .flatMap(v -> Observable.fromCallable(() -> {
                    System.out.println("flatMap " + v);
                    return "flatMap";
                }))
                .subscribe(v -> System.out.println("Subscribe " + v));
    }

    @Test
    public void testCompletable() {
        Completable.fromAction(() -> {
            System.out.println("Action");
        })
                .doOnSubscribe(e -> {
                    System.out.println("OnSubscribe");
                })
                .subscribe(() -> {
                    System.out.println("Complete");
                });
    }

    @Test
    public void testMaybe() {
        Maybe.just(1).subscribe(
                e -> System.out.println("onSuccess " + e),
                thr -> {},
                () -> System.out.println("onComplete")
        );

        Maybe.empty().subscribe(
                e -> System.out.println("onSuccess " + e),
                thr -> {},
                () -> System.out.println("onComplete")
        );
    }

}