import { Component, Input, OnInit } from '@angular/core';
import { SourceFeed } from '../source-feeds/source-feed.model';
import { EntryFilterListComponent } from './entry-filter-list.component';
import { Observable } from "rxjs/Rx";
import { EntryFilterList } from './entry-filter-list.model';

@Component({
	selector: 'source-feed-entry-filter-list',
	templateUrl: './entry-filter-list.component.html',
	styleUrls: ['./entry-filter-list.component.css']
})
export class SourceFeedEntryFilterListComponent extends EntryFilterListComponent implements OnInit {

	@Input()
	sourceFeed: SourceFeed;

	ngOnInit() {
		super.ngOnInit();
	}

	protected loadFilters(): Observable<EntryFilterList> {
		return this.entryFilterService.getSourceFeedEntryFilters(this.sourceFeed.id);
	}

	protected saveFilters(entryFilterList: EntryFilterList): Observable<EntryFilterList> {
		return this.entryFilterService.updateSourceFeedEntryFilters(this.sourceFeed.id, entryFilterList);
	}
}
