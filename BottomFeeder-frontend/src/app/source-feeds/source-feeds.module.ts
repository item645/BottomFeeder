import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SourceFeedComponent } from './source-feed.component';
import { SourceFeedListComponent } from './source-feed-list.component';
import { SourceFeedsRoutingModule } from './source-feeds-routing.module';
import { ReactiveFormsModule } from '@angular/forms';
import { EntryFiltersModule } from '../entry-filters/entry-filters.module';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

@NgModule({
	declarations: [
		SourceFeedComponent,
		SourceFeedListComponent
	],
	imports: [
		SourceFeedsRoutingModule,
		ReactiveFormsModule,
		CommonModule,
		NgbModule,
		EntryFiltersModule
	],
	exports: [
		SourceFeedListComponent
	]
})
export class SourceFeedsModule { }
