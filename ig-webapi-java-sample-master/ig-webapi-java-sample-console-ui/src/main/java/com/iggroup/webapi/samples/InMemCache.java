package com.iggroup.webapi.samples;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.iggroup.db.model.SeasonalRunModel;

public class InMemCache<K, T> {
    private Map<Object, Object> cacheMap;
 
    protected class CacheObject {
        public long lastAccessed = System.currentTimeMillis();
        public T value;
 
        protected CacheObject(T value) {
            this.value = value;
        }
    }
 
    public InMemCache(final long TimerInterval, int maxItems) {
 
        cacheMap = new HashMap<Object, Object>();
 
        if (TimerInterval > 0) {
 
            Thread t = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(TimerInterval * 1000);
                        } catch (InterruptedException ex) {
                        	
                        }
                        //refresh();
                    }
                }
            });
 
            t.setDaemon(true);
            t.start();
        }
    }
 
    public void put(K key, T value) {
        synchronized (cacheMap) {
            cacheMap.put(key, new CacheObject(value));
        }
    }
 
    @SuppressWarnings("unchecked")
    public T get(K key) {
        synchronized (cacheMap) {
        	CacheObject c = (CacheObject) cacheMap.get(key);
 
            if (c == null)
                return null;
            else {
                c.lastAccessed = System.currentTimeMillis();
                return c.value;
            }
        }
    }
 
    public void remove(K key) {
        synchronized (cacheMap) {
            cacheMap.remove(key);
        }
    }
 
    public int size() {
        synchronized (cacheMap) {
            return cacheMap.size();
        }
    }
 
    /*
    @SuppressWarnings("unchecked")
    public void refresh() {
 
        long now = System.currentTimeMillis();
        ArrayList<K> deleteKey = null;
 
        synchronized (cacheMap) {
            MapIterator itr = cacheMap.mapIterator();
 
            deleteKey = new ArrayList<K>((cacheMap.size() / 2) + 1);
            K key = null;
            CacheObject c = null;
 
            while (itr.hasNext()) {
                key = (K) itr.next();
                c = (CacheObject) itr.getValue();
 
                if (c != null && (now > (timeToLive + c.lastAccessed))) {
                    deleteKey.add(key);
                }
            }
        }
 
        for (K key : deleteKey) {
            synchronized (cacheMap) {
                cacheMap.remove(key);
            }
 
            Thread.yield();
        }
    }*/
//    public static void main(String[] args) throws InterruptedException {
//		while(true) {
//			checkInRange();
//			Thread.sleep(5000);
//		}
//	}
    
    public static boolean checkInRange() {
		try {
			String hr = "14:08".substring(0,2);
		    String mm = "14:08".substring(3,5);
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR, Integer.parseInt(hr));
			cal.set(Calendar.MINUTE, Integer.parseInt(mm));
			//cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Calendar calCurrent = Calendar.getInstance();
			
			//calCurrent.set(Calendar.SECOND, 0);
			calCurrent.set(Calendar.MILLISECOND, 0);
			
			
			
			Date newDt = new Date();
			SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
			Date today = sdformat.parse(sdformat.format(newDt));

			Date startDt = new Date();
			startDt = sdformat.parse(sdformat.format(startDt));
			if(startDt.compareTo(today) == 0) {
				
				if(cal.get(Calendar.HOUR) == calCurrent.get(Calendar.HOUR)
						&& cal.get(Calendar.MINUTE) == calCurrent.get(Calendar.MINUTE)
						&& cal.get(Calendar.SECOND)-10 == calCurrent.get(Calendar.SECOND)) {
					System.out.println("Time matched for "+cal+"  ---   "+calCurrent.getTime());
					return true;
				} else 
					System.out.println("Time NOT matched for "+cal.getTime()+"  ---   "+calCurrent.getTime());
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
