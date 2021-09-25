import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Digest } from './digest.model';
import { DigestService } from './digest.service';
import { Location } from '@angular/common';
import { ActiveTabMemorizerService } from '../core/active-tab-memorizer.service';

@Component({
	selector: 'app-digest',
	templateUrl: './digest.component.html',
	styleUrls: ['./digest.component.css']
})
export class DigestComponent implements OnInit {

	form: FormGroup;
	loading = false;
	submitted = false;

	private digestId: number;
	digest: Digest;

	constructor(
		private formBuilder: FormBuilder,
		private route: ActivatedRoute,
		private router: Router,
		private digestService: DigestService,
		public location: Location,
		private activeTabMemorizer: ActiveTabMemorizerService
	) { }

	ngOnInit() {
		this.digestId = this.route.snapshot.params['id'];

		let controls = {
			title: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(200)]],
			maxEntries: [20, [Validators.required, Validators.min(1), Validators.max(500)]],
			isPrivate: [false]
		}
		this.form = this.formBuilder.group(controls);

		if (this.digestId) {
			this.digestService
				.getDigest(this.digestId)
				.subscribe(digest => this.setDigest(digest));
		}
			
	}

	get activeTabId(): number {
		return this.activeTabMemorizer.getDigestActiveTabId(this.digestId);
	}

	set activeTabId(value: number) {
		this.activeTabMemorizer.setDigestActiveTabId(this.digestId, value);
	}

	get f() {
		return this.form.controls;
	}

	save() {
		this.submitted = true;
		if (this.form.invalid)
			return;

		this.loading = true;
		if (!this.digest)
			this.createDigest();
		else
			this.updateDigest();
	}

	private setDigest(digest: Digest) {
		this.digest = digest;
		this.f.title.setValue(digest.title);
		this.f.maxEntries.setValue(digest.maxEntries);
		this.f.isPrivate.setValue(digest.isPrivate);
	}

	private createDigest() {
		this.digestService
			.createDigest(this.form.value)
			.subscribe(
				digest => this.router.navigate([`../edit/${digest.id}`], { relativeTo: this.route, replaceUrl: true }),
				() => this.loading = false
			);
	}

	private updateDigest() {
		let updatedDigest: Digest = Object.assign(this.form.value, { id: this.digest.id });
		this.digestService
			.updateDigest(updatedDigest)
			.subscribe(
				digest => {
					this.setDigest(digest);
					this.loading = false;
				},
				() => this.loading = false
			);
	}
}
