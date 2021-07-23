import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminGuard } from '../core/auth/admin.guard';
import { DigestListComponent } from './digest-list.component';
import { DigestComponent } from './digest.component';

const sourceFeedsModule = () => import('../source-feeds/source-feeds.module').then(m => m.SourceFeedsModule);

const routes: Routes = [
	{ path: 'my', component: DigestListComponent },
	{ path: 'all', component: DigestListComponent, canActivate: [AdminGuard] },
	{ path: 'add', component: DigestComponent },
	{ path: 'edit/:id', component: DigestComponent },
	{ path: 'feeds', loadChildren: sourceFeedsModule },
	{ path: '**', redirectTo: 'my' }
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class DigestsRoutingModule { }
