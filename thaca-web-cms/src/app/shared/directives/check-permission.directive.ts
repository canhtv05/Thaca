import { Directive, inject, Input, OnInit, TemplateRef, ViewContainerRef } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';

@Directive({
  selector: '[checkPermission]',
  standalone: true,
})
export class CheckPermissionDirective implements OnInit {
  /*
   *[checkPermission]="['USER_MAKER', 'USER_VIEWER']"
   */
  @Input('checkPermission') permission: string[] = [];
  /*
   *[checkPermission]="['USER_MAKER', 'USER_VIEWER']" allMatched="true"
   */
  @Input('checkPermissionAllMatched') allMatched = false;
  /*
   *[checkPermission]="['USER_MAKER', 'USER_VIEWER']; else denied"
    <ng-template #denied>
      <div>Người dùng không có quyền truy cập</div>
    </ng-template>
   */
  @Input('checkPermissionElse') else: TemplateRef<any> | null = null;

  private readonly tpl = inject(TemplateRef);
  private readonly vcr = inject(ViewContainerRef);
  private readonly auth = inject(AuthService);

  ngOnInit(): void {
    this.checkPermission();
  }

  private checkPermission(): void {
    this.vcr.clear();
    if (!this.auth.isAuthenticated()) {
      this.renderElseTemplate();
      return;
    }
    if (this.auth.isSuperAdmin()) {
      this.vcr.createEmbeddedView(this.tpl);
      return;
    }
    if (!this.permission.length) {
      this.vcr.createEmbeddedView(this.tpl);
      return;
    }

    const userPermissions = new Set<string>(
      Object.values(this.auth.user()?.roles ?? {}).flatMap((role) =>
        Object.entries(role)
          .filter(([_, value]) => value === 'GRANT')
          .map(([key]) => key),
      ),
    );
    const isAllowed = this.allMatched
      ? this.permission.every((p) => userPermissions.has(p))
      : this.permission.some((p) => userPermissions.has(p));
    if (isAllowed) {
      this.vcr.createEmbeddedView(this.tpl);
    } else {
      this.renderElseTemplate();
    }
  }

  private renderElseTemplate(): void {
    if (this.else) {
      this.vcr.createEmbeddedView(this.else);
    } else {
      this.vcr.clear();
    }
  }
}
