export interface ICustomer {
  id?: number;
  name?: string;
  email?: string;
  phoneNumber?: string | null;
  address?: string | null;
}

export const defaultValue: Readonly<ICustomer> = {};
