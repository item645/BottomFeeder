import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EntryFilterList } from './entry-filter-list.model';
import { Condition, Connective, Element, EntryFilter } from './entry-filter.model';
import { EntryFilterService } from './entry-filter.service';
import { Observable } from "rxjs/Rx";

@Component({
	selector: 'entry-filter-list',
	template: ''
})
export class EntryFilterListComponent implements OnInit {

	form: FormGroup;
	saving = false;
	submitted = false;

	elementOptions = [
		{value: Element.AUTHOR, name: 'Author'},
		{value: Element.CATEGORIES, name: 'Categories'},
		{value: Element.CONTENT, name: 'Content'},
		{value: Element.LINK, name: 'Link'},
		{value: Element.PUBLISH_DATE, name: 'Publish Date'},
		{value: Element.TITLE, name: 'Title'},
		{value: Element.UPDATE_DATE, name: 'Update Date'}
	];

	private conditionOptions = new Map([
		[Condition.CONTAINS, {value: Condition.CONTAINS, name: 'Contains'}],
		[Condition.DOES_NOT_CONTAIN, {value: Condition.DOES_NOT_CONTAIN, name: 'Does Not Contain'}],
		[Condition.EQUALS, {value: Condition.EQUALS, name: 'Equals'}],
		[Condition.DOES_NOT_EQUAL, {value: Condition.DOES_NOT_EQUAL, name: 'Does Not Equal'}],
		[Condition.LESS_THAN, {value: Condition.LESS_THAN, name: 'Less Than'}],
		[Condition.MORE_THAN, {value: Condition.MORE_THAN, name: 'More Than'}]
	]);

	// This structure maps every element to the list of conditions applicable for that element
	elementConditionsOptions = new Map([
		[Element.AUTHOR, [
			this.conditionOptions.get(Condition.CONTAINS),
			this.conditionOptions.get(Condition.DOES_NOT_CONTAIN),
			this.conditionOptions.get(Condition.EQUALS),
			this.conditionOptions.get(Condition.DOES_NOT_EQUAL)
		]],
		[Element.CATEGORIES, [
			this.conditionOptions.get(Condition.CONTAINS),
			this.conditionOptions.get(Condition.DOES_NOT_CONTAIN)
		]],
		[Element.CONTENT, [
			this.conditionOptions.get(Condition.CONTAINS),
			this.conditionOptions.get(Condition.DOES_NOT_CONTAIN),
			this.conditionOptions.get(Condition.EQUALS),
			this.conditionOptions.get(Condition.DOES_NOT_EQUAL)
		]],
		[Element.LINK, [
			this.conditionOptions.get(Condition.CONTAINS),
			this.conditionOptions.get(Condition.DOES_NOT_CONTAIN),
			this.conditionOptions.get(Condition.EQUALS),
			this.conditionOptions.get(Condition.DOES_NOT_EQUAL)
		]],
		[Element.PUBLISH_DATE, [
			this.conditionOptions.get(Condition.EQUALS),
			this.conditionOptions.get(Condition.DOES_NOT_EQUAL),
			this.conditionOptions.get(Condition.LESS_THAN),
			this.conditionOptions.get(Condition.MORE_THAN)
		]],
		[Element.TITLE, [
			this.conditionOptions.get(Condition.CONTAINS),
			this.conditionOptions.get(Condition.DOES_NOT_CONTAIN),
			this.conditionOptions.get(Condition.EQUALS),
			this.conditionOptions.get(Condition.DOES_NOT_EQUAL)
		]],
		[Element.UPDATE_DATE, [
			this.conditionOptions.get(Condition.EQUALS),
			this.conditionOptions.get(Condition.DOES_NOT_EQUAL),
			this.conditionOptions.get(Condition.LESS_THAN),
			this.conditionOptions.get(Condition.MORE_THAN)
		]]
	]);

	connectiveOptions = Object.keys(Connective);

	constructor(protected entryFilterService: EntryFilterService, protected formBuilder: FormBuilder) {}

	ngOnInit() {
		this.form = this.formBuilder.group({
			filters: new FormArray([])
		});

		this.loadFilters().subscribe(entryFilterList => this.setEntryFilterList(entryFilterList));
	}

	get f() {
		return this.form.controls;
	}

	get filters() {
		return this.f.filters as FormArray;
	}

	save() {
		this.submitted = true;
		if (this.form.invalid)
			return;

		this.saveFilters(this.form.value).subscribe(
			entryFilterList => {
				this.setEntryFilterList(entryFilterList);
				this.completeSaving();
			},
			() => this.completeSaving());
	}

	protected loadFilters(): Observable<EntryFilterList> {
		throw this.abstractMethodError();
	}

	protected saveFilters(entryFilterList: EntryFilterList): Observable<EntryFilterList> {
		throw this.abstractMethodError();
	}

	private abstractMethodError() {
		return new Error('This method must be overridden by subclass');
	}

	private setEntryFilterList(entryFilterList: EntryFilterList) {
		this.filters.clear();
		for (let i = 0; i < entryFilterList.filters.length; i++)
			this.addExistingEntryFilter(entryFilterList.filters[i], i == entryFilterList.filters.length - 1)
	}

	private addExistingEntryFilter(entryFilter: EntryFilter, isLast: boolean) {
		this.filters.push(this.formBuilder.group({
			id: [entryFilter.id],
			ordinal: [entryFilter.ordinal],
			element: [entryFilter.element, Validators.required],
			condition: [entryFilter.condition, Validators.required],
			value: [entryFilter.value, Validators.maxLength(200)],
			connective: [{value: entryFilter.connective, disabled: isLast}, isLast ? [] : [Validators.required]]
		}));
	}

	addNewEntryFilter() {
		let lastConnective = this.getLastConnective();
		if (lastConnective) {
			if (!lastConnective.value)
				lastConnective.setValue(Connective.AND);
			lastConnective.enable();
			lastConnective.setValidators([Validators.required]);
			lastConnective.updateValueAndValidity();
		}

		this.filters.push(this.formBuilder.group({
			id: [null],
			ordinal: [this.getNextOrdinal()],
			element: [null, Validators.required],
			condition: [null, Validators.required],
			value: ['', Validators.maxLength(200)],
			connective: [{value: null, disabled: true}]
		}));
	}

	removeEntryFilter(filterIndex: number) {
		let isLast = filterIndex == this.filters.length - 1;
		this.filters.removeAt(filterIndex);
		if (isLast) {
			let lastConnective = this.getLastConnective();
			if (lastConnective) {
				lastConnective.setValue(null);
				lastConnective.disable();
				lastConnective.setValidators([]);
				lastConnective.updateValueAndValidity();
			}
		}
	}

	onElementChange(filterIndex: number) {
		let element = this.filters.controls[filterIndex].get('element');
		let condition = this.filters.controls[filterIndex].get('condition');
		
		// If element changed, check that current condition is valid for new element;
		// if it is not, replace it with first applicable condition.
		let options = this.elementConditionsOptions.get(element.value);
		if (!options.some(option => option.value === condition.value)) {
			condition.setValue(options[0].value);
			condition.updateValueAndValidity();
		}
	}

	private getNextOrdinal() {
		let filtersArray = this.filters.controls;
		return filtersArray.length ? filtersArray[filtersArray.length - 1].get('ordinal').value + 1 : 1;
	}

	private getLastConnective() {
		let filtersArray = this.filters.controls;
		return filtersArray.length ? filtersArray[filtersArray.length - 1].get('connective') : null;
	}

	private completeSaving() {
		this.saving = false;
		this.submitted = false;
	}

}
