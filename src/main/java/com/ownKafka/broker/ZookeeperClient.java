package com.ownKafka.broker;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ZookeeperClient implements Watcher {

    private static final Logger LOGGER = Logger.getLogger(ZookeeperClient.class.getName());

    private ZooKeeper zooKeeper;
    private CountDownLatch connectedSignal = new CountDownLatch(1);

    private final String connectString;
    private final int SESSION_TIMEOUT = 5000;

    public ZookeeperClient(String connectString) {
        this.connectString = connectString;
    }

    public void connect() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(connectString, SESSION_TIMEOUT, this);
        connectedSignal.await();

        try {
            createPersistentNode("/brokers", "");
            createPersistentNode("/topics", "");
            createPersistentNode("/controller", "");
        } catch (KeeperException e) {
            LOGGER.warning("Main folder already existing or it's a error: " + e.getMessage());
        }
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
            connectedSignal.countDown();
            LOGGER.info("Connected to ZooKeeper");
        }

        else if (event.getState() == Watcher.Event.KeeperState.Disconnected) {
            LOGGER.warning("Disconnected from ZooKeeper");
        }

        else if (event.getState() == Watcher.Event.KeeperState.Expired) {
            LOGGER.warning("ZooKeeper session expired, reconnecting...");
            try {

                if (zooKeeper != null) {
                    zooKeeper.close();
                }

                connectedSignal = new CountDownLatch(1);

                zooKeeper = new ZooKeeper(connectString, SESSION_TIMEOUT, this);

                connectedSignal.await();

                LOGGER.info("Reconnected to ZooKeeper after session expiry");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to reconnect to ZooKeeper", e);
            }
        }
    }

    public void createPersistentNode(String path, String data) throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(path, false);
        if (stat == null) {
            zooKeeper.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            LOGGER.info("Created persistent node: " + path);
        } else {
            zooKeeper.setData(path, data.getBytes(), -1);
            LOGGER.info("Updated persistent node: " + path);
        }
    }

    public boolean createEphemeralNode(String path, String data) throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(path, false);

        if (stat == null) {
            zooKeeper.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            LOGGER.info("Created ephemeral node: " + path);
            return true;
        } else {
            LOGGER.info("Ephemeral node already exists: " + path);
            return false;
        }
    }

    public interface ChildrenCallback {
        void onChildrenChanged(List<String> children);
    }

    public interface NodeCallback {
        void onNodeChanged();
    }

    public void watchChildren(String path, ChildrenCallback callback) {
        try {
            List<String> children = zooKeeper.getChildren(path, event -> {
                if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                    try {
                        List<String> newChildren = zooKeeper.getChildren(path, event2 -> {
                            if (event2.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                                watchChildren(path, callback);
                            }
                        });

                        callback.onChildrenChanged(newChildren);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error processing children changed event", e);
                    }
                }
            });

            callback.onChildrenChanged(children);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to watch children for path: " + path, e);
        }
    }

    public void watchNode(String path, NodeCallback callback) {
        try {
            zooKeeper.exists(path, event -> {
                if (event.getType() == Watcher.Event.EventType.NodeDeleted ||
                        event.getType() == Watcher.Event.EventType.NodeDataChanged ||
                        event.getType() == Watcher.Event.EventType.NodeCreated) {

                    callback.onNodeChanged();
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to watch node: " + path, e);
        }
    }
}