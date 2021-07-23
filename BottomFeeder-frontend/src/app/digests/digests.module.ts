import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DigestListComponent } from './digest-list.component';
import { DigestsRoutingModule } from './digests-routing.module';
import { ReactiveFormsModule } from '@angular/forms';
import { DigestComponent } from './digest.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { SourceFeedsModule } from '../source-feeds/source-feeds.module';

@NgModule({
	declarations: [
		DigestListComponent,
		DigestComponent
	],
	imports: [
		DigestsRoutingModule,
		ReactiveFormsModule,
		CommonModule,
		NgbModule,
		SourceFeedsModule
	]
})
export class DigestsModule { }
