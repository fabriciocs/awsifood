import dayjs from 'dayjs';
import { PaymentType } from 'app/shared/model/enumerations/payment-type.model';

export interface IPayment {
  id?: number;
  paymentDate?: dayjs.Dayjs;
  amount?: number;
  paymentType?: keyof typeof PaymentType;
}

export const defaultValue: Readonly<IPayment> = {};
