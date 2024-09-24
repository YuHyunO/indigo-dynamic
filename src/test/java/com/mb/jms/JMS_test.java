package com.mb.jms;

import com.indigo.indigomq.IndigoMQConnectionFactory;
import com.indigo.indigomq.pool.PooledConnectionFactory;
import com.indigo.indigomq.transport.discovery.DiscoveryTransportFactory;
import com.indigo.indigomq.transport.failover.FailoverTransportFactory;
import com.indigo.indigomq.transport.fanout.FanoutTransportFactory;
import com.indigo.indigomq.transport.mock.MockTransportFactory;
import com.indigo.indigomq.transport.multicast.MulticastTransportFactory;
import com.indigo.indigomq.transport.nio.NIOTransportFactory;
import com.indigo.indigomq.transport.peer.PeerTransportFactory;
import com.indigo.indigomq.transport.stomp.StompNIOTransportFactory;
import com.indigo.indigomq.transport.stomp.StompSslTransportFactory;
import com.indigo.indigomq.transport.stomp.StompTransportFactory;
import com.indigo.indigomq.transport.tcp.SslTransportFactory;
import com.indigo.indigomq.transport.tcp.TcpTransport;
import com.indigo.indigomq.transport.tcp.TcpTransportFactory;
import com.indigo.indigomq.transport.udp.UdpTransportFactory;
import com.indigo.indigomq.transport.vm.VMTransportFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class JMS_test {

    @Test
    public void esb_transport() {
        PooledConnectionFactory cp = new PooledConnectionFactory();
        IndigoMQConnectionFactory icf = new IndigoMQConnectionFactory();

        FailoverTransportFactory ftf = new FailoverTransportFactory(); //FailoverTransport#timeout -> failover
        DiscoveryTransportFactory dtf = new DiscoveryTransportFactory(); //DiscoveryTransport
        FanoutTransportFactory ftf2 = new FanoutTransportFactory(); //FanoutTransport
        MockTransportFactory mtf = new MockTransportFactory(); //MockTransport
        MulticastTransportFactory mtf2 = new MulticastTransportFactory(); //MulticastTransport//UdpTransport
        NIOTransportFactory niotf = new NIOTransportFactory(); //NIOTransport//TcpTransport#connectionTimeout ->nio
        PeerTransportFactory ptf = new PeerTransportFactory(); //?
        SslTransportFactory ssf = new SslTransportFactory(); //SslTransport//TcpTransport#connectionTimeout -> ssl
        StompTransportFactory stf = new StompTransportFactory(); //?
        StompNIOTransportFactory stniof = new StompNIOTransportFactory(); //StompNIOTransport//TcpTransport#connectionTimeout -> stomp+nio
        StompSslTransportFactory stssf = new StompSslTransportFactory(); //?
        TcpTransportFactory tcpft = new TcpTransportFactory();//TcpTransport#connectionTimeout -> tcp
        UdpTransportFactory udptf = new UdpTransportFactory();//UdpTransport
        VMTransportFactory vmf = new VMTransportFactory();//?

        TcpTransport tcpo = null;

    }

    @Test
    public void jms_dependency_check() {
        
    }

}
