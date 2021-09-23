import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DigestEntryFilterListComponent } from './digest-entry-filter-list.component';
import { SourceFeedEntryFilterListComponent } from './source-feed-entry-filter-list.component';
import { ReactiveFormsModule } from '@angular/forms';
import { EntryFilterListComponent } from './entry-filter-list.component';

@NgModule({
	declarations: [
		DigestEntryFilterListComponent,
		SourceFeedEntryFilterListComponent,
		EntryFilterListComponent
	],
	imports: [
		CommonModule,
		ReactiveFormsModule
	],
	exports: [
		EntryFilterListComponent,
		DigestEntryFilterListComponent,
		SourceFeedEntryFilterListComponent
	]
})
export class EntryFiltersModule { }
