package com.hictech.test.wildfly10;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

//Example link: ws://54.111.235.87:8080/GameServer/clients
@Stateless
@ServerEndpoint(value = "/clients")
public class ClientMessageServerEndPoint {
	
	private static Logger logger = Logger.getLogger(ClientMessageServerEndPoint.class);
	@Resource(lookup = "java:global/hcloud/TestService")
	private TestService service;
	private static Set<Session> newSessionsSet = Collections.synchronizedSet(new HashSet<Session>());
	
	
	@OnOpen
	public void onOpen(Session session) {
		logger.info("Openning websocket session: " + session.getId());
		newSessionsSet.add(session);
	}
	
	@OnMessage
	public void onMessage(Session session, String message) {
		logger.info("WebSocket received message on session: " + session.getId() + ", message: " + message);
		invokeOnBean(session, message);
	}
	
	@OnClose
	public void onClose(Session session) {
		logger.info("Closing websocket session: " + session.getId());
		newSessionsSet.remove(session);
	}
	
	@OnError
	public void error(Session session, Throwable t) {
		logger.error("Error occured on websocket session: " + session.getId() + ", error: " + t.getMessage());
		newSessionsSet.remove(session);
	}
	
	public static void sendMessageToClient(String message) {
		for (Session session : newSessionsSet) {
			session.getAsyncRemote().sendText(message);
		}
	}
	
	public void invokeOnBean(Session session, String user) {
		logger.info("Started for " + user);
		
		if(service == null ) {
			logger.error("The cacheHelper id NULL");
			return;
		}
		
		try {
			Integer myVal = service.test();
			System.out.println("Received greeting: " + myVal);
			session.getAsyncRemote().sendText(myVal + "");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(), e);
		}
		
	}

	
}