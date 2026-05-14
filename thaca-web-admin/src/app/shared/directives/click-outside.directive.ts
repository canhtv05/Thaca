import { Directive, ElementRef, EventEmitter, HostListener, Output, inject } from '@angular/core';

@Directive({
  selector: '[clickOutside]',
  standalone: true,
})
export class ClickOutsideDirective {
  private el = inject(ElementRef);

  @Output() clickOutside = new EventEmitter<void>();

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const host = this.el.nativeElement as HTMLElement;
    const path = event.composedPath();
    const clickedInside = path.includes(host);
    if (!clickedInside) {
      this.clickOutside.emit();
    }
  }
}
