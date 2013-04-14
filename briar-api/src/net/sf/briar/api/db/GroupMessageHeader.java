package net.sf.briar.api.db;

import net.sf.briar.api.Author;
import net.sf.briar.api.messaging.GroupId;
import net.sf.briar.api.messaging.MessageId;
import net.sf.briar.api.messaging.Rating;

public class GroupMessageHeader extends MessageHeader {

	private final GroupId groupId;

	public GroupMessageHeader(MessageId id, MessageId parent, Author author,
			String contentType, String subject, long timestamp, boolean read,
			boolean starred, Rating rating, GroupId groupId) {
		super(id, parent, author, contentType, subject, timestamp, read,
				starred, rating);
		this.groupId = groupId;
	}

	/** Returns the ID of the group to which the message belongs. */
	public GroupId getGroupId() {
		return groupId;
	}
}
