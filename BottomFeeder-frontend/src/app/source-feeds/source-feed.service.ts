import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { NewSourceFeed, SourceFeed } from './source-feed.model';

@Injectable({
	providedIn: 'root'
})
export class SourceFeedService {
	private baseURL = `${environment.apiURL}/feeds`;


	constructor(private http: HttpClient) { }

	getDigestSourceFeeds(digestId: number) {
		return this.http.get<SourceFeed[]>(`${this.baseURL}/digest/${digestId}`);
	}

	getSourceFeed(id: number) {
		return this.http.get<SourceFeed>(`${this.baseURL}/${id}`);
	}

	createSourceFeed(newSourceFeed: NewSourceFeed) {
		return this.http.post<SourceFeed>(this.baseURL, newSourceFeed);
	}

	updateSourceFeed(sourceFeed: SourceFeed) {
		return this.http.put<SourceFeed>(this.baseURL, sourceFeed);
	}

	deleteSourceFeed(id: number) {
		return this.http.delete<void>(`${this.baseURL}/${id}`);
	}

}
