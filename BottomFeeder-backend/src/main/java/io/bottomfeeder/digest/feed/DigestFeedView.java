package io.bottomfeeder.digest.feed;

import java.io.OutputStreamWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.AbstractView;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedOutput;

/**
 * A view for RSS/Atom-formatted XML document representing digest feed content.
 */
class DigestFeedView extends AbstractView {

	private final SyndFeed digestFeed;

	
	DigestFeedView(DigestFeedFormat digestFeedFormat, SyndFeed digestFeed) {
		this.digestFeed = digestFeed;
		setContentType(digestFeedFormat.contentType());
	}


	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		setResponseContentType(request, response);

		var feedOutput = new SyndFeedOutput();
		var outputStream = response.getOutputStream();
		feedOutput.output(digestFeed, new OutputStreamWriter(outputStream, digestFeed.getEncoding()));
		outputStream.flush();
	}

}
