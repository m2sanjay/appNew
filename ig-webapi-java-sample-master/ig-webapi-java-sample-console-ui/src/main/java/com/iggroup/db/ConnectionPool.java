package com.iggroup.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;

public class ConnectionPool implements Runnable {
    private int maxConnections;
    private int initialConnections;
    private boolean waitIfBusy;
    public Vector<Connection> availableConnections, busyConnections;
    private boolean connectionPending = false;
    
    
    
    public ConnectionPool() throws SQLException {
           this.maxConnections = 10;
           this.initialConnections = 5;
           this.waitIfBusy = true;
           if (initialConnections > maxConnections) {
                  initialConnections = maxConnections;
           }
           availableConnections = new Vector<Connection>(initialConnections);
           busyConnections = new Vector<Connection>();
           for (int i = 0; i < initialConnections; i++) {
                  availableConnections.addElement(makeNewConnection());
           }
    } 
 
 
    /**
     * Method to return Connections
     */
    public synchronized Connection getConnection() throws SQLException {
           if (!availableConnections.isEmpty()) {
                  Connection existingConnection = (Connection) availableConnections
                               .lastElement();
                  int lastIndex = availableConnections.size() - 1;
                  availableConnections.removeElementAt(lastIndex);
                  // If connection on available list is closed (e.g.,
                  // it timed out), then remove it from available list
                  // and repeat the process of obtaining a connection.
                  // Also wake up threads that were waiting for a
                  // connection because maxConnection limit was reached.
                  if (existingConnection.isClosed()) {
                        notifyAll(); // Freed up a spot for anybody waiting
                        return (getConnection());
                  } else {
                        busyConnections.addElement(existingConnection);
                        return (existingConnection);
                  }
           } else {
 
                  // Three possible cases:
                  // 1) You haven't reached maxConnections limit. So
                  // establish one in the background if there isn't
                  // already one pending, then wait for
                  // the next available connection (whether or not
                  // it was the newly established one).
                  // 2) You reached maxConnections limit and waitIfBusy
                  // flag is false. Throw SQLException in such a case.
                  // 3) You reached maxConnections limit and waitIfBusy
                  // flag is true. Then do the same thing as in second
                  // part of step 1: wait for next available connection.
 
                  if ((totalConnections() < maxConnections) && !connectionPending) {
                        makeBackgroundConnection();
                  } else if (!waitIfBusy) {
                        throw new SQLException("Connection limit reached");
               }
                  // Wait for either a new connection to be established
                  // (if you called makeBackgroundConnection) or for
                  // an existing connection to be freed up.
                  try {
                        wait();
                  } catch (InterruptedException ie) {
                  }
                  // Someone freed up a connection, so try again.
                  return (getConnection());
           }
    }
 
    /**
     * You can't just make a new connection in the foreground
    // when none are available, since this can take several
    // seconds with a slow network connection. Instead,
    // start a thread that establishes a new connection,
    // then wait. You get woken up either when the new connection
    // is established or if someone finishes with an existing
    // connection.
     */
    private void makeBackgroundConnection() {
           connectionPending = true;
           try {
                  Thread connectThread = new Thread(this);
                  connectThread.start();
           } catch (OutOfMemoryError oome) {
                  // Give up on new connection
           }
    }
 
    public void run() {
           try {
                  Connection connection = makeNewConnection();
                  synchronized (this) {
                        availableConnections.addElement(connection);
                        connectionPending = false;
                        notifyAll();
                  }
           } catch (Exception e) { // SQLException or OutOfMemory
                  // Give up on new connection and wait for existing one
                  // to free up.
                  e.printStackTrace();
           }
    }
    
    
    /**
           // Method explicitly makes a new connection. Called in
           // the foreground when initializing the ConnectionPool,
           // and called in the background when running.
    */
    private Connection makeNewConnection() throws SQLException {
    	try {
    		//Ole
    		String PUBLIC_DNS = "igindexdb1.chj0tphs0joi.eu-west-1.rds.amazonaws.com";
    		//Ambuj
    		//String PUBLIC_DNS = "igindexdb1.cfjohbne9yzq.eu-west-2.rds.amazonaws.com";
    		String PORT = "3306";
    		String DATABASE = "igindexdb1";
    		String REMOTE_DATABASE_USERNAME = "igindexu";
    		String DATABASE_USER_PASSWORD = "igindexp";
    		Class.forName("com.mysql.cj.jdbc.Driver"); 
    		Connection connection = DriverManager.getConnection("jdbc:mysql://" + PUBLIC_DNS + ":" + PORT + "/" + DATABASE, REMOTE_DATABASE_USERNAME, DATABASE_USER_PASSWORD);

    		return (connection);
    	} catch (Exception cnfe) {
    		// Simplify try/catch blocks of people using this by
    		// throwing only one exception type.
    		cnfe.printStackTrace();
    		throw new SQLException(
    				"ConnectionPool:: SQLException encountered:: "
    						+ cnfe.getMessage());
    	}
    }

    public synchronized void free(Connection connection) {
           busyConnections.removeElement(connection);
           availableConnections.addElement(connection);
           // Wake up threads that are waiting for a connection
           notifyAll();
    }
 
    public synchronized int totalConnections() {
           return (availableConnections.size() + busyConnections.size());
    }
 
    
    /**
     * Method to Close all the connections. Use with caution: be sure no connections are
     * in use before calling. Note that you are not <I>required</I> to call this
     * when done with a ConnectionPool, since connections are guaranteed to be
     * closed when garbage collected. But this method gives more control
     * regarding when the connections are closed.
     */
 
    public synchronized void closeAllConnections() {
           closeConnections(availableConnections);
           availableConnections = new Vector<Connection>();
           closeConnections(busyConnections);
           busyConnections = new Vector<Connection>();
    }
 
    private void closeConnections(Vector<Connection> connections) {
           try {
                  for (int i = 0; i < connections.size(); i++) {
                        Connection connection = (Connection) connections.elementAt(i);
                        if (!connection.isClosed()) {
                               connection.close();
                        }
               }
           } catch (SQLException sqle) {
                  sqle.printStackTrace();
                  // Ignore errors; garbage collect anyhow
           }
    }
 
    /**
     * String form of ConnectionPool class.
     */
    public synchronized String toString() {
           String info = "ConnectionPool(" 
                        + ", available=" + availableConnections.size() + ", busy="
                        + busyConnections.size() + ", max=" + maxConnections;
           return (info);
    }
}
