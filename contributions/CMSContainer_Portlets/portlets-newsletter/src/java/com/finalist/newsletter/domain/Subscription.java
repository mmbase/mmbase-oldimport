package com.finalist.newsletter.domain;

import java.util.HashSet;
import java.util.Set;

import com.finalist.cmsc.services.community.person.Person;

public class Subscription {

	private Person subscriber;

	private String mimeType;
	private STATUS status = STATUS.INACTIVE;

	private Set<Tag> tags = new HashSet<Tag>();
	private Newsletter newsletter;

	private int id;
	
	 public enum STATUS {
	      ACTIVE, PAUSED, INACTIVE
	   }

	public Person getSubscriber() {
		return subscriber;
	}

	public void setSubscriber(Person subscriber) {
		this.subscriber = subscriber;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Newsletter getNewsletter() {
		return this.newsletter;
	}


	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	

	public void setNewsletter(Newsletter newsletter) {
		this.newsletter = newsletter;
	}

	public STATUS getStatus() {
		return status;
	}

	public void setStatus(STATUS status) {
		this.status = status;
	}
	
	
}
