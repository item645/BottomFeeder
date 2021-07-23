import { Component, Input, OnInit } from '@angular/core';
import { Digest } from '../digests/digest.model';
import { SourceFeed } from './source-feed.model';
import { SourceFeedService } from './source-feed.service';

@Component({
	selector: 'source-feed-list',
	templateUrl: './source-feed-list.component.html',
	styleUrls: ['./source-feed-list.component.css']
})
export class SourceFeedListComponent implements OnInit {

	@Input()
	digest: Digest;

	sourceFeeds: SourceFeed[] = [];

	constructor(private sourceFeedService: SourceFeedService) { }

	ngOnInit() {
		this.sourceFeedService
			.getDigestSourceFeeds(this.digest.id)
			.subscribe(sourceFeeds => this.sourceFeeds = sourceFeeds);
	}

	deleteSourceFeed(id: number) {
		this.sourceFeedService
			.deleteSourceFeed(id)
			.subscribe(() => this.sourceFeeds = this.sourceFeeds.filter(feed => feed.id !== id));
	}
}
