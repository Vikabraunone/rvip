import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DiningPhilosophers {
    private final ReentrantLock[] priorities = new ReentrantLock[] {
            new ReentrantLock(),
            new ReentrantLock(),
            new ReentrantLock(),
            new ReentrantLock(),
            new ReentrantLock()
    };

    private final List<Condition> priorityCond = new ArrayList<>(5);
    private final byte[] hasPriority = new byte[]{ -1, -1, -1, -1, -1};

    public DiningPhilosophers() {
        for (ReentrantLock priority : priorities) {
            priorityCond.add(priority.newCondition());
        }
    }

    public void wantsToEat(int philosopher, Runnable pickLeftFork, Runnable pickRightFork, Runnable eat,
                           Runnable putLeftFork, Runnable putRightFork) throws InterruptedException {
        int leftFork, rightFork, firstFork, secondFork;
        byte fIndex = 0;
        Runnable pickFirst = pickLeftFork;
        Runnable pickSecond = pickRightFork;
        Runnable putFirst = putLeftFork;
        Runnable putSecond = putRightFork;
        leftFork = (philosopher + 1) % 5;
        rightFork = philosopher;
        firstFork = Math.min(leftFork, rightFork);
        if (firstFork == leftFork){
            secondFork = rightFork;
        }
        else {
            pickFirst = pickRightFork;
            pickSecond = pickLeftFork;
            putFirst =putRightFork;
            putSecond = putLeftFork;
            secondFork = leftFork;
            fIndex = 1;
        }

        priorities[firstFork].lock();
        while (hasPriority[firstFork] == 1 - fIndex && priorities[firstFork].hasQueuedThreads()){
            priorityCond.get(firstFork).await();
        }

        pickFirst.run();
        priorities[secondFork].lock();
        while (hasPriority[secondFork] == fIndex && priorities[secondFork].hasQueuedThreads()){
            priorityCond.get(secondFork).await();
        }

        pickSecond.run();
        eat.run();

        hasPriority[secondFork] = fIndex;
        putSecond.run();
        priorityCond.get(secondFork).signal();
        priorities[secondFork].unlock();

        hasPriority[firstFork] = (byte)(1-fIndex);
        putFirst.run();
        priorityCond.get(firstFork).signal();
        priorities[firstFork].unlock();
    }
}