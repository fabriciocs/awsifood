export interface IRestaurant {
  id?: number;
  name?: string;
  location?: string | null;
  rating?: number | null;
}

export const defaultValue: Readonly<IRestaurant> = {};
