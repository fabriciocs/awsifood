import { IRestaurant } from 'app/shared/model/restaurant.model';

export interface IMenu {
  id?: number;
  name?: string;
  description?: string | null;
  restaurant?: IRestaurant | null;
}

export const defaultValue: Readonly<IMenu> = {};
