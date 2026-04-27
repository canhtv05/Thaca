import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class EscapeStackService {
  private stack: (() => void)[] = [];

  register(handler: () => void) {
    this.stack.push(handler);
  }

  unregister(handler: () => void) {
    this.stack = this.stack.filter((h) => h !== handler);
  }

  trigger() {
    if (this.stack.length === 0) return;
    const last = this.stack[this.stack.length - 1];
    last();
  }
}
