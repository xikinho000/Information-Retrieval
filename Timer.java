/**
 * Developer Kamel Assaf
 * Date modified : 27-May-2018
 * Date updated  : 27-Jul-2022
 */
public class Timer {
	long startTime, endTime, elapsedTime, memAvailable, memUsed;

	public Timer() {
		startTime = System.currentTimeMillis();
	}

	public void start() {
		startTime = System.currentTimeMillis();
	}

	public Timer end() {
		endTime = System.currentTimeMillis();
		elapsedTime = endTime - startTime;
		memAvailable = Runtime.getRuntime().totalMemory();
		memUsed = memAvailable - Runtime.getRuntime().freeMemory();
		return this;
	}

	public String toString() {
		return "Time: " + elapsedTime + " msec.\n";
	}

}
