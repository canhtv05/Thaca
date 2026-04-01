import { IconName, IconType } from '@ng-icons/core';
import { solarGlobalBoldDuotone } from '@ng-icons/solar-icons/bold-duotone';

export interface AppIconConfig {
  icon: string;
  name: IconName;
  type: IconType;
}

export const APP_CONFIG_ICONS: Record<string, AppIconConfig> = {
  solarGlobalBoldDuotone: {
    name: 'solarGlobalBoldDuotone' as IconName,
    icon: solarGlobalBoldDuotone,
    type: 'bold-duotone',
  },
};
