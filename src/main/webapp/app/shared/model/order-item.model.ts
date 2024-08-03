import { IOrder } from 'app/shared/model/order.model';

export interface IOrderItem {
  id?: number;
  quantity?: number;
  totalPrice?: number;
  order?: IOrder | null;
}

export const defaultValue: Readonly<IOrderItem> = {};
