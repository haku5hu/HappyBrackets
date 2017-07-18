/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.core;

import de.sciss.net.*;

import java.io.IOException;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages broadcast communication - i.e., one to all comms. You would mainly use it to send broadcast messages and listen to broadcast messages. However, these functions are wrapped in the {@link net.happybrackets.device.HB} class and should be accessed there.
 *
 * Created by ollie on 1/06/2016.
 *  Multiple interface behaviour add by Sam Gillespie on 11/08/2016.
 */
public class BroadcastManager {

    final static Logger logger = LoggerFactory.getLogger(BroadcastManager.class);

    String                                        address;
    int                                           port;
    List<NetworkInterfacePair<OSCTransmitter>>    transmitters;
    List<NetworkInterfacePair<OSCReceiver>>       receivers;
    List<OSCListener>                             listeners;
    List<OnListener>                              interfaceListeners; //listeners who care what interface the message arrived at.
    List<NetworkInterface>                        netInterfaces;

    /**
     * Create a new BroadcastManager.
     *
     * @param address should be a multicast address.
     */
    public BroadcastManager(String address, int port) {
        this.address = address;
        this.port = port;
        initBroadcaster(address, port);
        //automatically refresh the broadcaster every second
    }

    public Thread startRefreshThread() {
        logger.debug("creating broadcast refresh thread...");
        Thread t = new Thread() {
            public void run() {
                while(true) {
                    logger.debug("refresh loop...");
                    refreshBroadcaster();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        logger.error("Broadcast manager poll interval interrupted!", e);
                    }
                }
            }
        };
        t.start();
        return t;
    }

    /**
     * Initialises this BroadcastManager instance.
     */
    private void initBroadcaster(String address, int port) {
        listeners               = new ArrayList<>();
        interfaceListeners      = new ArrayList<>();
        receivers               = new ArrayList<>();
        transmitters            = new ArrayList<>();
        netInterfaces           = new ArrayList<>();
        //  Defer the fiding of interfaces to the refresh cycle so that we don't block the thread loading the BroadcastManager
//        netInterfaces           = Device.viableInterfaces();
//        netInterfaces.forEach( ni -> {
//            try {
//                InetAddress group = InetAddress.getByName(address);
//                //set up a listener and receiver for our broadcast address on this interface
//                DatagramChannel dc = null;
//                // Try creating IPv6 channel first.
//                try {
//                    dc = DatagramChannel.open(StandardProtocolFamily.INET6)
//                            .setOption(StandardSocketOptions.SO_REUSEADDR, true)
//                            .bind(new InetSocketAddress(port))
//                            .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
//                    dc.join(group, ni);
//                }
//                catch (Exception ex) {
//                    // If creating IPv6 channel doesn't work try IPv4.
//                    dc = DatagramChannel.open(StandardProtocolFamily.INET)
//                            .setOption(StandardSocketOptions.SO_REUSEADDR, true)
//                            .bind(new InetSocketAddress(port))
//                            .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
//                    if(dc != null) {
//                        dc.join(group, ni);
//                    }
//                }
//                if (dc != null) {
//                    //add receivers
//                    OSCReceiver receiver = OSCReceiver.newUsing(dc);
//                    receiver.startListening();
//                    receiver.addOSCListener(new MessageAggregator(ni));
//                    receivers.add(new NetworkInterfacePair<OSCReceiver>(ni, receiver));
//                    // add transmitters
//                    OSCTransmitter transmitter = OSCTransmitter.newUsing(dc);
//                    transmitter.setTarget(new InetSocketAddress(group.getHostAddress(), port));
//                    transmitters.add(new NetworkInterfacePair<OSCTransmitter>(ni, transmitter));
//                    logger.debug("Broadcasting on interface: {}", ni.getName());
//                }
//            } catch (IOException e) {
//                logger.error("BroadcastManager encountered an IO exception when creating a listener socket on interface {}! This interface will not be used.", ni.getName());
//            }
//        });
    }

    /**
     * Calls dispose on all receivers (OSCReceiver) and transmitters (OSCTransmitter).
     */
    public void dispose() {
//        These calls take an unusually long time and may not be necessary? Hammering the tests.
        receivers.forEach(r -> r.value.dispose());
        transmitters.forEach(t -> t.value.dispose());
    }

    /**
     * Rebuilds this BroadcastManager. Effectively deletes this instance and creates a new BroadcastManager.
     */
    public void refreshBroadcaster() {
//        statefulRefreshHard(listeners, interfaceListeners);
        statefulRefreshSoft(listeners, interfaceListeners);
    }

    /**
     * Rebuilds a listener with the state provided. This is aggressive and does a complete dispose and rebuild.
     * @param listeners
     * @param interfaceListeners
     */
    private void statefulRefreshHard(List<OSCListener> listeners, List<OnListener> interfaceListeners) {
        // cleanup
        dispose();
        // rebuild
        initBroadcaster(address, port);
        listeners.forEach(l -> addBroadcastListener(l));            //Re add the old listeners to the new lists created by initBroadcaster
        interfaceListeners.forEach(l -> addOnMessage(l));           //
    }

    String wifiIPaddress;

    /**
     * Rebuilds a listener with the state provided. This is gentle and only rebuilds what needs to be rebuilt.
     * @param listeners
     * @param interfaceListeners
     */
    private void statefulRefreshSoft(List<OSCListener> listeners, List<OnListener> interfaceListeners) {
        //Soft refresh - only fix connections that are broken.
        //iterate through existing network interfaces, destroy as required
        List<NetworkInterface> toRemove = new ArrayList<>();
        netInterfaces.forEach( ni -> {
            String name = ni.getName();
            if(!Device.isViableNetworkInterface(ni)) {
                toRemove.add(ni);
                logger.debug("The network interface " + ni + " is no longer valid! Removing it!");
                if(name.equals("en0")) {
                    wifiIPaddress = null;
                }
            }
        });
        //we now have a to-remove list
        //clean up the removes
        receivers.forEach(r -> {
            if(toRemove.contains(r.networkInterface)) {
                r.value.dispose();
            }
        });
        transmitters.forEach(t -> {
            if(toRemove.contains(t.networkInterface)) {
                t.value.dispose();
            }
        });
        netInterfaces.removeAll(toRemove);
        //iterate through the viable interfaces to see if new interfaces have become viable.
        List<NetworkInterface> tempInterfaces = Device.viableInterfaces();
        tempInterfaces.forEach(newInterface -> {
            boolean[] exists = new boolean[] {false};
            netInterfaces.forEach(existingInterface -> {
                if(newInterface.getName().equals(existingInterface.getName())) {
                    exists[0] = true;
                }
            });
            if(!exists[0]) {
                logger.debug("The network interface " + newInterface + " has become valid! Adding it!");
                try {
                    InetAddress group = InetAddress.getByName(address);
                    //set up a listener and receiver for our broadcast address on this interface
                    DatagramChannel dc = null;
                    // Try creating IPv6 channel first.
                    try {
                        dc = DatagramChannel.open(StandardProtocolFamily.INET6)
                                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                                .bind(new InetSocketAddress(port))
                                .setOption(StandardSocketOptions.IP_MULTICAST_IF, newInterface);
                        dc.join(group, newInterface);
                    }

                    catch (Exception ex) {
                        // If creating IPv6 channel doesn't work try IPv4.
                        logger.debug("IPv6 failed, falling back to IPv4 for interface {}", newInterface.getName());
                        dc = DatagramChannel.open(StandardProtocolFamily.INET)
                                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                                .bind(new InetSocketAddress(port))
                                .setOption(StandardSocketOptions.IP_MULTICAST_IF, newInterface);
                        if(dc != null) {
                            dc.join(group, newInterface);
                        }
                    }

                    if (dc != null) {
                        //add receivers
                        OSCReceiver receiver = OSCReceiver.newUsing(dc);
                        receiver.startListening();
                        receiver.addOSCListener(new MessageAggregator(newInterface));
                        receivers.add(new NetworkInterfacePair<OSCReceiver>(newInterface, receiver));
                        // add transmitters
                        OSCTransmitter transmitter = OSCTransmitter.newUsing(dc);
                        transmitter.setTarget(new InetSocketAddress(group.getHostAddress(), port));
                        transmitters.add(new NetworkInterfacePair<OSCTransmitter>(newInterface, transmitter));

                        netInterfaces.add(newInterface);
                        logger.debug("Broadcasting on interface: {}", newInterface.getName());
                    }

                } catch (IOException e) {
                    logger.warn("BroadcastManager encountered an IO exception when creating a listener socket on interface {}! Trying again next refresh.", newInterface.getName());
                    logger.debug("Stacktrace:", e);
                }
            }
        });
    }

    /**
     * Broadcast an {@link OSCMessage} msg over the multicast group.
     *
     * @param name the message string to send.
     * @param args the args to the message.
     */
    public void broadcast(String name, Object... args) {
        OSCMessage msg = new OSCMessage(name, args);
        transmitters.stream().map(pair -> pair.value).forEach( transmitter -> {
          try {
              transmitter.send(msg);
          } catch (IOException e) {
              logger.warn("Removing broadcaster interface due to error:", e);
              transmitters.stream().filter(t -> transmitter.equals((OSCTransmitter) t.value)).forEach(match -> {
                  netInterfaces.remove(match.networkInterface);
                  transmitters.remove(match);
                  match.value.dispose();
              });
//              transmitters.remove(transmitter);
//              netInterfaces.remove(transmitter)
//              transmitter.dispose();
          }
        });
    }

    /**
     * Execute onTransmitter.cb(NetworkInterface, OSCTransmitter) for all transmitters.
     * @param onTransmitter
     */
    public void forAllTransmitters(OnTransmitter onTransmitter) {
        transmitters.forEach(pair -> {
            try {
                onTransmitter.cb(pair.networkInterface, pair.value);
            } catch (Exception e) {
                logger.error("Error executing call back on transmitter for interface {}, removing interface", pair.networkInterface.getDisplayName(), e);

                netInterfaces.remove(pair.networkInterface);
                transmitters.remove(pair);
                pair.value.dispose();
            }
        });
    }

    /**
     * Add a new {@link OSCListener}.
     *
     * @param bl the new {@link OSCListener}.
     */
    public void addBroadcastListener(OSCListener bl) {
        listeners.add(bl);
        // receiver.addOSCListener(bl);
    }

    /**
     * Add a new interface aware listener
     */
    public void addOnMessage(OnListener onListener) {
        interfaceListeners.add(onListener);
    }

    /**
     * Remove the given {@link OSCListener}.
     *
     * @param bl the {@link OSCListener} to remove.
     */
    public void removeBroadcastListener(OSCListener bl) {
        listeners.remove(bl);
        // receiver.removeOSCListener(bl);
    }

    /**
     * Clear all {@link OSCListener}s.
     */
    public void clearBroadcastListeners() {
        // for(OSCListener listener : listeners) {
        //     receiver.removeOSCListener(listener);
        // }
        listeners.clear();
    }

    private class NetworkInterfacePair<T> {
        T value;
        NetworkInterface networkInterface;

        public NetworkInterfacePair(NetworkInterface networkInterface, T value) {
            this.value = value;
            this.networkInterface = networkInterface;
        }
    }

    /**
     * An OSCListener for aggregating various broadcast streams for out listeners
     * Enable trace level logging for detailed OSC events
     */
    private class MessageAggregator implements OSCListener {
        NetworkInterface networkInterface;

        public MessageAggregator(NetworkInterface networkInterface) {
            this.networkInterface = networkInterface;
        }

        public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
            if (logger.isTraceEnabled()) {
                String args = " ";
                for(int i = 0; i < msg.getArgCount(); i++) {
                    args += msg.getArg(i) + " ";
                }
                logger.trace("Received broadcast message {} with {} args [{}] from {}", new Object[]{msg.getName(), msg.getArgCount(), args, sender.toString()});
            }
            listeners.forEach(l -> l.messageReceived(msg, sender, time));
            interfaceListeners.forEach(l -> l.cb(networkInterface, msg, sender, time));
        }
    }

    /**
     * Call back interface for sending to all interfaces where the specific interface matters
     */
    public interface OnTransmitter {
        void cb(NetworkInterface ni, OSCTransmitter transmitter) throws IOException;
    }

    /**
     * Call back interface for listening to all interfaces where the specific interface matters
     */
    public interface OnListener {
        void cb(NetworkInterface ni, OSCMessage msg, SocketAddress sender, long time);
    }

}
