import dayjs from 'dayjs';
import { ICustomer } from 'app/shared/model/customer.model';
import { OrderStatus } from 'app/shared/model/enumerations/order-status.model';

export interface IOrder {
  id?: number;
  orderDate?: dayjs.Dayjs;
  status?: keyof typeof OrderStatus;
  customer?: ICustomer | null;
}

export const defaultValue: Readonly<IOrder> = {};
