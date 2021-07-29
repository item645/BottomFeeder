import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PrincipalService } from '../core/auth/principal.service';
import { Digest } from './digest.model';
import { DigestService } from './digest.service';
import { saveAs } from "file-saver";

@Component({
	selector: 'digest-list',
	templateUrl: './digest-list.component.html',
	styleUrls: ['./digest-list.component.css']
})
export class DigestListComponent implements OnInit {

	isOwnDigests: boolean = true;
	listTitle: string;

	digests: Digest[] = [];

	constructor(
		private route: ActivatedRoute, 
		private digestService: DigestService, 
		private principal: PrincipalService
	) { }

	ngOnInit() {
		let url = this.route.snapshot.url;
		if (url.length === 1) {
			let path = this.route.snapshot.url[0].path;
			if (path === 'my')
				this.isOwnDigests = true;
			else if (path === 'all')
				this.isOwnDigests = false;
			else
				throw new Error(`Unsupported path for this component: ${path}`);
		}
		else {
			throw new Error(`Route snapshot is expected to contain 1 element but contains ${url.length}: ${url}`);
		}

		this.listTitle = this.isOwnDigests ? 'My Digests' : 'All Digests';

		let digestsObservable = this.isOwnDigests ? this.digestService.getOwnDigests() : this.digestService.getAllDigests();
		digestsObservable.subscribe(digests => this.digests = digests);
	}


	deleteDigest(id: number) {
		this.digestService.deleteDigest(id).subscribe(() => this.digests = this.digests.filter(digest => digest.id !== id));
	}

	exportDigests() {
		let currentLogin = this.principal.getAccount().login;
		this.digestService.exportOwnDigests().subscribe(digestsData => saveAs(digestsData, `${currentLogin}_digests.json`));
	}
}
