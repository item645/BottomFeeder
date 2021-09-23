import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { EntryFilterList } from './entry-filter-list.model';

@Injectable({
	providedIn: 'root'
})
export class EntryFilterService {
	private baseURL = `${environment.apiURL}/filters`;

	constructor(private http: HttpClient) { }

	getDigestEntryFilters(digestId: number) {
		return this.http.get<EntryFilterList>(`${this.baseURL}/digest/${digestId}`);
	}

	updateDigestEntryFilters(digestId: number, entryFilterList: EntryFilterList) {
		return this.http.post<EntryFilterList>(`${this.baseURL}/digest/${digestId}`, entryFilterList);
	}

	getSourceFeedEntryFilters(sourceFeedId: number) {
		return this.http.get<EntryFilterList>(`${this.baseURL}/feed/${sourceFeedId}`);
	}

	updateSourceFeedEntryFilters(sourceFeedId: number, entryFilterList: EntryFilterList) {
		return this.http.post<EntryFilterList>(`${this.baseURL}/feed/${sourceFeedId}`, entryFilterList);
	}
}
