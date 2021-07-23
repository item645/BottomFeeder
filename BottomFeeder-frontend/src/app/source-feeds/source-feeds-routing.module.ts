import { NgModule } from '@angular/core';
import { SourceFeedComponent } from './source-feed.component';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
	{ path: 'add', component: SourceFeedComponent },
	{ path: 'edit/:id', component: SourceFeedComponent },
	{ path: '**', redirectTo: 'add' }
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class SourceFeedsRoutingModule { }
