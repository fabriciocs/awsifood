import { IMenu } from 'app/shared/model/menu.model';

export interface IDish {
  id?: number;
  name?: string;
  price?: number;
  description?: string | null;
  spicyLevel?: number | null;
  menu?: IMenu | null;
}

export const defaultValue: Readonly<IDish> = {};
