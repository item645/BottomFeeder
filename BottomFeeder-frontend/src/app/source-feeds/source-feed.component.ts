import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NewSourceFeed, SourceFeed } from './source-feed.model';
import { SourceFeedService } from './source-feed.service';
import { Location } from '@angular/common';
import { DigestTitle } from '../digests/digest.model';
import { DigestService } from '../digests/digest.service';
import { PrincipalService } from '../core/auth/principal.service';
import { Observable } from 'rxjs';

@Component({
	selector: 'app-source-feed',
	templateUrl: './source-feed.component.html',
	styleUrls: ['./source-feed.component.css']
})
export class SourceFeedComponent implements OnInit {

	form: FormGroup;
	saving = false;
	savingAndUpdating = false;
	submitted = false;

	digestId: number;
	ownDigestTitles: DigestTitle[] = [];
	otherDigestTitles: DigestTitle[] = [];

	private sourceFeedId: number;
	sourceFeed: SourceFeed;

	constructor(
		private formBuilder: FormBuilder,
		private route: ActivatedRoute,
		private router: Router,
		private sourceFeedService: SourceFeedService,
		private digestService: DigestService,
		private principal: PrincipalService,
		public location: Location
	) {
		this.sourceFeedId = this.route.snapshot.params['id'];

		let state = this.router.getCurrentNavigation().extras.state;
		if (state && state.digestId)
			this.digestId = state.digestId;
	}

	ngOnInit() {
		let controls = {
			digestId: [this.digestId, Validators.required],
			source: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(400)]],
			contentUpdateInterval: [60, [Validators.required, Validators.min(10), Validators.max(1440)]]
		}
		this.form = this.formBuilder.group(controls);

		if (this.sourceFeedId) {
			this.sourceFeedService
				.getSourceFeed(this.sourceFeedId)
				.subscribe(sourceFeed => this.setSourceFeed(sourceFeed));
		}
		else {
			this.loadDigestTitles();
		}

	}

	private setSourceFeed(sourceFeed: SourceFeed) {
		this.sourceFeed = sourceFeed;
		this.digestId = sourceFeed.digestId;
		this.f.digestId.setValue(this.digestId);
		this.f.source.setValue(sourceFeed.source);
		this.f.contentUpdateInterval.setValue(sourceFeed.contentUpdateInterval);
		this.loadDigestTitles();
	}

	private loadDigestTitles() {
		let digestTitlesObservable: Observable<DigestTitle[]>;
		if (this.principal.isAdmin())
			digestTitlesObservable = this.digestService.getAllDigestTitles();
		else
			digestTitlesObservable = this.digestService.getOwnDigestTitles();

		digestTitlesObservable.subscribe(digestTitles => {
			this.ownDigestTitles = [];
			this.otherDigestTitles = [];
			let currentLogin = this.principal.getAccount().login;
			for (let digestTitle of digestTitles) {
				if (digestTitle.ownerLogin == currentLogin)
					this.ownDigestTitles.push(digestTitle);
				else
					this.otherDigestTitles.push(digestTitle);
			}
		});
	}

	get f() {
		return this.form.controls;
	}

	isSaving() {
		return this.saving || this.savingAndUpdating;
	}

	save() {
		this.doSave(false);
	}

	saveAndUpdate() {
		this.doSave(true);
	}

	private doSave(updateContent: boolean) {
		this.submitted = true;
		if (this.form.invalid)
			return;

		this.saving = !updateContent;
		this.savingAndUpdating = updateContent;

		if (!this.sourceFeed)
			this.createSourceFeed(updateContent);
		else
			this.updateSourceFeed(updateContent);
	}

	private createSourceFeed(updateContent: boolean) {
		let newSourceFeed: NewSourceFeed = Object.assign(this.form.value, { updateContent: updateContent });
		this.sourceFeedService
			.createSourceFeed(newSourceFeed)
			.subscribe(
				sourceFeed => this.router.navigate([`../edit/${sourceFeed.id}`], { relativeTo: this.route, replaceUrl: true }),
				() => this.completeSaving()
			);
	}

	private updateSourceFeed(updateContent: boolean) {
		let updatedSourceFeed: SourceFeed = Object.assign(this.form.value, { id: this.sourceFeed.id, updateContent: updateContent });
		this.sourceFeedService
			.updateSourceFeed(updatedSourceFeed)
			.subscribe(
				sourceFeed => {
					this.setSourceFeed(sourceFeed);
					this.completeSaving();
				},
				() => this.completeSaving()
			);
	}

	private completeSaving() {
		this.saving = false;
		this.savingAndUpdating = false;
	}
}
