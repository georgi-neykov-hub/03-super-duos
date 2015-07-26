package it.jaschke.alexandria.data;

public class LoaderResult<D> {
    private D data;
    private Throwable error;
    private boolean loadSuccessful;

    public LoaderResult(D data) {
        this.data = data;
        this.loadSuccessful = true;
    }

    public LoaderResult(Throwable error) {
        this.error = error;
        this.loadSuccessful = false;
    }

    public D getData() {
        return data;
    }

    public Throwable getError() {
        return error;
    }

    public boolean isLoadSuccessful(){
        return loadSuccessful;
    }
}
