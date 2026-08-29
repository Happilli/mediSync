package com.bca.medisync.data.remote.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ParallelEnricher {

  public interface Callback1<T> {
    void onResult(T result);
  }

  public static <T, R, O> void run(
      List<T> items,
      Function<T, Call<R>> callFactory,
      BiFunction<T, R, O> mapper,
      Callback1<List<O>> onDone) {

    if (items.isEmpty()) {
      onDone.onResult(new ArrayList<>());
      return;
    }

    List<O> result = new ArrayList<>();
    AtomicInteger remaining = new AtomicInteger(items.size());

    for (T item : items) {
      callFactory
          .apply(item)
          .enqueue(
              new Callback<R>() {
                @Override
                public void onResponse(Call<R> call, Response<R> response) {
                  O mapped = mapper.apply(item, response.isSuccessful() ? response.body() : null);
                  synchronized (result) {
                    result.add(mapped);
                  }
                  if (remaining.decrementAndGet() == 0) onDone.onResult(result);
                }

                @Override
                public void onFailure(Call<R> call, Throwable t) {
                  O mapped = mapper.apply(item, null);
                  synchronized (result) {
                    result.add(mapped);
                  }
                  if (remaining.decrementAndGet() == 0) onDone.onResult(result);
                }
              });
    }
  }
}
