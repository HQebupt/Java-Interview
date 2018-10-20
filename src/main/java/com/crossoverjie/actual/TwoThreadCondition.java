import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: huangqiang
 * Date: 17/5/2
 * Time: 下午2:24
 */
public class TwoThreadCondition {
  	public static Lock lock = new ReentrantLock();
	public static Condition jiWait = lock.newCondition();
	public static Condition ouWait = lock.newCondition();
	public int num = 0;

	static class PrintJiNum implements Runnable	{
		TwoThreadCondition sol;
		PrintJiNum(TwoThreadCondition sol) {
			this.sol = sol;
		}

		public void run() {
			for(;;) {
				TwoThreadCondition.lock.lock();
				if (sol.num % 2 == 0) {
					sol.num += 1;
					System.out.println("Ji : " +  sol.num);
					TwoThreadCondition.ouWait.signalAll();
				} else {
					try {
						jiWait.await();
					} catch (InterruptedException e) {
						System.err.println("e:" + e);
					}
				}
				TwoThreadCondition.lock.unlock();
				if (sol.num > 500) {
					break;
				}
			}

		}

	}

	static class PrintOuNum implements Runnable	{
		TwoThreadCondition sol;
		PrintOuNum(TwoThreadCondition sol) {
			this.sol = sol;
		}

		public void run() {
			for(;;) {
				TwoThreadCondition.lock.lock();
				if (sol.num % 2 == 0) {
					try {
						ouWait.await();
					} catch (InterruptedException e) {
						System.err.println("e:" + e);
					}

				} else {
					sol.num += 1;
					System.out.println("ou : " +  sol.num);
					TwoThreadCondition.jiWait.signalAll();

				}
				TwoThreadCondition.lock.unlock();
				if (sol.num > 500) {
					break;
				}
			}
		}
	}


	public static void main(String[] args) {
		TwoThreadCondition sol = new TwoThreadCondition();
		Thread ji = new Thread(new PrintJiNum(sol));
		Thread ou = new Thread(new PrintOuNum(sol));
		ji.start();
		ou.start();

	}
}
