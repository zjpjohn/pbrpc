package dos.parallel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import dos.parallel.client.ClientExchange;

public class ExchangeFuture<V> implements Future<V> {
    private ClientExchange exchange;
    public ExchangeFuture(ClientExchange exchange) {
        this.exchange = exchange;
    }
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        
        return false;
    }

    @Override
    public boolean isCancelled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDone() {
        return exchange.getDone().get();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        synchronized (exchange.getExchange()) {
            while(!exchange.getDone().get()) {
                exchange.getExchange().wait(2000);;
            }
            try {
                ObjectInputStream oin = new ObjectInputStream(exchange.getDoneExchange().getResult().newInput());
                return (V)oin.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        
        synchronized (exchange.getExchange()) {
            exchange.getExchange().wait(unit.toMillis(timeout));
            if (!this.exchange.getDone().get()) {
                throw new TimeoutException("message timeout");
            }
            try {
                ObjectInputStream oin = new ObjectInputStream(exchange.getDoneExchange().getResult().newInput());
                return (V)oin.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
