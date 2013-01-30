package net.sf.briar.api.db;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import net.sf.briar.api.ContactId;
import net.sf.briar.api.Rating;
import net.sf.briar.api.TransportConfig;
import net.sf.briar.api.TransportProperties;
import net.sf.briar.api.db.event.DatabaseListener;
import net.sf.briar.api.messaging.Ack;
import net.sf.briar.api.messaging.AuthorId;
import net.sf.briar.api.messaging.Group;
import net.sf.briar.api.messaging.GroupId;
import net.sf.briar.api.messaging.Message;
import net.sf.briar.api.messaging.MessageId;
import net.sf.briar.api.messaging.Offer;
import net.sf.briar.api.messaging.Request;
import net.sf.briar.api.messaging.RetentionAck;
import net.sf.briar.api.messaging.RetentionUpdate;
import net.sf.briar.api.messaging.SubscriptionAck;
import net.sf.briar.api.messaging.SubscriptionUpdate;
import net.sf.briar.api.messaging.TransportAck;
import net.sf.briar.api.messaging.TransportId;
import net.sf.briar.api.messaging.TransportUpdate;
import net.sf.briar.api.transport.Endpoint;
import net.sf.briar.api.transport.TemporarySecret;

/**
 * Encapsulates the database implementation and exposes high-level operations
 * to other components.
 */
public interface DatabaseComponent {

	/**
	 * Opens the database.
	 * @param resume true to reopen an existing database or false to create a
	 * new one.
	 */
	void open(boolean resume) throws DbException, IOException;

	/** Waits for any open transactions to finish and closes the database. */
	void close() throws DbException, IOException;

	/** Adds a listener to be notified when database events occur. */
	void addListener(DatabaseListener d);

	/** Removes a listener. */
	void removeListener(DatabaseListener d);

	/**
	 * Adds a new contact to the database and returns an ID for the contact.
	 */
	ContactId addContact() throws DbException;

	/** Adds an endpoitn to the database. */
	void addEndpoint(Endpoint ep) throws DbException;

	/** Adds a locally generated group message to the database. */
	void addLocalGroupMessage(Message m) throws DbException;

	/** Adds a locally generated private message to the database. */
	void addLocalPrivateMessage(Message m, ContactId c) throws DbException;

	/**
	 * Stores the given temporary secrets and deletes any secrets that have
	 * been made obsolete.
	 */
	void addSecrets(Collection<TemporarySecret> secrets) throws DbException;

	/** Adds a transport to the database. */
	void addTransport(TransportId t) throws DbException;

	/**
	 * Generates an acknowledgement for the given contact. Returns null if
	 * there are no messages to acknowledge.
	 */
	Ack generateAck(ContactId c, int maxMessages) throws DbException;

	/**
	 * Generates a batch of raw messages for the given contact, with a total
	 * length less than or equal to the given length. Returns null if
	 * there are no sendable messages that fit in the given length.
	 */
	Collection<byte[]> generateBatch(ContactId c, int maxLength)
			throws DbException;

	/**
	 * Generates a batch of raw messages for the given contact from the given
	 * collection of requested messages, with a total length less than or equal
	 * to the given length. Any messages that were either added to the batch,
	 * or were considered but are no longer sendable to the contact, are
	 * removed from the collection of requested messages before returning.
	 * Returns null if there are no sendable messages that fit in the given
	 * length.
	 */
	Collection<byte[]> generateBatch(ContactId c, int maxLength,
			Collection<MessageId> requested) throws DbException;

	/**
	 * Generates an offer for the given contact. Returns null if there are no
	 * messages to offer.
	 */
	Offer generateOffer(ContactId c, int maxMessages) throws DbException;

	/**
	 * Generates a retention ack for the given contact. Returns null if no ack
	 * is due.
	 */
	RetentionAck generateRetentionAck(ContactId c) throws DbException;

	/**
	 * Generates a retention update for the given contact. Returns null if no
	 * update is due.
	 */
	RetentionUpdate generateRetentionUpdate(ContactId c) throws DbException;

	/**
	 * Generates a subscription ack for the given contact. Returns null if no
	 * ack is due.
	 */
	SubscriptionAck generateSubscriptionAck(ContactId c) throws DbException;

	/**
	 * Generates a subscription update for the given contact. Returns null if
	 * no update is due.
	 */
	SubscriptionUpdate generateSubscriptionUpdate(ContactId c)
			throws DbException;

	/**
	 * Generates a batch of transport acks for the given contact. Returns null
	 * if no acks are due.
	 */
	Collection<TransportAck> generateTransportAcks(ContactId c)
			throws DbException;

	/**
	 * Generates a batch of transport updates for the given contact. Returns
	 * null if no updates are due.
	 */
	Collection<TransportUpdate> generateTransportUpdates(ContactId c)
			throws DbException;

	/** Returns the configuration for the given transport. */
	TransportConfig getConfig(TransportId t) throws DbException;

	/** Returns the IDs of all contacts. */
	Collection<ContactId> getContacts() throws DbException;

	/** Returns the local transport properties for the given transport. */
	TransportProperties getLocalProperties(TransportId t) throws DbException;

	/** Returns the headers of all messages in the given group. */
	Collection<MessageHeader> getMessageHeaders(GroupId g) throws DbException;

	/** Returns the user's rating for the given author. */
	Rating getRating(AuthorId a) throws DbException;

	/** Returns all remote transport properties for the given transport. */
	Map<ContactId, TransportProperties> getRemoteProperties(TransportId t)
			throws DbException;

	/** Returns all temporary secrets. */
	Collection<TemporarySecret> getSecrets() throws DbException;

	/** Returns the set of groups to which the user subscribes. */
	Collection<Group> getSubscriptions() throws DbException;

	/** Returns the number of unread messages in each subscribed group. */
	Map<GroupId, Integer> getUnreadMessageCounts() throws DbException;

	/** Returns the contacts to which the given group is visible. */
	Collection<ContactId> getVisibility(GroupId g) throws DbException;

	/** Returns the subscriptions that are visible to the given contact. */
	Collection<GroupId> getVisibleSubscriptions(ContactId c) throws DbException;

	/** Returns true if any messages are sendable to the given contact. */
	boolean hasSendableMessages(ContactId c) throws DbException;

	/**
	 * Increments the outgoing connection counter for the given contact
	 * transport in the given rotation period and returns the old value.
	 */
	long incrementConnectionCounter(ContactId c, TransportId t, long period)
			throws DbException;

	/**
	 * Merges the given configuration with existing configuration for the
	 * given transport.
	 */
	void mergeConfig(TransportId t, TransportConfig c) throws DbException;

	/**
	 * Merges the given properties with the existing local properties for the
	 * given transport.
	 */
	void mergeLocalProperties(TransportId t, TransportProperties p)
			throws DbException;

	/** Processes an ack from the given contact. */
	void receiveAck(ContactId c, Ack a) throws DbException;

	/** Processes a message from the given contact. */
	void receiveMessage(ContactId c, Message m) throws DbException;

	/**
	 * Processes an offer from the given contact and generates a request for
	 * any messages in the offer that the contact should send. To prevent
	 * contacts from using offers to test for subscriptions that are not
	 * visible to them, any messages belonging to groups that are not visible
	 * to the contact are requested just as though they were not present in the
	 * database.
	 */
	Request receiveOffer(ContactId c, Offer o) throws DbException;

	/** Processes a retention ack from the given contact. */
	void receiveRetentionAck(ContactId c, RetentionAck a) throws DbException;

	/** Processes a retention update from the given contact. */
	void receiveRetentionUpdate(ContactId c, RetentionUpdate u)
			throws DbException;

	/** Processes a subscription ack from the given contact. */
	void receiveSubscriptionAck(ContactId c, SubscriptionAck a)
			throws DbException;

	/** Processes a subscription update from the given contact. */
	void receiveSubscriptionUpdate(ContactId c, SubscriptionUpdate u)
			throws DbException;

	/** Processes a transport ack from the given contact. */
	void receiveTransportAck(ContactId c, TransportAck a) throws DbException;

	/** Processes a transport update from the given contact. */
	void receiveTransportUpdate(ContactId c, TransportUpdate u)
			throws DbException;

	/** Removes a contact (and all associated state) from the database. */
	void removeContact(ContactId c) throws DbException;

	/**
	 * Sets the connection reordering window for the given endoint in the given
	 * rotation period.
	 */
	void setConnectionWindow(ContactId c, TransportId t, long period,
			long centre, byte[] bitmap) throws DbException;

	/** Records the user's rating for the given author. */
	void setRating(AuthorId a, Rating r) throws DbException;

	/** Records the given messages as having been seen by the given contact. */
	void setSeen(ContactId c, Collection<MessageId> seen) throws DbException;

	/**
	 * Makes the given group visible to the given set of contacts and invisible
	 * to any other contacts.
	 */
	void setVisibility(GroupId g, Collection<ContactId> visible)
			throws DbException;

	/** Subscribes to the given group. */
	void subscribe(Group g) throws DbException;

	/**
	 * Unsubscribes from the given group. Any messages belonging to the group
	 * are deleted from the database.
	 */
	void unsubscribe(GroupId g) throws DbException;
}
