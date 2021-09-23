import { Component, Input, OnInit } from '@angular/core';
import { Digest } from '../digests/digest.model';
import { EntryFilterListComponent } from './entry-filter-list.component';
import { EntryFilterList } from './entry-filter-list.model';
import { Observable } from "rxjs/Rx";

@Component({
	selector: 'digest-entry-filter-list',
	templateUrl: './entry-filter-list.component.html',
	styleUrls: ['./entry-filter-list.component.css']
})
export class DigestEntryFilterListComponent extends EntryFilterListComponent implements OnInit {

	@Input()
	digest: Digest;

	ngOnInit() {
		super.ngOnInit();
	}

	protected loadFilters(): Observable<EntryFilterList> {
		return this.entryFilterService.getDigestEntryFilters(this.digest.id);
	}

	protected saveFilters(entryFilterList: EntryFilterList): Observable<EntryFilterList> {
		return this.entryFilterService.updateDigestEntryFilters(this.digest.id, entryFilterList);
	}

}
