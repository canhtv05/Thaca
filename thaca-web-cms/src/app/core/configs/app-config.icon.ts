import { IconName, IconType } from '@ng-icons/core';
import { solarGlobalBoldDuotone } from '@ng-icons/solar-icons/bold-duotone';
import { solarUserBold, solarLockPasswordBold } from '@ng-icons/solar-icons/bold';
import { solarEye, solarEyeClosed } from '@ng-icons/solar-icons/outline';

export const APP_CONFIG_ICONS: Record<string, { name: IconName; icon: IconType }> = {
  solarGlobalBoldDuotone: {
    name: 'solarGlobalBoldDuotone',
    icon: solarGlobalBoldDuotone,
  },
  solarUserBold: {
    name: 'solarUserBold',
    icon: solarUserBold,
  },
  solarLockPasswordBold: {
    name: 'solarLockPasswordBold',
    icon: solarLockPasswordBold,
  },
  solarEye: {
    name: 'solarEye',
    icon: solarEye,
  },
  solarEyeClosed: {
    name: 'solarEyeClosed',
    icon: solarEyeClosed,
  },
};
