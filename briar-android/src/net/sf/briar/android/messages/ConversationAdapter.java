package net.sf.briar.android.messages;

import static android.graphics.Typeface.BOLD;
import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;
import static java.text.DateFormat.SHORT;
import static net.sf.briar.android.widgets.CommonLayoutParams.WRAP_WRAP_1;

import java.util.ArrayList;

import net.sf.briar.R;
import net.sf.briar.android.widgets.HorizontalSpace;
import net.sf.briar.api.db.PrivateMessageHeader;
import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

class ConversationAdapter extends ArrayAdapter<PrivateMessageHeader> {

	ConversationAdapter(Context ctx) {
		super(ctx, android.R.layout.simple_expandable_list_item_1,
				new ArrayList<PrivateMessageHeader>());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PrivateMessageHeader item = getItem(position);
		Context ctx = getContext();

		LinearLayout layout = new LinearLayout(ctx);
		layout.setOrientation(HORIZONTAL);
		if(!item.isRead()) {
			Resources res = ctx.getResources();
			layout.setBackgroundColor(res.getColor(R.color.unread_background));
		}

		LinearLayout innerLayout = new LinearLayout(ctx);
		// Give me all the unused width
		innerLayout.setLayoutParams(WRAP_WRAP_1);
		innerLayout.setOrientation(VERTICAL);

		TextView name = new TextView(ctx);
		name.setTextSize(18);
		name.setMaxLines(1);
		name.setPadding(10, 10, 10, 10);
		name.setText(item.getAuthor().getName());
		innerLayout.addView(name);

		if(item.getContentType().equals("text/plain")) {
			TextView subject = new TextView(ctx);
			subject.setTextSize(14);
			subject.setMaxLines(2);
			subject.setPadding(10, 0, 10, 10);
			if(!item.isRead()) subject.setTypeface(null, BOLD);
			String s = item.getSubject();
			subject.setText(s == null ? "" : s);
			innerLayout.addView(subject);
		} else {
			LinearLayout attachmentLayout = new LinearLayout(ctx);
			attachmentLayout.setOrientation(HORIZONTAL);
			ImageView attachment = new ImageView(ctx);
			attachment.setPadding(10, 0, 10, 10);
			attachment.setImageResource(R.drawable.content_attachment);
			attachmentLayout.addView(attachment);
			attachmentLayout.addView(new HorizontalSpace(ctx));
			innerLayout.addView(attachmentLayout);
		}
		layout.addView(innerLayout);

		TextView date = new TextView(ctx);
		date.setTextSize(14);
		date.setPadding(0, 10, 10, 10);
		long then = item.getTimestamp(), now = System.currentTimeMillis();
		date.setText(DateUtils.formatSameDayTime(then, now, SHORT, SHORT));
		layout.addView(date);

		return layout;
	}
}
