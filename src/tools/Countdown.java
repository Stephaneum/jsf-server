package tools;

import java.util.ArrayList;
import java.util.List;

public class Countdown {
	
	static private List<Timer> runnables = new ArrayList<Timer>();
	
	//nach einer Zeitangabe wird eine Methode ausgeführt
	static public void start(final int time_milli_sec, final Action action, final Object key) {
		
		//existiert bereits ein Thread?
		if(key != null) {
			for(int i = 0; i < runnables.size(); i++) {
				
				Timer timer = runnables.get(i);
				if(timer.key == key) {
					//found
					timer.restart(time_milli_sec);
					return;
				}
			}
		}
		
		Timer runnable = new Timer(time_milli_sec,action,key);
		new Thread(runnable).start();
		runnables.add(runnable);
	}
	
	static public void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	static private class Timer implements Runnable {
		
		private int time_milli_sec;
		private Action action;
		private Object key;
		
		//restart
		private boolean restart = false;
		private int next_time_milli_sec;
		
		Timer(int time_milli_sec, Action action, Object key) {
			this.time_milli_sec = time_milli_sec;
			this.action = action;
			this.key = key;
		}

		@Override
		public void run() {
			long start_time = System.currentTimeMillis();
			
			while(start_time + time_milli_sec > System.currentTimeMillis()) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if(restart) {
					start_time = System.currentTimeMillis();
					restart = false;
					time_milli_sec = next_time_milli_sec;
				}
			}
			
			action.startAction();
		}
		
		public void restart(int time_milli_sec) {
			restart = true;
			next_time_milli_sec = time_milli_sec;
		}
		
	}
}
