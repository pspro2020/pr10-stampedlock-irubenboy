package classes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

public class Product {

    private final int id;
    private static final List<Integer> stock = new ArrayList<>();
    private final StampedLock stampedLock = new StampedLock();
    private final DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss");

    public Product(int id){
        if(id <= 0) throw new IllegalArgumentException("Non validate arguments");
        this.id = id;
        stock.add(id-1,0);
        stock.set(id-1, stock.get(id-1)+1);
    }

    public void consultStock() throws InterruptedException {
        long stamp = stampedLock.tryOptimisticRead();
        getStock(stamp);
    }

    public void getStock(long stamp) throws InterruptedException {
        System.out.printf("%s -> %s consulting stock...\n", LocalDateTime.now().format(f),
                Thread.currentThread().getName());
        TimeUnit.SECONDS.sleep(5);
        int value = stock.get(id-1);
        if(!stampedLock.validate(stamp)){
            stamp = stampedLock.readLock();
            try {
                value = stock.get(id-1);
            } finally {
                stampedLock.unlock(stamp);
            }
        }
        System.out.printf("%s -> %s There %s %d stocks of Product #%d\n", LocalDateTime.now().format(f),
                Thread.currentThread().getName(), stock.get(id-1) > 1 ? "are" : "is", stock.get(id-1), id);
    }

    public void updateStock() throws InterruptedException {
        long stamp =stampedLock.writeLock();
        try {
            incrementStock();
        } finally {
            stampedLock.unlock(stamp);
        }
    }

    private void incrementStock() throws InterruptedException {
        System.out.printf("%s -> %s is updating stock...\n", LocalDateTime.now().format(f),
                Thread.currentThread().getName());
        TimeUnit.SECONDS.sleep(3);
        stock.set(id-1, stock.get(id-1)+1);
        System.out.printf("%s -> %s New Stock: %d\n",
                LocalDateTime.now().format(f),
                Thread.currentThread().getName(),
                stock.get(id-1));
    }
}
