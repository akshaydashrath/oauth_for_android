package com.novoda.oauth.statemachines;

import org.apache.commons.scxml.env.AbstractStateMachine;
import org.apache.commons.scxml.model.SCXML;

public class WebAppOauthStateMachine extends AbstractStateMachine {

	public WebAppOauthStateMachine(SCXML stateMachine) {
		super(WebAppOauthStateMachine.class.getResource("com/novoda/oauth/statemachines/webapp.xml"));
	}

}
