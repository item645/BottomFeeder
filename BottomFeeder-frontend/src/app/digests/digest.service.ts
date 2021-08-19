import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { Digest, DigestTitle, NewDigest } from './digest.model';

@Injectable({
	providedIn: 'root'
})
export class DigestService {
	private baseURL = `${environment.apiURL}/digests`;

	constructor(private http: HttpClient) { }

	getAllDigests() {
		return this.http.get<Digest[]>(`${this.baseURL}/all`);
	}

	getAllDigestTitles() {
		return this.http.get<DigestTitle[]>(`${this.baseURL}/all/titles`);
	}

	getOwnDigests() {
		return this.http.get<Digest[]>(`${this.baseURL}/own`);
	}

	getOwnDigestTitles() {
		return this.http.get<DigestTitle[]>(`${this.baseURL}/own/titles`);
	}

	getDigest(id: number) {
		return this.http.get<Digest>(`${this.baseURL}/${id}`);
	}

	createDigest(newDigest: NewDigest) {
		return this.http.post<Digest>(this.baseURL, newDigest);
	}

	updateDigest(digest: Digest) {
		return this.http.put<Digest>(this.baseURL, digest);
	}

	deleteDigest(id: number) {
		return this.http.delete<void>(`${this.baseURL}/${id}`);
	}

	importOwnDigests(digestsData: Blob) {
		return this.http.post<void>(`${this.baseURL}/own/import`, digestsData);
	}

	exportOwnDigests() {
		return this.http.get(`${this.baseURL}/own/export`, { responseType: 'blob' });
	}

}
