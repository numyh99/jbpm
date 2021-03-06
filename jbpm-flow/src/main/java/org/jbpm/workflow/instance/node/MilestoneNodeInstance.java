/**
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.workflow.instance.node;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.kie.api.event.rule.MatchCancelledEvent;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.AgendaGroupPoppedEvent;
import org.kie.api.event.rule.AgendaGroupPushedEvent;
import org.kie.api.event.rule.BeforeMatchFiredEvent;
import org.kie.api.event.rule.RuleFlowGroupActivatedEvent;
import org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent;
import org.drools.core.rule.Rule;
import org.kie.api.runtime.process.NodeInstance;
import org.drools.core.runtime.rule.impl.InternalAgenda;
import org.drools.core.spi.Activation;
import org.jbpm.workflow.core.node.MilestoneNode;

/**
 * Runtime counterpart of a milestone node.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class MilestoneNodeInstance extends StateBasedNodeInstance implements AgendaEventListener {

    private static final long serialVersionUID = 510l;

    protected MilestoneNode getMilestoneNode() {
        return (MilestoneNode) getNode();
    }

    public void internalTrigger(final NodeInstance from, String type) {
    	super.internalTrigger(from, type);
    	// if node instance was cancelled, abort
		if (getNodeInstanceContainer().getNodeInstance(getId()) == null) {
			return;
		}
		if (!org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE.equals(type)) {
            throw new IllegalArgumentException(
                "A MilestoneNode only accepts default incoming connections!");
        }
        String rule = "RuleFlow-Milestone-" + getProcessInstance().getProcessId()
        	+ "-" + getMilestoneNode().getUniqueId();
        boolean isActive = ((InternalAgenda) getProcessInstance().getKnowledgeRuntime().getAgenda())
			.isRuleActiveInRuleFlowGroup("DROOLS_SYSTEM", rule, getProcessInstance().getId());
        if (isActive) {
        	triggerCompleted();
        } else {
            addActivationListener();
        }
    }
    
    public void addEventListeners() {
        super.addEventListeners();
        addActivationListener();
    }
    
    private void addActivationListener() {
    	getProcessInstance().getKnowledgeRuntime().addEventListener(this);
    }

    public void removeEventListeners() {
        super.removeEventListeners();
        getProcessInstance().getKnowledgeRuntime().removeEventListener(this);
    }

    public void matchCreated(MatchCreatedEvent event) {
        // check whether this activation is from the DROOLS_SYSTEM agenda group
        String ruleFlowGroup = ((Rule) event.getMatch().getRule()).getRuleFlowGroup();
        if ("DROOLS_SYSTEM".equals(ruleFlowGroup)) {
            // new activations of the rule associate with a milestone node
            // trigger node instances of that milestone node
            String ruleName = event.getMatch().getRule().getName();
            String milestoneName = "RuleFlow-Milestone-" + getProcessInstance().getProcessId() + "-" + getNodeId();
            if (milestoneName.equals(ruleName) && checkProcessInstance((Activation) event.getMatch())) {
        		if ( !((InternalKnowledgeRuntime) getProcessInstance().getKnowledgeRuntime()).getActionQueue().isEmpty() ) {
        			((InternalKnowledgeRuntime) getProcessInstance().getKnowledgeRuntime()).executeQueuedActions();
                }
            	synchronized(getProcessInstance()) {
	                removeEventListeners();
	                triggerCompleted();
            	}
            }
        }
    }

    public void matchCancelled(MatchCancelledEvent event) {
        // Do nothing
    }

    public void afterMatchFired(AfterMatchFiredEvent event) {
        // Do nothing
    }

    public void agendaGroupPopped(AgendaGroupPoppedEvent event) {
        // Do nothing
    }

    public void agendaGroupPushed(AgendaGroupPushedEvent event) {
        // Do nothing
    }

    public void beforeMatchFired(BeforeMatchFiredEvent event) {
        // Do nothing
    }

	public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
	}

	public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
	}

	public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
	}

	public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
	}

}
