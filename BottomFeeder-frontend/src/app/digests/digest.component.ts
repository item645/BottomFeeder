import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SessionStorageService } from 'ngx-webstorage';
import { Digest } from './digest.model';
import { DigestService } from './digest.service';
import { Location } from '@angular/common';

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
		private sessionStorage: SessionStorageService,
		public location: Location
	) { }

	ngOnInit() {
		this.digestId = this.route.snapshot.params['id'];

		let controls = {
			title: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(200)]],
			maxItems: [20, [Validators.required, Validators.min(1), Validators.max(500)]],
			isPrivate: [false]
		}
		this.form = this.formBuilder.group(controls);

		if (this.digestId) {
			this.digestService
				.getDigest(this.digestId)
				.subscribe(digest => this.setDigest(digest));
		}
			
	}

	private getTabIndexStorageKey() {
		return this.digestId ? `bf_digest_${this.digestId}_tab` : '';
	}

	get activeTabId(): number {
		let key = this.getTabIndexStorageKey();
		let value = key ? this.sessionStorage.retrieve(key) : null;
		return value ? value : 1;
	}

	set activeTabId(value: number) {
		let key = this.getTabIndexStorageKey();
		if (key)
			this.sessionStorage.store(key, value);
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
		this.f.maxItems.setValue(digest.maxItems);
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
