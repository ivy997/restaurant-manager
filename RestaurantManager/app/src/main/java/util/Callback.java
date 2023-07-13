package util;

public interface Callback<T> {
    void onSuccess(T result);
    void onError(String errorMessage);
}
