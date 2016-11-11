package org.briarproject.android.sharing;

import org.briarproject.android.contactselection.ContactSelectorControllerImpl;
import org.briarproject.android.contactselection.SelectableContactItem;
import org.briarproject.android.controller.handler.ExceptionHandler;
import org.briarproject.api.blogs.BlogSharingManager;
import org.briarproject.api.contact.Contact;
import org.briarproject.api.contact.ContactId;
import org.briarproject.api.contact.ContactManager;
import org.briarproject.api.db.DatabaseExecutor;
import org.briarproject.api.db.DbException;
import org.briarproject.api.db.NoSuchContactException;
import org.briarproject.api.db.NoSuchGroupException;
import org.briarproject.api.lifecycle.LifecycleManager;
import org.briarproject.api.nullsafety.NotNullByDefault;
import org.briarproject.api.sync.GroupId;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static java.util.logging.Level.WARNING;

@Immutable
@NotNullByDefault
public class ShareBlogControllerImpl
		extends ContactSelectorControllerImpl<SelectableContactItem>
		implements ShareBlogController {

	private final static Logger LOG =
			Logger.getLogger(ShareBlogControllerImpl.class.getName());

	private final BlogSharingManager blogSharingManager;

	@Inject
	public ShareBlogControllerImpl(
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			ContactManager contactManager,
			BlogSharingManager blogSharingManager) {
		super(dbExecutor, lifecycleManager, contactManager);
		this.blogSharingManager = blogSharingManager;
	}

	@Override
	protected boolean isSelected(Contact c, boolean wasSelected)
			throws DbException {
		return wasSelected;
	}

	@Override
	protected boolean isDisabled(GroupId g, Contact c) throws DbException {
		return !blogSharingManager.canBeShared(g, c);
	}

	@Override
	protected SelectableContactItem getItem(Contact c, boolean selected,
			boolean disabled) {
		return new SelectableContactItem(c, selected, disabled);
	}

	@Override
	public void share(final GroupId g, final Collection<ContactId> contacts,
			final String msg,
			final ExceptionHandler<DbException> handler) {
		runOnDbThread(new Runnable() {
			@Override
			public void run() {
				try {
					for (ContactId c : contacts) {
						try {
							blogSharingManager.sendInvitation(g, c, msg);
						} catch (NoSuchContactException | NoSuchGroupException e) {
							if (LOG.isLoggable(WARNING))
								LOG.log(WARNING, e.toString(), e);
						}
					}
				} catch (DbException e) {
					if (LOG.isLoggable(WARNING))
						LOG.log(WARNING, e.toString(), e);
					handler.onException(e);
				}
			}
		});
	}

}
