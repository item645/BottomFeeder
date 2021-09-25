import { Injectable } from '@angular/core';
import { SessionStorageService } from 'ngx-webstorage';

@Injectable({
	providedIn: 'root'
})
export class ActiveTabMemorizerService {
	private digestEntity = 'digest';
	private sourceFeedEntity = 'source_feed';

	constructor(private sessionStorage: SessionStorageService) { }

	getDigestActiveTabId(digestId: number): number {
		return this.getActiveTabId(this.digestEntity, digestId);
	}

	setDigestActiveTabId(digestId: number, activeTabId: number) {
		this.setActiveTabId(this.digestEntity, digestId, activeTabId);
	}

	getSourceFeedActiveTabId(sourceFeedId: number): number {
		return this.getActiveTabId(this.sourceFeedEntity, sourceFeedId);
	}

	setSourceFeedActiveTabId(sourceFeedId: number, activeTabId: number) {
		this.setActiveTabId(this.sourceFeedEntity, sourceFeedId, activeTabId);
	}

	private getActiveTabId(entity: string, entityId: number) {
		let key = this.getTabIdStorageKey(entity, entityId);
		let value = key ? this.sessionStorage.retrieve(key) : null;
		return value ? value : 1;
	}
	
	private setActiveTabId(entity: string, entityId: number, activeTabId: number) {
		let key = this.getTabIdStorageKey(entity, entityId);
		if (key)
			this.sessionStorage.store(key, activeTabId);
	}

	private getTabIdStorageKey(entity: string, entityId: number) {
		return entityId ? `bf_${entity}_${entityId}_tab` : '';
	}
}
